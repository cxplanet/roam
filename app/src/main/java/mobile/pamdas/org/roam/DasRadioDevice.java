package mobile.pamdas.org.roam;


import java.util.HashMap;
import java.util.Map;

class DasRadioDevice {

    private String mMfgId;
    private String mSubjectName;
    private Map<String, String> mKeyVals;
    // TODO - expose these as configurables in the UI
    private final String mSourceType = "gps-radio";
    private final String mSubjectSubtype = "ranger";
    private final String mProvider = "roam-mobile";

    public DasRadioDevice(String sensorId, String subjectName) {
        mMfgId = sensorId;
        mSubjectName = subjectName;
        mKeyVals = toMap();
    }

    public Map getMap() {
        return mKeyVals;
    }

    private Map<String, String> toMap() {

        Map keyVals = new HashMap<String, String>();
        keyVals.put("message_key", "observation");
        keyVals.put("manufacturer_id", mMfgId);
        keyVals.put("source_type", mSourceType);
        keyVals.put("subject_name", mSubjectName);
        keyVals.put("subject_subtype_id", mSubjectSubtype);

        return keyVals;
    }

}
