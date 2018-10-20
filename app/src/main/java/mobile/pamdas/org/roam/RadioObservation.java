package mobile.pamdas.org.roam;

import android.location.Location;

import org.json.JSONObject;

import java.util.Date;

class RadioObservation {

    private String mSensorType = "dasradioagent";
    private Date mObservedDate;
    private Location mObservedLocation;
    private DasDevice mDeviceInfo;

    public RadioObservation(DasDevice device, Location loc, Date observed) {
        mDeviceInfo = device;
        mObservedLocation = loc;
        mObservedDate = observed;
    }

    public JSONObject toJson() {
        return new JSONObject();
    }


}
