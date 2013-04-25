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
public class AuditFilter extends AuditFilterConstants implements Filter {
    public static final String KEY_SITE = "site";
    public static final String KEY_MODULE = "module";
    public static final String KEY_OBJECT = "object";
    public static final String KEY_ACTION = "action";

    private ServletContext servletContext;
    // Store the "object" parameters to get for each module
    private static HashMap<String, String> moduleIds;
    private static HashMap<String, String[]> ignoredCases;
    private static Set<String> ignoredUrl;
    private static final Log logger = LogFactory.getLog(AuditFilter.class);

    // Identifiant utilisé en base pour identifier un audit sur le repo et non
    // sur un site.
    private static final String REPOSITORY_SITE = "/service";

    static {
        // Urls exactes devant être filtrées
        ignoredUrl = new HashSet<String>();
        ignoredUrl.add("/share/page");
        ignoredUrl.add("/share/page/");
        ignoredUrl.add("/share/page/console");
        ignoredUrl.add("/share/page/console/");

        // Cas ignorés des sites.
        ignoredCases = new HashMap<String, String[]>();
        ignoredCases.put("wiki", new String[] { "create" });
        ignoredCases.put("blog", new String[] { "postedit" });
        ignoredCases.put("links", new String[] { "linkedit" });
        ignoredCases.put("discussions", new String[] { "createtopic" });
        ignoredCases.put("calendar", new String[] { "month", "week", "day" });

        // "Tableau" module -> paramètres
        // Utilisé pour lire les paramètres d'audit
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
        if (user != null && requestURI != null && !ignoredUrl.contains(requestURI)) {
            try {
                // Préparation du JSON à envoyer.
                JSONObject auditSample = new JSONObject();
                auditSample.put(AUDIT_ID, "0");
                auditSample.put(AUDIT_USER_ID, user.getId());

                String ref = request.getHeader("referer");
                if (requestURI.endsWith("/dologin") && (ref != null)) {
                    requestURI = ref;
                }
                HashMap<String, String> auditData = getAuditData(request, user.getId(), requestURI);
                // Le parsing inclue parfois les paramètres lorsque le
                // chargement de la page est interrompu prématurément
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
                        logger.info("Ignored : " + requestURI);
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
            connector = FrameworkUtil.getConnector(request.getSession(true), auditSample.getString(AUDIT_USER_ID),
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

        HashMap<String, String> urlData = getUrlData(urlTokens, requestURL.indexOf("site") != -1);

        auditData.putAll(urlData);

        try {
            // On récupère l'identifiant de l'<<objet>> consulté à partir de son
            // module
            // En cas de null, on catch et on met une chaîne vide.
            String obj = request.getParameter(moduleIds.get(urlData.get(KEY_MODULE)));
            if (obj != null) {
                // On déplace le paramètre dans l'action pour faciliter les
                // requêtes
                if (auditData.get(KEY_MODULE).equals("calendar")) {
                    auditData.put(KEY_ACTION, obj);
                    auditData.put(KEY_OBJECT, "");
                } else if (auditData.get(KEY_MODULE).equals(MOD_LINKS) && auditData.get(KEY_ACTION).equals("view")) {
                    String auditObject = getNodeRefRemoteCall(request, userId, auditData.get(KEY_SITE), auditData.get(KEY_MODULE), obj);
                    auditData.put(KEY_OBJECT, auditObject);
                    auditData.put(KEY_ACTION, "single-view");
                } else if (auditData.get("module").equals("search")) {
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
        }

        return filter(auditData);
    }

    /**
     * Parse l'url découpage afin d'en tirer les informations d'audit
     * 
     * @param urlTokens
     * @param hasSite
     * @return HashMap
     */
    public HashMap<String, String> getUrlData(String[] urlTokens, boolean hasSite) {
        HashMap<String, String> urlData = new HashMap<String, String>();
        urlData.put(KEY_MODULE, "");
        urlData.put(KEY_ACTION, "");
        urlData.put(KEY_SITE, "");

        // Voir les effets de bord au retrait de traitement des url sans sites
        if (!hasSite) {
            // On simule la présence d'un site en modifiant les morceaux d'url
            String[] newUrlTokens = new String[urlTokens.length + 2];
            int j = 0;
            String token;
            for (int i = 0; i < urlTokens.length; i++) {
                token = urlTokens[i];
                newUrlTokens[i + j] = token;
                // On simule la présence d'un site.
                if (urlTokens[i].equals("page")) {
                    newUrlTokens[i + 1] = "site";
                    newUrlTokens[i + 2] = REPOSITORY_SITE;
                    j = 2;
                }
            }
            // On recopie les nouveaux tokens
            urlTokens = newUrlTokens;
        }

        boolean siteFlag = false;
        for (int i = 0; i < urlTokens.length; i++) {
            if ("site".equals(urlTokens[i]) && !siteFlag) {
                siteFlag = true;
            }
            // On trouve le token "site" dans l'url, le prochain token est
            // le nom du site
            else if (siteFlag && (urlData.get(KEY_SITE).isEmpty()) && i < urlTokens.length) {
                urlData.put(KEY_SITE, urlTokens[i]);
                String[] splittedModuleAction = urlTokens[i + 1].split("-");
                // test pour site-members & site-groups
                if (splittedModuleAction[0].equals("site")) {
                    urlData.put(KEY_MODULE, MOD_MEMBERS);
                } else {
                    urlData.put(KEY_MODULE, splittedModuleAction[0]);
                }

                // Test d'action (module-action; wiki-create par exemple)
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
                        // On suppose que c'est une consultation
                        urlData.put(KEY_ACTION, "view");
                    }
                }
            }
        }

        return urlData;
    }

    /**
     * Filtre les données d'audit.
     * 
     * @param auditData
     * @return
     */
    public HashMap<String, String> filter(HashMap<String, String> auditData) {
        String module = auditData.get(KEY_MODULE);
        String action = auditData.get(KEY_ACTION);

        if (module != null && action != null) {
            if (ignoredCases.containsKey(module)) {
                if (contains(ignoredCases.get(module), action)) {
                    // Ne sera pas audité si ne contient pas de site
                    auditData.put(KEY_SITE, "");
                }
            }
        }
        return auditData;
    }

    /**
     * Indique si une String est contenu dans un tableau de String
     * 
     * @param array
     * @param toFind
     * @return
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
