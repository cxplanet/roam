package mobile.pamdas.org.roam;

import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

class RadioObservation {

    private String mSensorType = "dasradioagent";
    private Instant mObservedDate;
    private Location mObservedLocation;
    private DasRadioDevice mDeviceInfo;

    public RadioObservation(DasRadioDevice device, Location loc, Instant observed) {
        mDeviceInfo = device;
        mObservedLocation = loc;
        mObservedDate = observed;
    }

    public JSONObject toJson() {
        JSONObject params = new JSONObject(mDeviceInfo.getMap());
        String utcDate = mObservedDate.toString();
        try {
            params.put("recorded_at", utcDate);
            params.put("location", buildLocationObj(mObservedLocation));
            params.put("additional", buildAdditionalObj());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return params;
    }

    private JSONObject buildLocationObj(Location loc) throws JSONException {
        JSONObject location = new JSONObject();
        location.put("lat", loc.getLatitude());
        location.put("lon", loc.getLongitude());
        return location;
    }

    private JSONObject buildAdditionalObj() throws JSONException {
        JSONObject additional = new JSONObject();
        additional.put("event_action", "roam app action");
        additional.put("radio_state", "roam app state");
        additional.put("radio_state_at", Instant.now().toString());
        return additional;
    }


}
