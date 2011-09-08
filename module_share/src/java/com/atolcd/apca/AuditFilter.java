package com.atolcd.apca;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.ConnectorContext;
import org.springframework.extensions.webscripts.connector.HttpMethod;
import org.springframework.extensions.webscripts.connector.User;
import org.springframework.web.context.support.WebApplicationContextUtils;

@SuppressWarnings("deprecation")
public class AuditFilter implements Filter {
	private ServletContext servletContext;
	// Store the "object" parameters to get for each module
	private HashMap<String, String> moduleIds;

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
		this.moduleIds.put("site", "");// Members tab
	}

	private ApplicationContext getApplicationContext() {
		return WebApplicationContextUtils
				.getRequiredWebApplicationContext(servletContext);
	}

	@Override
	public void doFilter(ServletRequest sReq, ServletResponse sRes,
			FilterChain chain) throws IOException, ServletException {

		// Get the HTTP request/response/session
		HttpServletRequest request = (HttpServletRequest) sReq;
		// HttpServletResponse response = (HttpServletResponse) sRes;

		// initialize a new request context
		RequestContext context = ThreadLocalRequestContext.getRequestContext();

		if (context == null) {
			try {
				// perform a "silent" init - i.e. no user creation or remote
				// connections
				context = RequestContextUtil.initRequestContext(
						getApplicationContext(), request, true);
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
				// Pr�paration du JSON à envoyer.
				JSONObject auditSample = new JSONObject();
				auditSample.put("id", "0");
				auditSample.put("auditUserId", user.getId());

				// Audit de la console ??
				if (requestURI.startsWith("/share/page/console/")) {
					/*
					 * String[] urlTokens = requestURI.split("/");
					 * auditSample.put("auditSite", "");
					 * auditSample.put("auditAppName", "console");
					 * auditSample.put
					 * ("auditActionName",urlTokens[urlTokens.length-1]);
					 * auditSample.put("auditObject", "");
					 * auditSample.put("auditTime",
					 * Long.toString(System.currentTimeMillis()));
					 *
					 * //Remote call for DB remoteCall(request,auditSample);
					 */
				} else {
					String ref = request.getHeader("referer");
					if (requestURI.endsWith("/dologin") && (ref != null)) {
						requestURI = ref;
					}
					HashMap<String, String> auditData = getAuditData(request,
							requestURI);
					// TODO Check for other actions to audit if null
					if ((auditData.get("module").length() > 0)
							&& (auditData.get("site").length() > 0)) {
						auditSample.put("auditSite", auditData.get("site"));
						auditSample
								.put("auditAppName", auditData.get("module"));
						auditSample.put("auditActionName",
								auditData.get("action"));
						auditSample.put("auditObject", auditData.get("object"));
						auditSample.put("auditTime",
								Long.toString(System.currentTimeMillis()));

						// Remote call for DB
						remoteCall(request, auditSample);
					}
				}
			} catch (Exception e) {
				System.out.println(" Error while auditing data in AuditFilter");
			}
		}
		chain.doFilter(sReq, sRes);
	}

	private void remoteCall(HttpServletRequest request, JSONObject auditSample)
			throws JSONException, URIException, UnsupportedEncodingException {
		Connector connector;
		try {
			connector = FrameworkUtil.getConnector(request.getSession(true),
					auditSample.getString("auditUserId"),
					AlfrescoUserFactory.ALFRESCO_ENDPOINT_ID);

			// En get
			// String query ="/db/connect?entry=" + entry.toJSON();
			// Response resp = connector.call(URIUtil.encodeQuery(query));

			// parameters = null, on passe par le inputstream.
			// Le webscript est appelé avec l'audit converti en JSON.
			ConnectorContext postContext = new ConnectorContext(null,
					buildDefaultHeaders());
			postContext.setMethod(HttpMethod.POST);
			postContext.setContentType("text/plain;charset=UTF-8");
			InputStream in = new ByteArrayInputStream(auditSample.toString()
					.getBytes("UTF-8"));

			// Appel au webscript - Response resp =
			connector.call("/share-stats/insert-audit", postContext, in);

			/*
			 * System.out.println("Response : " + resp.toString()); if
			 * (resp.getStatus().getCode() == Status.STATUS_OK) { try {
			 * JSONObject json = new JSONObject(resp.getResponse()); if
			 * (json.has("firstName")) {
			 * System.out.println(json.get("firstName")); } } catch
			 * (JSONException e) { e.printStackTrace(); } }
			 */
		} catch (ConnectorServiceException e) {
			e.printStackTrace();
		}
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
	public HashMap<String, String> getAuditData(HttpServletRequest request,
			String requestURL) {
		HashMap<String, String> auditData = new HashMap<String, String>();
		String[] urlTokens = requestURL.split("/");

		HashMap<String, String> urlData = getUrlData(urlTokens);
		auditData.putAll(urlData);

		try {
			// On récupère l'identifiant de l'<<objet>> consulté à partir de
			// son module
			// En cas de null, on catch et on met une chaîne vide.
			String obj = request.getParameter(this.moduleIds.get(urlData
					.get("module")));
			if (obj != null) {
				// On déplace le paramètre dans l'action pour faciliter les
				// requêtes
				if (auditData.get("module").equals("calendar")) {
					auditData.put("action", obj);
					auditData.put("object", "");
				} else {
					auditData.put("object", obj);
				}
			} else {
				auditData.put("object", "");
			}
		} catch (Exception e) {
			auditData.put("object", "");
		}
		return auditData;
	}

	/**
	 * Parse l'url découpée afin d'en tirer les informations d'audit
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
					} else {
						// On suppose que c'est une consultation
						urlData.put("action", "view");
					}
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
		headers.put("Accept-Language",
				I18NUtil.getLocale().toString().replace('_', '-'));
		return headers;
	}
}
