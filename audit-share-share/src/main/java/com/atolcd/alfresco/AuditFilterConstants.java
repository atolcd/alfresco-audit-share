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
package com.atolcd.alfresco;

public class AuditFilterConstants {
    // Constants used by"hashmaps" and "parameters"
    public static final String AUDIT_ID = "id";
    public static final String AUDIT_USER_ID = "auditUserId";
    public static final String AUDIT_APP_NAME = "auditAppName";
    public static final String AUDIT_SITE = "auditSite";
    public static final String AUDIT_ACTION_NAME = "auditActionName";
    public static final String AUDIT_OBJECT = "auditObject";
    public static final String AUDIT_TIME = "auditTime";

    // Audited modules
    public static final String MOD_WIKI = "wiki";
    public static final String MOD_BLOG = "blog";
    public static final String MOD_LINKS = "links";
    public static final String MOD_DISCUSSIONS = "discussions";
    public static final String MOD_CALENDAR = "calendar";
    public static final String MOD_DATA = "data";
    public static final String MOD_MEMBERS = "members";
    public static final String MOD_DOCUMENT = "document";

    public static final String SITE_REPOSITORY = "_repository";
}