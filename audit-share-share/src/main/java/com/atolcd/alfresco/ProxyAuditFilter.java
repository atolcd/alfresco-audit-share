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
import java.util.Map;

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
import org.json.JSONArray;
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

import com.atolcd.alfresco.helper.AuditHelper;

@SuppressWarnings("deprecation")
public class ProxyAuditFilter extends AuditFilterConstants implements Filter {
    // Logger
    private static final Log    logger                = LogFactory.getLog(ProxyAuditFilter.class);

    private ServletContext      servletContext;

    // XXX: externalize configuration?
    public static final String  SHARE_WEBAPP_NAME     = "share";
    public static final String  ALFRESCO_ENDPOINT_ID  = "alfresco";
    public static final String  SHORT_PROXY_URL       = "/" + SHARE_WEBAPP_NAME + "/page/proxy/" + ALFRESCO_ENDPOINT_ID + "/";

    public static final String  KEY_SITE              = "site";
    public static final String  KEY_MODULE            = "module";
    public static final String  KEY_ACTION            = "action";

    // Fake site id (for repository)
    public static final String  TEMP_SITE             = "/service";

    // URIs parsed
    private static final String URI_BLOG              = SHORT_PROXY_URL + "api/blog/";
    private static final String URI_LINKS             = SHORT_PROXY_URL + "api/links/";
    private static final String URI_DOWNLOAD          = SHORT_PROXY_URL + "api/node/content/";
    private static final String URI_DOWNLOAD_2        = SHORT_PROXY_URL + "slingshot/node/content/";
    private static final String URI_CALENDAR          = "/calendar/create";

    // Check the method of the request
    private static final String URI_DISCUSSIONS       = SHORT_PROXY_URL + "api/forum/";
    private static final String URI_WIKI              = SHORT_PROXY_URL + "slingshot/wiki/page/";

    private static final String URI_DATALIST          = SHORT_PROXY_URL + "slingshot/datalists/item/";
    private static final String URI_DATALIST_DELETE   = SHORT_PROXY_URL + "slingshot/datalists/action/item";

    // Updated from form
    private static final String URI_NODE_UPDATE       = SHORT_PROXY_URL + "api/node/";
    private static final String FORMPROCESSOR         = "/formprocessor";

    // Repository and sites
    private static final String URI_ACTION            = SHORT_PROXY_URL + "slingshot/doclib/action/files";
    private static final String URI_UPLOAD            = SHORT_PROXY_URL + "api/upload";

    // Social features
    private static final String URI_SOCIAL_PUBLISHING = SHORT_PROXY_URL + "api/publishing/queue";

    private static final String GET                   = HttpMethod.GET.toString();
    private static final String POST                  = HttpMethod.POST.toString();
    private static final String PUT                   = HttpMethod.PUT.toString();
    private static final String DELETE                = HttpMethod.DELETE.toString();

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

        RequestWrapper requestWrapper = new RequestWrapper(request);
        // Initialize a new request context
        RequestContext context = ThreadLocalRequestContext.getRequestContext();

        String referer = request.getHeader("referer");

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
        String method = request.getMethod().toUpperCase();

