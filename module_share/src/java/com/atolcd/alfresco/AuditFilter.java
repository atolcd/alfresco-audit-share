package com.atolcd.alfresco;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

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
import org.springframework.extensions.surf.support.AlfrescoUserFactory;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.ConnectorContext;
import org.springframework.extensions.webscripts.connector.HttpMethod;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.webscripts.connector.User;
import org.springframework.web.context.support.WebApplicationContextUtils;

@SuppressWarnings("deprecation")
public class AuditFilter implements Filter {
    private ServletContext servletContext;
    // Store the "object" parameters to get for each module
    private HashMap<String, String> moduleIds;
    private HashMap<String, String[]> ignoredCases;
    private static final Log logger = LogFactory.getLog(AuditFilter.class);

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig args) throws ServletException {
        this.servletContext = args.getServletContext();
        // "Tableau" module -> paramètres
        // Utilisé pour lire les paramètres d'audit
        this.moduleIds = new HashMap<String, String>();
        this.moduleIds.put("wiki", "title");
        this.moduleIds.put("blog", "postId");
        this.moduleIds.put("document", "nodeRef");
        this.moduleIds.put("documentlibrary", "filter");
        this.moduleIds.put("calendar", "view");
        this.moduleIds.put("links", "linkId");
        this.moduleIds.put("discussions", "topicId");
        this.moduleIds.put("data", "list");
        this.moduleIds.put("members", "");
        this.moduleIds.put("dashboard", "");
        this.moduleIds.put("search", "t");

        this.ignoredCases = new HashMap<String, String[]>();
        ignoredCases.put("wiki", new String[] { "create" });
        ignoredCases.put("blog", new String[] { "postedit" });
        ignoredCases.put("links", new String[] { "linkedit" });
        ignoredCases.put("discussions", new String[] { "createtopic" });
        ignoredCases.put("calendar", new String[] { "month", "week", "day" });
    }

    private ApplicationContext getApplicationContext() {
        return WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
    }

    @Override
    public void doFilter(ServletRequest sReq, ServletResponse sRes, FilterChain chain) throws IOException, ServletException {

        // Get the HTTP request/response/session
        HttpServletRequest request = (HttpServletRequest) sReq;
        // HttpServletResponse response = (HttpServletResponse) sRes;

        // initialize a new request context
        RequestContext context = ThreadLocalRequestContext.getRequestContext();

        if (context == null) {
            try {
                // perform a "silent" init - i.e. no user creation or remote
                // connections
                context = RequestContextUtil.initRequestContext(getApplicationContext(), request, true);
                try {
                    RequestContextUtil.populateRequestContext(context, request);
                } catch (ResourceLoaderException e) {
                    // e.printStackTrace();
                } catch (UserFactoryException e) {
                    // e.printStackTrace();
                }
            } catch (RequestContextException ex) {
                throw new ServletException(ex);
            }
        }
        User user = context.getUser();
        String requestURI = request.getRequestURI();
        if (user != null && requestURI != null) {
            try {
                // Préparation du JSON à envoyer.
                JSONObject auditSample = new JSONObject();
                auditSample.put("id", "0");
                auditSample.put("auditUserId", user.getId());

                // Audit de la console ??
                if (requestURI.startsWith("/share/page/console/")) {

                } else {
                    String ref = request.getHeader("referer");
                    if (requestURI.endsWith("/dologin") && (ref != null)) {
                        requestURI = ref;
                    }
                    HashMap<String, String> auditData = getAuditData(request, user.getId(), requestURI);
                    // Le parsing inclue parfois les paramètres lorsque le
                    // chargement de la page est interrompu prématurément
                    if ((auditData.get("module").length() > 0)
                            && (auditData.get("site").length() > 0 && !auditData.get("action").contains("?"))) {
                        auditSample.put("auditSite", auditData.get("site"));
                        auditSample.put("auditAppName", auditData.get("module"));
                        auditSample.put("auditActionName", auditData.get("action"));
                        auditSample.put("auditObject", auditData.get("object"));
                        auditSample.put("auditTime", Long.toString(System.currentTimeMillis()));

                        // Remote call for DB
                        if (moduleIds.containsKey(auditSample.get("auditAppName"))) {
                            remoteCall(request, auditSample);
                        } else {
                            logger.info("Ignored : " + requestURI);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(" Error while auditing data in AuditFilter");
                e.printStackTrace();
            }
        }
        chain.doFilter(sReq, sRes);
    }

    private void remoteCall(HttpServletRequest request, JSONObject auditSample) throws JSONException, URIException,
            UnsupportedEncodingException {
        Connector connector;
        try {
            connector = FrameworkUtil.getConnector(request.getSession(true), auditSample.getString("auditUserId"),
                    AlfrescoUserFactory.ALFRESCO_ENDPOINT_ID);

            // parameters = null, on passe par le inputstream.
            // Le webscript est appelé avec l'audit converti en JSON.
            ConnectorContext postContext = new ConnectorContext(null, buildDefaultHeaders());
            postContext.setMethod(HttpMethod.POST);
            postContext.setContentType("text/plain;charset=UTF-8");
            InputStream in = new ByteArrayInputStream(auditSample.toString().getBytes("UTF-8"));

            // Appel au webscript - Response resp =
            connector.call("/share-stats/insert-audit", postContext, in);
        } catch (ConnectorServiceException e) {
            e.printStackTrace();
        }
    }

    private String getNodeRefRemoteCall(HttpServletRequest request, String userId, String siteId, String componentId, String objectId)
            throws JSONException, URIException, UnsupportedEncodingException {
        Connector connector;
        try {
            connector = FrameworkUtil.getConnector(request.getSession(true), userId, AlfrescoUserFactory.ALFRESCO_ENDPOINT_ID);
            // <url>/share-stats/slingshot/details/{siteId}/{componentId}/{objectId}</url>
            Response resp = connector.call("/share-stats/slingshot/details/" + siteId + "/" + componentId + "/" + URLEncoder.encode(objectId, "UTF-8"));

            if (resp.getStatus().getCode() == Status.STATUS_OK) {
                try {
                    JSONObject json = new JSONObject(resp.getResponse());
                    if (json.has("nodeRef")) {
                        return (String) json.get("nodeRef");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (ConnectorServiceException e) {
            e.printStackTrace();
        }

        return objectId;
    }

    /**
     * Découpe l'url, analyse les morceaux, puis analyse les paramètres
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

        HashMap<String, String> urlData = getUrlData(urlTokens);
        auditData.putAll(urlData);

        try {
            // On récupère l'identifiant de l'<<objet>> consulté à partir de son
            // module
            // En cas de null, on catch et on met une chaîne vide.
            String obj = request.getParameter(this.moduleIds.get(urlData.get("module")));
            if (obj != null) {
                // On déplace le paramètre dans l'action pour faciliter les
                // requêtes
                if (auditData.get("module").equals("calendar")) {
                    auditData.put("action", obj);
                    auditData.put("object", "");
                } else if (auditData.get("module").equals("links") && auditData.get("action").equals("view")) {
                    String auditObject = getNodeRefRemoteCall(request, userId, auditData.get("site"), auditData.get("module"), obj);
                    auditData.put("object", auditObject);
                    auditData.put("action", "single-view");
                } else if (auditData.get("module").equals("search")) {
                    if (!obj.isEmpty()) {
                        auditData.put("action", "query");
                        auditData.put("object", obj);
                    } else {
                        auditData.put("object", "");
                    }

                } else {
                    String auditObject = getNodeRefRemoteCall(request, userId, auditData.get("site"), auditData.get("module"), obj);
                    auditData.put("object", auditObject);
                }
            } else {
                auditData.put("object", "");
            }
        } catch (Exception e) {
            auditData.put("object", "");
        }

        return filter(auditData);
    }

    /**
     * Parse l'url découpage afin d'en tirer les informations d'audit
     * 
     * @param urlTokens
     *            String[]
     * @return HashMap
     */
    public HashMap<String, String> getUrlData(String[] urlTokens) {
        HashMap<String, String> urlData = new HashMap<String, String>();
        urlData.put("module", "");
        urlData.put("action", "");
        urlData.put("site", "");

        boolean siteFlag = false;
        for (int i = 0; i < urlTokens.length; i++) {
            if (urlTokens[i].equals("site") && !siteFlag) {
                siteFlag = true;
            }
            // On trouve le token "site" dans l'url, le prochain token est
            // le nom du site
            else if (siteFlag && (urlData.get("site").equals(""))) {
                urlData.put("site", urlTokens[i]);
                String[] splittedModuleAction = urlTokens[i + 1].split("-");
                // test pour site-members & site-groups
                if (splittedModuleAction[0].equals("site")) {
                    urlData.put("module", "members");
                } else {
                    urlData.put("module", splittedModuleAction[0]);
                }

                // Test d'action (module-action; wiki-create par exemple)
                if (splittedModuleAction.length > 1) {
                    urlData.put("action", splittedModuleAction[1]);
                } else if (splittedModuleAction.length == 1) {
                    if (urlData.get("module").endsWith("library")) {
                        urlData.put("module", "document");
                        urlData.put("action", "library");
                    } else if (urlData.get("module").endsWith("search")) {
                        urlData.put("action", splittedModuleAction[0]);
                        urlData.put("module", "search");
                    } else {
                        // On suppose que c'est une consultation
                        urlData.put("action", "view");
                    }
                }
            }
        }
        return urlData;
    }

    public HashMap<String, String> filter(HashMap<String, String> auditData) {
        String module = auditData.get("module");
        String action = auditData.get("action");

        if (module != null && action != null) {
            if (ignoredCases.containsKey(module)) {
                if (contains(ignoredCases.get(module), action)) {
                    // Ne sera pas audité si ne contient pas de site
                    auditData.put("site", "");
                }
            }
        }
        return auditData;
    }

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
