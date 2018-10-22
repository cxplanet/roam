package mobile.pamdas.org.roam;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.time.Instant;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    // logging identity
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private DasClient mDasClient;
    private DasRadioDevice mRadioDevice;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private Location mCurrLocation;

    private boolean mRequestingLocationUpdates = false;
    private LocationCallback mLocationCallback;
    private Switch mObserveSwitch;
    //private EditText mSensorMfgId;
    private EditText mDeviceName;
    //private EditText mProviderId;
    private String mDeviceGuid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // TODO: use the imei for the sensor id in secure settings
//        TelephonyManager telephonyManager;
//        telephonyManager = (TelephonyManager) getSystemService(Context.
//                TELEPHONY_SERVICE);
//        mDeviceIMEI = telephonyManager.getImei();
        mObserveSwitch = (Switch) findViewById(R.id.observeSwitch);
        mObserveSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (validateDeviceSettings()) {
                        mRequestingLocationUpdates = true;
                        startLocationUpdates();
                    }
                    else {
                        mObserveSwitch.setChecked(false);
                    }
                }
                else {
                    mRequestingLocationUpdates = false;
                    stopLocationUpdates();
                }
            }
        });

        mDeviceName = (EditText) findViewById(R.id.model_name);
        mDeviceName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                int result = actionId & EditorInfo.IME_MASK_ACTION;
                switch(result) {
                    case EditorInfo.IME_ACTION_DONE:
                        hideKeyboard(v);
                        v.clearFocus();
                        setDeviceName();
                        break;
                    case EditorInfo.IME_ACTION_NEXT:
                        Log.d(TAG, "Next key");
                        break;
                }
                return false;
            }
        });

        // this replaces the googleapiclient of last year - soe day google
        // is going to stabilize their geo apis...
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationRequest = createLocationRequest();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                LocationNotifier lrh = new LocationNotifier(MainActivity.this, locationResult.getLocations());
                lrh.showNotification();
                if(mRequestingLocationUpdates) {
                    for (Location loc : locationResult.getLocations()) {
                        RadioObservation obs = new RadioObservation(mRadioDevice, loc, Instant.now());
                        mDasClient.postRadioSensorUpdate(obs);
                    }
                }
            };
        };

        mDeviceGuid = Utils.deviceId(this);
        mDasClient = new DasClient(this);

        String deviceName = Utils.getDeviceName(this);
        if(deviceName != null)
            mDeviceName.setText(deviceName);
            mRadioDevice = new DasRadioDevice(mDeviceGuid, deviceName);

        requestLocationPermissions();

    }

    private void setDeviceName() {
        String devName = mDeviceName.getText().toString();
        mRadioDevice = new DasRadioDevice(mDeviceGuid, devName);
        Utils.setDeviceName(devName, this);
    }

    private void hideKeyboard(View view) {
        InputMethodManager manager = (InputMethodManager) view.getContext()
                .getSystemService(INPUT_METHOD_SERVICE);
        if (manager != null)
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private boolean validateDeviceSettings() {
        if (TextUtils.isEmpty(mDeviceName.getText())) {
            Snackbar.make(
                    findViewById(R.id.activity_main),
                    "Please specify a device name to enable tracking",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.i(TAG, "Name and mfg id are required");
                        }
                    })
                    .show();
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        if (mRequestingLocationUpdates && checkLocationPermissions()) {
            startLocationUpdates();
        }
    }

    // TODO make the permissions check DRY
    private boolean checkTelephonyPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkLocationPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    // ask user to allow the app to enable location permissions
    private void requestLocationPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. Hardly ever used.
        if (shouldProvideRationale) {
            Log.i(TAG, "Secondary request for location permission");
            Snackbar.make(
                    findViewById(R.id.activity_main),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            // This is ignored if the user has already delined
            Log.i(TAG, "Requesting location permissions");
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    // todo - This needs a sensible perission question. Also, IMEI might
    // be a bit too intrusive to use as a sensor id
    protected void requestTelephonyState() {
        Snackbar.make(
                findViewById(R.id.activity_main),
                "Roam would like to use the IMEI of ths device",
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Request permission
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.READ_PHONE_STATE},
                                REQUEST_PERMISSIONS_REQUEST_CODE);
                    }
                })
                .show();
    }

    // TODO - Let this be configurable - you'll need GPS in some locations
    protected LocationRequest createLocationRequest() {
        LocationRequest locReq = new LocationRequest();
        locReq.setInterval(100000);
        locReq.setFastestInterval(100000);
        locReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locReq;
    }

    // becasue android studio doesn't see the above permissions call
    // we have to suppress the bogus permissions warning
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null);
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }
}
