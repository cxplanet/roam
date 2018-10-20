package mobile.pamdas.org.roam;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    // logging identity
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private Location mCurrLocation;
    private boolean mRequestingLocationUpdates = true;
    private LocationCallback mLocationCallback;
    private Switch mObserveSwitch;
    private EditText mSensorMfgId;
    private EditText mDeviceName;
    private EditText mProviderId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mObserveSwitch = (Switch) findViewById(R.id.observeSwitch);
        mObserveSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && validateDeviceSettings()) {
                    startLocationUpdates();
                }
            }
        });

        mSensorMfgId = (EditText) findViewById(R.id.mfg_id);
        mDeviceName = (EditText) findViewById(R.id.model_name);
        mProviderId = (EditText) findViewById(R.id.provider_id);

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
            };
        };

        requestLocationPermissions();

    }

    private boolean validateDeviceSettings() {
        // TODO
        return true;
        //  Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        //     findViewById(R.id.pw).startAnimation(shake);
        //     Toast.makeText(this, "Device name must be specifiec", Toast.LENGTH_SHORT).show();
        // }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        if (mRequestingLocationUpdates && checkLocationPermissions()) {
            startLocationUpdates();
        }
    }

    private boolean checkLocationPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    // try to get the user to allow the app to run by enabling location permissions
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
            Log.i(TAG, "Requesting location permission");
            // This is ignored if the user has already delined
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    // TODO - Let this be configurable - you'll need GPS in some locations
    protected LocationRequest createLocationRequest() {
        LocationRequest locReq = new LocationRequest();
        locReq.setInterval(30000);
        locReq.setFastestInterval(10000);
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
}