        if (userId != null && userId.trim().length() > 0) {
            try {
                JSONObject auditSample = new JSONObject();
                auditSample.put(AUDIT_ID, "0");
                auditSample.put(AUDIT_USER_ID, userId);
                auditSample.put(AUDIT_SITE, "");
                auditSample.put(AUDIT_APP_NAME, "");
                auditSample.put(AUDIT_ACTION_NAME, "");
                auditSample.put(AUDIT_OBJECT, "");
                auditSample.put(AUDIT_TIME, Long.toString(System.currentTimeMillis()));

                // For documents only (only in sites!)
                if (requestURI.endsWith("/doclib/activity") && request.getMethod().equals(POST)) {
                    String type = request.getContentType().split(";")[0];
                    if ("application/json".equals(type)) {
                        // Get JSON Object
                        JSONObject activityFeed = new JSONObject(requestWrapper.getStringContent());

                        String activityType = activityFeed.getString("type");
                        if (activityType != null) {
                            if ("file-added".equals(activityType)
                                    || ("file-updated".equals(activityType) && (referer != null && !referer.contains("document-details"))) // Done in JavaScript on "document-details" page
                                    || "file-deleted".equals(activityType)) {
                                if (activityFeed.has("nodeRef")) {
                                    auditSample.put(AUDIT_APP_NAME, MOD_DOCUMENT);
                                    auditSample.put(AUDIT_SITE, activityFeed.getString("site"));
                                    auditSample.put(AUDIT_ACTION_NAME, activityType);
                                    auditSample.put(AUDIT_OBJECT, activityFeed.getString("nodeRef"));
                                    auditSample.put(AUDIT_APP_NAME, MOD_DOCUMENT);

                                    remoteCall(request, auditSample);
                                }
                            } else if ("files-added".equals(activityType) || "files-deleted".equals(activityType)) {
                                // multiple file uploads/deletions (5 or more)
                                Integer fileCount = activityFeed.getInt("fileCount");
                                if (fileCount != null && fileCount > 0) {
                                    auditSample.put(AUDIT_APP_NAME, MOD_DOCUMENT);
                                    auditSample.put(AUDIT_SITE, activityFeed.getString("site"));
                                    auditSample.put(AUDIT_ACTION_NAME, "file-" + activityType.split("-")[1]);
                                    auditSample.put(AUDIT_APP_NAME, MOD_DOCUMENT);

                                    for (int i = 0; i < fileCount; i++) {
                                        remoteCall(request, auditSample);
                                    }
                                }
                            }
                        }
                    }
                } else if (requestURI.startsWith(URI_NODE_UPDATE) && requestURI.endsWith(FORMPROCESSOR)) {
                    JSONObject updatedData = new JSONObject(requestWrapper.getStringContent());
                    // Online edit used the same form (plus the cm_content
                    // metadata)
                    if (!updatedData.has("prop_cm_content")) {
                        auditSample.put(AUDIT_APP_NAME, MOD_DOCUMENT);
                        auditSample.put(AUDIT_OBJECT, getNodeRefFromUrl(requestURI, 1));

                        auditSample.put(AUDIT_ACTION_NAME, "update");
                        auditSample.put(AUDIT_SITE, TEMP_SITE);
                        remoteCall(request, auditSample);
                    }
                } else if (requestURI.endsWith("/activity/create")) {
                    String jsonPost = requestWrapper.getStringContent();
                    if (jsonPost != null && !jsonPost.isEmpty()) {
                        JSONObject json = new JSONObject(jsonPost);
                        String mod = AuditHelper.extractModFromActivity(json);
                        if (mod != null) {
                            auditSample.put(AUDIT_APP_NAME, mod);
                            auditSample.put(AUDIT_SITE, json.getString("site"));
                            auditSample.put(AUDIT_ACTION_NAME, AuditHelper.extractActionFromActivity(json));
                            auditSample.put(AUDIT_OBJECT, json.getString("nodeRef"));
                            remoteCall(request, auditSample);
                        }
                    }
                } else if (requestURI.equals(URI_UPLOAD)) {
                    // XXX: issue with big files
                    // Nothing to do - Insert request is done in JavaScript
                } else if (referer != null && (requestURI.endsWith("/comments") || requestURI.endsWith("/replies"))) {
                    // Comments & replies
                    String[] urlTokens = referer.split("/");
                    HashMap<String, String> auditData = this.getUrlData(urlTokens);

                    auditSample.put(AUDIT_SITE, auditData.get(KEY_SITE));
                    auditSample.put(AUDIT_APP_NAME, auditData.get(KEY_MODULE));
                    auditSample.put(AUDIT_ACTION_NAME, "comments");
                    auditSample.put(AUDIT_OBJECT, getNodeRefFromUrl(requestURI, 1));

                    // Remote call for DB
                    remoteCall(request, auditSample);
                } else if (requestURI.startsWith(URI_WIKI)) {
                    String[] urlTokens = requestURI.split("/");
                    String wikiPageId = urlTokens[urlTokens.length - 1];
                    String siteId = urlTokens[urlTokens.length - 2];
                    if (method.equals(PUT)) {
                        JSONObject params = new JSONObject(requestWrapper.getStringContent());
                        auditSample.put(AUDIT_SITE, siteId);
                        auditSample.put(AUDIT_APP_NAME, MOD_WIKI);
                        if (params.has("currentVersion")) {
                            auditSample.put(AUDIT_ACTION_NAME, "update-post");
                        } else {
                            auditSample.put(AUDIT_ACTION_NAME, "create-post");
                        }

                        String auditObject = getNodeRefRemoteCall(request, userId, siteId, MOD_WIKI, wikiPageId);
                        auditSample.put(AUDIT_OBJECT, auditObject);

                        // Remote call
                        remoteCall(request, auditSample);
                    } else if (method.equals(DELETE)) {
                        auditSample.put(AUDIT_SITE, siteId);
                        auditSample.put(AUDIT_APP_NAME, MOD_WIKI);
                        auditSample.put(AUDIT_ACTION_NAME, "delete-post");
                        auditSample.put(AUDIT_OBJECT, wikiPageId);
                        // Remote call
                        remoteCall(request, auditSample);
                    }
                } else if (requestURI.startsWith(URI_BLOG)) {
                    auditSample.put(AUDIT_APP_NAME, MOD_BLOG);
                    if (method.equals(POST)) {
                        JSONObject params = new JSONObject(requestWrapper.getStringContent());
                        auditSample.put(AUDIT_SITE, params.get("site"));
                        auditSample.put(AUDIT_ACTION_NAME, "blog-create");
                        auditSample.put(AUDIT_OBJECT, params.get("title"));

                        remoteCall(request, auditSample);
                    } else if (method.equals(PUT)) {
                        JSONObject params = new JSONObject(requestWrapper.getStringContent());
                        auditSample.put(AUDIT_SITE, params.get("site"));
                        auditSample.put(AUDIT_ACTION_NAME, "blog-update");
                        auditSample.put(AUDIT_OBJECT, getNodeRefFromUrl(requestURI, 0));

                        remoteCall(request, auditSample);
                    } else if (method.equals(DELETE)) {
                        String[] urlTokens = requestURI.split("/");
                        auditSample.put(AUDIT_OBJECT, urlTokens[urlTokens.length - 1]);
                        auditSample.put(AUDIT_SITE, urlTokens[urlTokens.length - 3]);
                        auditSample.put(AUDIT_ACTION_NAME, "blog-delete");

                        remoteCall(request, auditSample);
                    }
                } else if (requestURI.startsWith(URI_DISCUSSIONS)) {
                    auditSample.put(AUDIT_APP_NAME, "discussions");
                    if (method.equals(POST)) {
                        JSONObject params = new JSONObject(requestWrapper.getStringContent());
                        auditSample.put(AUDIT_SITE, params.get("site"));
                        auditSample.put(AUDIT_ACTION_NAME, "discussions-create");
                        auditSample.put(AUDIT_OBJECT, params.get("title"));

                        remoteCall(request, auditSample);
                    } else if (method.equals(PUT)) {
                        JSONObject params = new JSONObject(requestWrapper.getStringContent());
                        String siteId = (String) params.get("site");
                        auditSample.put(AUDIT_SITE, siteId);
                        auditSample.put(AUDIT_ACTION_NAME, "discussions-update");

                        String[] urlTokens = requestURI.split("/");
                        String discussionId = urlTokens[urlTokens.length - 1];
                        String auditObject = getNodeRefRemoteCall(request, userId, siteId, "discussions", discussionId);
                        auditSample.put(AUDIT_OBJECT, auditObject);

                        remoteCall(request, auditSample);
                    } else if (method.equals(DELETE)) {
                        String[] urlTokens = requestURI.split("/");
                        auditSample.put(AUDIT_ACTION_NAME, "discussions-deleted");
                        auditSample.put(AUDIT_OBJECT, urlTokens[urlTokens.length - 1]);
                        auditSample.put(AUDIT_SITE, urlTokens[urlTokens.length - 3]);

                        remoteCall(request, auditSample);
                    }
                } else if (requestURI.startsWith(URI_LINKS) && !method.equals(GET)) {
                    String[] urlTokens = requestURI.split("/");
                    JSONObject params = new JSONObject(requestWrapper.getStringContent());
                    auditSample.put(AUDIT_APP_NAME, MOD_LINKS);

                    if (method.equals(POST)) {
                        if (requestURI.startsWith(URI_LINKS + "delete/")) {
                            auditSample.put(AUDIT_SITE, urlTokens[urlTokens.length - 2]);
                            auditSample.put(AUDIT_OBJECT, params.getJSONArray("items").get(0));
                            auditSample.put(AUDIT_ACTION_NAME, "links-delete");
                        } else {
                            auditSample.put(AUDIT_OBJECT, params.get("title"));
                            auditSample.put(AUDIT_SITE, urlTokens[urlTokens.length - 3]);
                            auditSample.put(AUDIT_ACTION_NAME, "links-create");
                        }
                        remoteCall(request, auditSample);
                    } else if (method.equals(PUT)) {
                        String siteId = urlTokens[urlTokens.length - 3];
                        auditSample.put(AUDIT_SITE, siteId);
                        auditSample.put(AUDIT_ACTION_NAME, "links-update");

                        String auditObject = getNodeRefRemoteCall(request, userId, siteId, MOD_LINKS, urlTokens[urlTokens.length - 1]);
                        auditSample.put(AUDIT_OBJECT, auditObject);

                        remoteCall(request, auditSample);
                    }
                } else if (requestURI.startsWith(URI_DOWNLOAD) || requestURI.startsWith(URI_DOWNLOAD_2)) {
                    String a = request.getParameter("a");
                    if (a != null && !a.isEmpty()) {
                        auditSample.put(AUDIT_APP_NAME, MOD_DOCUMENT);
                        auditSample.put(AUDIT_OBJECT, getNodeRefFromUrl(requestURI, 1));

                        auditSample.put(AUDIT_ACTION_NAME, "true".equalsIgnoreCase(a) ? "download" : "stream");
                        auditSample.put(AUDIT_SITE, TEMP_SITE);
                        remoteCall(request, auditSample);
                    }
                } else if (requestURI.startsWith(URI_ACTION)) {
                    // XXX: done in JavaScript
                } else if (requestURI.endsWith("memberships") && method.equals(GET)) {

                    String type = request.getParameter("authorityType");
                    String nf = request.getParameter("nf");
                    String[] urlTokens = requestURI.split("/");

                    auditSample.put(AUDIT_SITE, urlTokens[urlTokens.length - 2]);
                    auditSample.put(AUDIT_APP_NAME, MOD_MEMBERS);
                    auditSample.put(AUDIT_ACTION_NAME, type.toLowerCase());
                    auditSample.put(AUDIT_OBJECT, nf);
                    remoteCall(request, auditSample);
                } else if (requestURI.endsWith(URI_CALENDAR)) {
                    JSONObject params = new JSONObject(requestWrapper.getStringContent());
                    auditSample.put(AUDIT_APP_NAME, MOD_CALENDAR);
                    auditSample.put(AUDIT_ACTION_NAME, "create");
                    auditSample.put(AUDIT_SITE, params.get("site"));
                    auditSample.put(AUDIT_OBJECT, params.get("what"));

                    remoteCall(request, auditSample);
                } else if ((requestURI.startsWith(URI_DATALIST) || requestURI.startsWith(URI_DATALIST_DELETE)) && method.equals(POST)) {
                    boolean isDeleteRequest = request.getParameter("alf_method") != null;
                    auditSample.put(AUDIT_APP_NAME, MOD_DATA);
                    auditSample.put(AUDIT_SITE, TEMP_SITE);
                    if (isDeleteRequest) {
                        auditSample.put(AUDIT_ACTION_NAME, "datalist-delete");
                        JSONObject params = new JSONObject(requestWrapper.getStringContent());
                        JSONArray items = params.getJSONArray("nodeRefs");
                        for (int i = 0; i < items.length(); i++) {
                            auditSample.put(AUDIT_OBJECT, items.getString(i));

                            remoteCall(request, auditSample);
                        }
                    } else {
                        auditSample.put(AUDIT_ACTION_NAME, "datalist-post");
                        auditSample.put(AUDIT_OBJECT, getNodeRefFromUrl(requestURI, 0));

                        remoteCall(request, auditSample);
                    }
                } else if (requestURI.startsWith(URI_SOCIAL_PUBLISHING) && method.equals(POST)) {
                    auditSample.put(AUDIT_APP_NAME, MOD_DOCUMENT);
                    auditSample.put(AUDIT_SITE, TEMP_SITE);

                    auditSample.put(AUDIT_ACTION_NAME, "publish");
                    JSONObject params = new JSONObject(requestWrapper.getStringContent());
                    JSONArray items = params.getJSONArray("publishNodes");
                    for (int i = 0; i < items.length(); i++) {
                        auditSample.put(AUDIT_OBJECT, items.getString(i));
                        remoteCall(request, auditSample);
                    }
                } else if (requestURI.indexOf("/ratings") != -1) {
                    auditSample.put(AUDIT_APP_NAME, MOD_DOCUMENT);
                    auditSample.put(AUDIT_SITE, TEMP_SITE);
                    int offset = 1;
                    if (POST.equals(method)) {
                        auditSample.put(AUDIT_ACTION_NAME, "rate");
                    } else if (DELETE.equals(method)) {
                        auditSample.put(AUDIT_ACTION_NAME, "unrate");
                        offset = 2;
                    }
                    auditSample.put(AUDIT_OBJECT, getNodeRefFromUrl(requestURI, offset));
                    remoteCall(request, auditSample);
                }

            } catch (JSONException e) {
                logger.error("JSON Error during a remote call ...");
                if (logger.isDebugEnabled()) {
                    logger.debug(e.getMessage(), e);
                }
            }
        }
        chain.doFilter(requestWrapper, sRes);
    }

    private void remoteCall(HttpServletRequest request, JSONObject auditSample) throws JSONException, URIException,
            UnsupportedEncodingException {
        Connector connector;
        try {
            connector = FrameworkUtil.getConnector(request.getSession(true), auditSample.getString(AUDIT_USER_ID),
                    AlfrescoUserFactory.ALFRESCO_ENDPOINT_ID);

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
     * 
     * @param url
     * @param offset
     * @return nodeRef
     */
    public String getNodeRefFromUrl(String url, int offset) {
        String[] urlTokens = url.split("/");
        return urlTokens[urlTokens.length - offset - 3] + "://" + urlTokens[urlTokens.length - offset - 2] + "/"
                + urlTokens[urlTokens.length - offset - 1];
    }

    /**
     * 
     * @param urlTokens
     * @return
     */
    public HashMap<String, String> getUrlData(String[] urlTokens) {
        HashMap<String, String> urlData = new HashMap<String, String>();
        urlData.put(KEY_MODULE, "");
        urlData.put(KEY_ACTION, "");
        urlData.put(KEY_SITE, "");

        boolean siteFlag = false;
        for (int i = 0; i < urlTokens.length; i++) {
            if (urlTokens[i].equals(KEY_SITE) && !siteFlag) {
                siteFlag = true;
            } else if (siteFlag && ("".equals(urlData.get(KEY_SITE)))) {
                urlData.put(KEY_SITE, urlTokens[i]);
                String[] splittedModuleAction = urlTokens[i + 1].split("-");
                urlData.put(KEY_MODULE, splittedModuleAction[0]);
                if (splittedModuleAction.length > 1) {
                    urlData.put(KEY_ACTION, splittedModuleAction[1]);
                } else if (splittedModuleAction.length == 1) {
                    urlData.put(KEY_ACTION, "");
                }
            }
        }
        return urlData;
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
