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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.FrameworkUtil;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.RequestContextUtil;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.surf.exception.RequestContextException;
import org.springframework.extensions.surf.exception.ResourceLoaderException;
import org.springframework.extensions.surf.exception.UserFactoryException;
import org.springframework.extensions.surf.site.AuthenticationUtil;
import org.springframework.extensions.surf.support.AlfrescoUserFactory;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.ConnectorContext;
import org.springframework.extensions.webscripts.connector.HttpMethod;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.web.context.support.WebApplicationContextUtils;

@SuppressWarnings("deprecation")
public class AuditFilter extends AuditFilterConstants implements Filter {
    // Logger
    private static final Log logger = LogFactory.getLog(AuditFilter.class);

    public static final String KEY_SITE = "site";
    public static final String KEY_MODULE = "module";
    public static final String KEY_OBJECT = "object";
    public static final String KEY_ACTION = "action";

    private ServletContext servletContext;
    // Store the "object" parameters to get for each module
    private static HashMap<String, String> moduleIds;
    private static HashMap<String, String[]> ignoredCases;
    private static Set<String> ignoredUrl;

    // Id used to identify an audit (in the database) from the repository (and
    // not from a site).
    private static final String REPOSITORY_SITE = "/service";

    static {
        // Exact URLs to be filtered
        ignoredUrl = new HashSet<String>();
        ignoredUrl.add("/share/page");
        ignoredUrl.add("/share/page/");
        ignoredUrl.add("/share/page/dologin");
        ignoredUrl.add("/share/page/dologin/");
        ignoredUrl.add("/share/page/dologout");
        ignoredUrl.add("/share/page/dologout/");
        ignoredUrl.add("/share/page/console");
        ignoredUrl.add("/share/page/console/");

        // Ignored cases (sites)
        ignoredCases = new HashMap<String, String[]>();
        ignoredCases.put("wiki", new String[] { "create" });
        ignoredCases.put("blog", new String[] { "postedit" });
        ignoredCases.put("links", new String[] { "linkedit" });
        ignoredCases.put("discussions", new String[] { "createtopic" });
        ignoredCases.put("calendar", new String[] { "month", "week", "day" });

        // Array: 'module -> parameters'
        // Used to read 'audit' settings
        moduleIds = new HashMap<String, String>();
        moduleIds.put("wiki", "title");
        moduleIds.put("blog", "postId");
        moduleIds.put("document", "nodeRef");
        moduleIds.put("documentlibrary", "filter");
        moduleIds.put("calendar", "view");
        moduleIds.put("links", "linkId");
        moduleIds.put("discussions", "topicId");
        moduleIds.put("data", "list");
        moduleIds.put("members", "");
        moduleIds.put("dashboard", "");
        moduleIds.put("search", "t");
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig args) throws ServletException {
        this.servletContext = args.getServletContext();
    }

    private ApplicationContext getApplicationContext() {
        return WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
    }

    @Override
    public void doFilter(ServletRequest sReq, ServletResponse sRes, FilterChain chain) throws IOException, ServletException {

        // Get the HTTP request/response/session
        HttpServletRequest request = (HttpServletRequest) sReq;

        // Initialize a new request context
        RequestContext context = ThreadLocalRequestContext.getRequestContext();

        if (context == null) {
            try {
                // Perform a "silent" init - i.e. no user creation or remote
                // connections
                context = RequestContextUtil.initRequestContext(getApplicationContext(), request, true);
                try {
                    RequestContextUtil.populateRequestContext(context, request);
                } catch (ResourceLoaderException e) {
                  logger.debug(e);
                } catch (UserFactoryException e) {
                  logger.debug(e);
                }
            } catch (RequestContextException ex) {
                throw new ServletException(ex);
            }
        }

        String userId = AuthenticationUtil.getUserId(request);
        if (userId == null || userId.trim().length() == 0) {
            userId = request.getRemoteUser();
        }

        String requestURI = request.getRequestURI();

        if ((userId != null && userId.trim().length() > 0) && (requestURI != null && requestURI.trim().length() > 0) && !ignoredUrl.contains(requestURI)) {
            try {
                // Preparation of JSON to send.
                JSONObject auditSample = new JSONObject();
                auditSample.put(AUDIT_ID, "0");
                auditSample.put(AUDIT_USER_ID, userId);

                String ref = request.getHeader("referer");
                if (requestURI.endsWith("/dologin") && (ref != null)) {
                    requestURI = ref;
                }
                HashMap<String, String> auditData = getAuditData(request, userId, requestURI);
                // Parsing sometimes includes parameters when the page loading
                // is interrupted prematurely
                if ((auditData.get(KEY_MODULE).length() > 0) && (!auditData.get(KEY_ACTION).contains("?"))) {
                    auditSample.put(AUDIT_SITE, auditData.get(KEY_SITE));
                    auditSample.put(AUDIT_APP_NAME, auditData.get(KEY_MODULE));
                    auditSample.put(AUDIT_ACTION_NAME, auditData.get(KEY_ACTION));
                    auditSample.put(AUDIT_OBJECT, auditData.get(KEY_OBJECT));
                    auditSample.put(AUDIT_TIME, Long.toString(System.currentTimeMillis()));

                    // Remote call for DB
                    if (moduleIds.containsKey(auditSample.get(AUDIT_APP_NAME))) {
                        remoteCall(request, auditSample);
                    } else {
                        logger.info("Ignored: " + requestURI);
                    }
                }
            } catch (Exception e) {
                logger.error(" Error while auditing data in AuditFilter");
                if (logger.isDebugEnabled()) {
                    logger.debug(e.getMessage(), e);
                }
            }
        }
        chain.doFilter(sReq, sRes);
    }

