package mobile.pamdas.org.roam;

import android.content.Context;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;

import org.json.JSONArray;

public class DasClient {

    private static final String TAG = MainActivity.class.getSimpleName();
    // todo make provider configurable
    private final String mProvider = "roam-mobile";
    // todo - get this out of code. maybe put this is the keystore?
    private final String mAuthToken;
    private final Context mContext;
    private String mDasEndpoint;

    public DasClient(Context ctx) {
        mContext = ctx;
        mAuthToken = mContext.getString(R.string.das_api_token);
        mDasEndpoint = mContext.getString(R.string.das_api_endpoint);
        AndroidNetworking.initialize(mContext.getApplicationContext());
    }

    public void setDasEndpoint(String endpoint) {
        mDasEndpoint = endpoint;
    }

    public void postRadioSensorUpdate(RadioObservation obs) {
        String sensorEndpoint = String.format("%s/sensors/dasradioagent/roam-mobile/status", mDasEndpoint);
        Log.d(TAG, obs.toJson().toString());
        AndroidNetworking.post(sensorEndpoint)
                .setContentType("application/json; charset=utf-8")
                .addHeaders("Authorization", "Bearer " + mAuthToken)
                .addJSONObjectBody(obs.toJson()) // posting json
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.i(TAG, response.toString());
                    }
                    @Override
                    public void onError(ANError error) {
                        Log.e(TAG, error.getErrorBody());
                    }
                });

    }
}
