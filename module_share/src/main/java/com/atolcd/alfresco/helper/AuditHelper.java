/*
 * Copyright (C) 2018 Atol Conseils et Développements.
 * http://www.atolcd.com/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atolcd.alfresco.helper;

import org.json.JSONException;
import org.json.JSONObject;

import com.atolcd.alfresco.AuditFilterConstants;

public class AuditHelper {
    /**
     * Extract the action that triggered the activity
     * 
     * @param json
     * @return String Type
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
     * Extract the module concerned by the activity
     * 
     * @param json
     * @return String Module
     * @throws JSONException
     */
    public static String extractModFromActivity(JSONObject json) throws JSONException {
        String mod = null;
        if (json.has("appTool")) {
            String tool = json.getString("appTool");
            if ("datalists".equals(tool)) {
                mod = AuditFilterConstants.MOD_DATA;
            } else if ("documentlibrary".equals(tool)) {
                mod = AuditFilterConstants.MOD_DOCUMENT;
            }
        }
        return mod;
    }
}