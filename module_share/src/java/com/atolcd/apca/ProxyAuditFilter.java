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
public class ProxyAuditFilter implements Filter {
	private ServletContext servletContext;

	private static final String URI_BLOG = "/share/proxy/alfresco/api/blog/";

	private static final String URI_LINKS = "/share/proxy/alfresco/api/links/";

	// Check Methode de la requête
	private static final String URI_DISCUSSIONS = "/share/proxy/alfresco/api/forum/";
	private static final String URI_WIKI = "/share/proxy/alfresco/slingshot/wiki/page/";

	@Override
	public void destroy() {
	}

	@Override
	public void init(FilterConfig args) throws ServletException {
		this.servletContext = args.getServletContext();
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
		RequestWrapper requestWrapper = new RequestWrapper(request);

		// System.out.println("Requested : " +
		// request.getRequestURL().toString());
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
		String requestURI = request.getRequestURI();// .getRequestURL().toString();
		String method = request.getMethod().toUpperCase();
		// Tester la méthode get ?
		if (user != null) {
			try {
				// Création de l'auditSample
				JSONObject auditSample = new JSONObject();
				auditSample.put("id", "0");
				auditSample.put("auditUserId", user.getId());
				auditSample.put("auditSite", "");
				auditSample.put("auditAppName", "");
				auditSample.put("auditActionName", "");
				auditSample.put("auditObject", "");
				auditSample.put("auditTime",
						Long.toString(System.currentTimeMillis()));

				// Ajout de documents - Possibilité de filtrer les suppressions
				// (method : delete)
				// Ne se déclenche pas pour les replies/comments ?! Que pour les
				// docs ?!
				if (requestURI.endsWith("/activity")
						&& request.getMethod().toLowerCase().equals("post")) {
					String type = request.getContentType().split(";")[0];
					if (type.equals("application/json")) {
						// Get JSON Object
						JSONObject activityFeed = new JSONObject(
								requestWrapper.getStringContent());
						// Mise à jour de l'auditSample à insérer
						if (activityFeed.has("nodeRef")) {
							auditSample.put("auditAppName",
									activityFeed.getString("page"));
							auditSample.put("auditSite",
									activityFeed.getString("site"));
							auditSample.put("auditActionName",
									activityFeed.getString("type"));
							auditSample.put("auditObject",
									activityFeed.getString("nodeRef")); // Ou
																		// fileName
																		// ?
						} else if (activityFeed.has("fileCount")) {
							// Plusieurs docs d'un coup. On ne récupère pas les
							// nodeRefs.
							auditSample.put("auditAppName",
									activityFeed.getString("page"));
							auditSample.put("auditSite",
									activityFeed.getString("site"));
							auditSample.put("auditActionName",
									activityFeed.getString("type"));

							int fileCount = activityFeed.getInt("fileCount");
							for (int i = 0; i < fileCount; i++) {
								remoteCall(request, auditSample);
							}
						}

						// Uniformisation des noms
						if (auditSample.get("auditAppName").equals(
								"documentlibrary")) {
							auditSample.put("auditAppName", "document");
						}

						if (auditSample.get("auditActionName").toString()
								.endsWith("added")) {
							auditSample.put("auditActionName", "file-added");
						} else if (auditSample.get("auditActionName")
								.toString().endsWith("deleted")) {
							auditSample.put("auditActionName", "file-deleted");
						}
						// Remote call for DB
						remoteCall(request, auditSample);
					}
				} else if (requestURI.endsWith("/comments")
						|| requestURI.endsWith("/replies")) {
					// Comments & replies
					String[] urlTokens = request.getHeader("referer")
							.toString().split("/");
					HashMap<String, String> auditData = this
							.getUrlData(urlTokens);

					auditSample.put("auditSite", auditData.get("site"));
					auditSample.put("auditAppName", auditData.get("module"));
					auditSample.put("auditActionName", "comments");
					auditSample.put("auditObject",
							getNodeRefFromUrl(requestURI));
					// Ancienne méthode pour récupérer le postId - NodeRef
					// Maintenant
					// auditSample.put("auditObject",
					// getUrlParameters(urlTokens[urlTokens.length-1]).get("postId"));

					// Remote call for DB
					remoteCall(request, auditSample);
				} else if (requestURI.startsWith(URI_WIKI)) {
					String[] urlTokens = requestURI.split("/");
					if (method.equals("PUT")) {
						// TODO : Créer des snippets pour la récupération du
						// site selon JSON/urlTokens??
						JSONObject params = new JSONObject(
								requestWrapper.getStringContent());
						auditSample.put("auditSite",
								urlTokens[urlTokens.length - 2]);
						auditSample.put("auditAppName", "wiki");
						if (params.has("currentversion")) {
							auditSample.put("auditActionName", "update-post");
						} else {
							auditSample.put("auditActionName", "create-post");
						}
						auditSample.put("auditObject",
								urlTokens[urlTokens.length - 1]);
						// Remote call
						remoteCall(request, auditSample);
					} else if (method.equals("DELETE")) {
						auditSample.put("auditSite",
								urlTokens[urlTokens.length - 2]);
						auditSample.put("auditAppName", "wiki");
						auditSample.put("auditActionName", "delete-post");
						auditSample.put("auditObject",
								urlTokens[urlTokens.length - 1]);
						// Remote call
						remoteCall(request, auditSample);
					}
				} else if (requestURI.startsWith(URI_BLOG)) {
					auditSample.put("auditAppName", "blog");
					if (method.equals("POST")) {
						JSONObject params = new JSONObject(
								requestWrapper.getStringContent());
						auditSample.put("auditSite", params.get("site"));
						auditSample.put("auditActionName", "blog-create");
						auditSample.put("auditObject", params.get("title"));

						remoteCall(request, auditSample);
					} else if (method.equals("PUT")) {
						JSONObject params = new JSONObject(
								requestWrapper.getStringContent());
						auditSample.put("auditSite", params.get("site"));
						auditSample.put("auditActionName", "blog-update");
						auditSample.put("auditObject", params.get("title"));

						remoteCall(request, auditSample);
					} else if (method.equals("DELETE")) {
						String[] urlTokens = requestURI.split("/");
						auditSample.put("auditObject",
								urlTokens[urlTokens.length - 1]);
						auditSample.put("auditSite",
								urlTokens[urlTokens.length - 3]);
						auditSample.put("auditActionName", "blog-delete");

						remoteCall(request, auditSample);
					}
				} else if (requestURI.startsWith(URI_DISCUSSIONS)) {
					auditSample.put("auditAppName", "discussions");
					if (method.equals("POST")) {
						JSONObject params = new JSONObject(
								requestWrapper.getStringContent());
						auditSample.put("auditSite", params.get("site"));
						auditSample
								.put("auditActionName", "discussions-create");
						auditSample.put("auditObject", params.get("title"));

						remoteCall(request, auditSample);
					} else if (method.equals("PUT")) {
						JSONObject params = new JSONObject(
								requestWrapper.getStringContent());
						auditSample.put("auditSite", params.get("site"));
						auditSample
								.put("auditActionName", "discussions-update");
						auditSample.put("auditObject", params.get("title"));

						remoteCall(request, auditSample);
					} else if (method.equals("DELETE")) {
						String[] urlTokens = requestURI.split("/");
						auditSample.put("auditActionName",
								"discussions-deleted");
						auditSample.put("auditObject",
								urlTokens[urlTokens.length - 1]);
						auditSample.put("auditSite",
								urlTokens[urlTokens.length - 3]);

						remoteCall(request, auditSample);
					}
				} else if (requestURI.startsWith(URI_LINKS)
						&& !method.equals("GET")) {
					String[] urlTokens = requestURI.split("/");
					JSONObject params = new JSONObject(
							requestWrapper.getStringContent());
					auditSample.put("auditAppName", "links");

					if (method.equals("POST")) {
						if (requestURI.startsWith(URI_LINKS + "delete/")) {
							auditSample.put("auditSite",
									urlTokens[urlTokens.length - 2]);
							auditSample.put("auditObject",
									params.getJSONArray("items").get(0));
							auditSample.put("auditActionName", "links-delete");
						} else {
							auditSample.put("auditObject", params.get("title"));
							auditSample.put("auditSite",
									urlTokens[urlTokens.length - 3]);
							auditSample.put("auditActionName", "links-create");
						}
						remoteCall(request, auditSample);
					} else if (method.equals("PUT")) {
						auditSample.put("auditSite",
								urlTokens[urlTokens.length - 3]);
						auditSample.put("auditObject", params.get("title"));
						auditSample.put("auditActionName", "links-update");

						remoteCall(request, auditSample);
					}
				}
			} catch (JSONException e) {
				System.out.println("JSON Error during a remote call ...");
				e.printStackTrace();
			}
		}
		chain.doFilter(requestWrapper, sRes);
	}

