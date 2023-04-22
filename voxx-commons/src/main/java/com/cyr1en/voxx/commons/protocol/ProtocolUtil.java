package com.cyr1en.voxx.commons.protocol;

import org.json.JSONObject;

public class ProtocolUtil {

    public static String flattenJSONObject(JSONObject jsonObject) {
        return jsonObject.toString().replaceAll("\\s{2,}|\\n", "");
    }
}
