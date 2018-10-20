package mobile.pamdas.org.roam;

import android.content.Context;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;

import org.json.JSONArray;

public class DasClient {

    private final String mProvider;
    private final String mAuthToken;
    private final Context mContext;
    private String mDasEndpoint;

    public DasClient(String provider, String token, Context ctx) {
        mProvider = provider;
        mAuthToken = token;
        mContext = ctx;
        AndroidNetworking.initialize(mContext.getApplicationContext());
    }

    public void setDasEndpoint(String endpoint) {
        mDasEndpoint = endpoint;
    }

    public void postRadioSensorUpdate(RadioObservation obs) {
        String sensorEndpoint = String.format("%s/sensor/update", mDasEndpoint);
        AndroidNetworking.post(sensorEndpoint)
                .addJSONObjectBody(obs.toJson()) // posting json
                .setTag("test")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // do anything with response
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                    }
                });

    }
}
