package com.atolcd.alfresco.helper;

import org.json.JSONException;
import org.json.JSONObject;

import com.atolcd.alfresco.AuditFilterConstants;

public class AuditHelper {
    /**
     * Extrait l'action ayant déclenchée l'activité
     * 
     * @param json
     * @return
     * @throws JSONException
     */
    public static String extractActionFromActivity(JSONObject json) throws JSONException {
        String type = null;
        if (json.has("type")) {
            String[] tokens = json.getString("type").split("\\.");
            if (tokens.length > 0) {
                type = tokens[tokens.length - 1];
            }
        }
        return type;
    }

    /**
     * Extrait le module concerné par l'activité
     * 
     * @param json
     * @return
     * @throws JSONException
     */
    public static String extractModFromActivity(JSONObject json) throws JSONException {
        String mod = null;
        if (json.has("appTool")) {
            String tool = json.getString("appTool");
            if ("datalists".equals(tool)) {
                mod = AuditFilterConstants.MOD_DOCUMENT;
            } else if ("documentlibrary".equals(tool)) {
                mod = AuditFilterConstants.MOD_DOCUMENT;
            }
        }
        return mod;
    }
}