    private void remoteCall(HttpServletRequest request, JSONObject auditSample) throws JSONException, URIException,
            UnsupportedEncodingException {
        Connector connector;
        try {
            connector = FrameworkUtil.getConnector(request.getSession(true), auditSample.getString(AUDIT_USER_ID),
                    AlfrescoUserFactory.ALFRESCO_ENDPOINT_ID);
            // The webscript is called with the audit converted into JSON.
            ConnectorContext postContext = new ConnectorContext(null, buildDefaultHeaders());
            postContext.setMethod(HttpMethod.POST);
            postContext.setContentType("text/plain;charset=UTF-8");
            InputStream in = new ByteArrayInputStream(auditSample.toString().getBytes("UTF-8"));

            // Webscript call
            connector.call("/share-stats/insert-audit", postContext, in);
        } catch (ConnectorServiceException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage(), e);
            }
        }
    }

    /**
     * @param request
     * @param userId
     * @param siteId
     * @param componentId
     * @param objectId
     * @return
     * @throws JSONException
     * @throws URIException
     * @throws UnsupportedEncodingException
     */
    private String getNodeRefRemoteCall(HttpServletRequest request, String userId, String siteId, String componentId, String objectId)
            throws JSONException, URIException, UnsupportedEncodingException {
        Connector connector;
        try {
            connector = FrameworkUtil.getConnector(request.getSession(true), userId, AlfrescoUserFactory.ALFRESCO_ENDPOINT_ID);
            // <url>/share-stats/slingshot/details/{siteId}/{componentId}/{objectId}</url>
            Response resp = connector.call("/share-stats/slingshot/details/" + siteId + "/" + componentId + "/"
                    + URLEncoder.encode(objectId, "UTF-8"));

            if (resp.getStatus().getCode() == Status.STATUS_OK) {
                try {
                    JSONObject json = new JSONObject(resp.getResponse());
                    if (json.has("nodeRef")) {
                        return (String) json.get("nodeRef");
                    }
                } catch (JSONException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(e.getMessage(), e);
                    }
                }
            }
        } catch (ConnectorServiceException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage(), e);
            }
        }

        return objectId;
    }

    /**
     * Cut and analysis the url and analysis parameters
     *
     * @param request
     *            HttpServletRequest
     * @param requestURL
     *            String
     * @return HashMap
     */
    public HashMap<String, String> getAuditData(HttpServletRequest request, String userId, String requestURL) {
        HashMap<String, String> auditData = new HashMap<String, String>();
        String[] urlTokens = requestURL.split("/");

        Map<String, String> urlData = getUrlData(urlTokens, requestURL.indexOf("site") != -1);

        auditData.putAll(urlData);

        try {
            // Retrieves the id of the <<objet>> viewed from its module
            String obj = request.getParameter(moduleIds.get(urlData.get(KEY_MODULE)));
            if (obj != null) {
                if ("calendar".equals(auditData.get(KEY_MODULE))) {
                    auditData.put(KEY_ACTION, obj);
                    auditData.put(KEY_OBJECT, "");
                } else if (auditData.get(KEY_MODULE).equals(MOD_LINKS) && "view".equals(auditData.get(KEY_ACTION))) {
                    String auditObject = getNodeRefRemoteCall(request, userId, auditData.get(KEY_SITE), auditData.get(KEY_MODULE), obj);
                    auditData.put(KEY_OBJECT, auditObject);
                    auditData.put(KEY_ACTION, "single-view");
                } else if ("search".equals(auditData.get("module"))) {
                    if (!obj.isEmpty()) {
                        auditData.put(KEY_ACTION, "query");
                        auditData.put(KEY_OBJECT, obj);
                    } else {
                        auditData.put(KEY_OBJECT, "");
                    }

                } else {
                    String auditObject = getNodeRefRemoteCall(request, userId, auditData.get(KEY_SITE), auditData.get(KEY_MODULE), obj);
                    auditData.put(KEY_OBJECT, auditObject);
                }
            } else {
                auditData.put(KEY_OBJECT, "");
            }
        } catch (Exception e) {
            auditData.put(KEY_OBJECT, "");
            logger.trace(e);
        }

        return filter(auditData);
    }

    /**
     * URL parsing
     *
     * @param urlTokens
     * @param hasSite
     * @return HashMap
     */
    public Map<String, String> getUrlData(final String[] pUrlTokens, boolean hasSite) {
        String[] urlTokens = pUrlTokens;

        HashMap<String, String> urlData = new HashMap<String, String>();
        urlData.put(KEY_MODULE, "");
        urlData.put(KEY_ACTION, "");
        urlData.put(KEY_SITE, "");

        if (!hasSite) {
            String[] newUrlTokens = new String[urlTokens.length + 2];
            int j = 0;
            String token;
            for (int i = 0; i < urlTokens.length; i++) {
                token = urlTokens[i];
                newUrlTokens[i + j] = token;

                if ("page".equals(urlTokens[i])) {
                    newUrlTokens[i + 1] = "site";
                    newUrlTokens[i + 2] = REPOSITORY_SITE;
                    j = 2;
                }
            }

            urlTokens = newUrlTokens;
        }

        boolean siteFlag = false;
        int limit = urlTokens.length - 1;
        for (int i = 0; i < urlTokens.length; i++) {
            if ("site".equals(urlTokens[i]) && !siteFlag) {
                siteFlag = true;
            } else if (siteFlag && (urlData.get(KEY_SITE).isEmpty()) && i < limit) {
                urlData.put(KEY_SITE, urlTokens[i]);
                String[] splittedModuleAction = urlTokens[i + 1].split("-");
                // "site-members" & "site-groups" test
                if ("site".equals(splittedModuleAction[0])) {
                    urlData.put(KEY_MODULE, MOD_MEMBERS);
                } else {
                    urlData.put(KEY_MODULE, splittedModuleAction[0]);
                }

                // Action test (for example: module-action ; wiki-create)
                if (splittedModuleAction.length > 1) {
                    urlData.put(KEY_ACTION, splittedModuleAction[1]);
                } else if (splittedModuleAction.length == 1) {
                    if (urlData.get(KEY_MODULE).endsWith("library")) {
                        urlData.put(KEY_MODULE, MOD_DOCUMENT);
                        urlData.put(KEY_ACTION, "library");
                    } else if (urlData.get(KEY_MODULE).endsWith("search")) {
                        urlData.put(KEY_ACTION, splittedModuleAction[0]);
                        urlData.put(KEY_MODULE, "search");
                    } else {
                        urlData.put(KEY_ACTION, "view");
                    }
                }
            }
        }

        return urlData;
    }

    /**
     * Audit data filter
     *
     * @param auditData
     * @return HashMap
     */
    public HashMap<String, String> filter(HashMap<String, String> auditData) {
        String module = auditData.get(KEY_MODULE);
        String action = auditData.get(KEY_ACTION);

        if (module != null && action != null) {
            if (ignoredCases.containsKey(module)) {
                if (contains(ignoredCases.get(module), action)) {
                    // No audited if it does not contain site
                    auditData.put(KEY_SITE, "");
                }
            }
        }
        return auditData;
    }

    /**
     * Checks if a String is contained into an array
     *
     * @param array
     * @param toFind
     * @return boolean
     */
    public boolean contains(String[] array, String toFind) {
        boolean res = false;
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(toFind)) {
                res = true;
            }
        }
        return res;
    }

    /**
     * Helper to build a map of the default headers for script requests - we
     * send over the current users locale so it can be respected by any
     * appropriate REST APIs.
     *
     * @return map of headers
     */
    private static Map<String, String> buildDefaultHeaders() {
        Map<String, String> headers = new HashMap<String, String>(1, 1.0f);
        headers.put("Accept-Language", I18NUtil.getLocale().toString().replace('_', '-'));
        return headers;
    }
}