	private void remoteCall(HttpServletRequest request, JSONObject auditSample)
			throws JSONException, URIException, UnsupportedEncodingException {
		Connector connector;
		try {
			connector = FrameworkUtil.getConnector(request.getSession(true),
					auditSample.getString("auditUserId"),
					AlfrescoUserFactory.ALFRESCO_ENDPOINT_ID);

			// Le webscript est appelé avec l'audit converti en JSON.
			ConnectorContext postContext = new ConnectorContext(null,
					buildDefaultHeaders());
			postContext.setMethod(HttpMethod.POST);
			postContext.setContentType("text/plain;charset=UTF-8");
			InputStream in = new ByteArrayInputStream(auditSample.toString()
					.getBytes("UTF-8"));

			// Appel au webscript
			connector.call("/share-stats/insert-audit", postContext, in);

			// System.out.println("Response : " + resp.toString());

		} catch (ConnectorServiceException e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param url
	 * @return nodeRef
	 */
	public String getNodeRefFromUrl(String url) {
		String nodeRef = "";
		String[] urlTokens = url.split("/");
		nodeRef = urlTokens[urlTokens.length - 4] + "://"
				+ urlTokens[urlTokens.length - 3] + "/"
				+ urlTokens[urlTokens.length - 2];

		return nodeRef;
	}

	/**
	 * @deprecated
	 * @param url
	 * @return
	 */
	public HashMap<String, String> getUrlParameters(String queryString) {
		HashMap<String, String> urlParameters = new HashMap<String, String>();
		queryString = queryString.substring(queryString.indexOf('?') + 1);
		String[] urlTokens = queryString.split("&");
		for (String token : urlTokens) {
			String[] parameter = token.split("=");
			if (parameter.length > 1) {
				urlParameters.put(parameter[0], parameter[1]);
			} else {
				urlParameters.put(parameter[0], "");
			}
		}
		return urlParameters;
	}

	/**
	 *
	 * @param url
	 * @return
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
			} else if (siteFlag && (urlData.get("site").equals(""))) {
				urlData.put("site", urlTokens[i]);
				String[] splittedModuleAction = urlTokens[i + 1].split("-");
				urlData.put("module", splittedModuleAction[0]);
				if (splittedModuleAction.length > 1) {
					urlData.put("action", splittedModuleAction[1]);
				} else if (splittedModuleAction.length == 1) {
					urlData.put("action", "");
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
