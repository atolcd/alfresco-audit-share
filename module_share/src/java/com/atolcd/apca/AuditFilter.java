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
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.webscripts.connector.User;
import org.springframework.web.context.support.WebApplicationContextUtils;

@SuppressWarnings("deprecation")
public class AuditFilter implements Filter {
	private ServletContext servletContext;
	//Store the "object" parameters to get for each module
	private HashMap<String, String> moduleIds;

	@Override
	public void destroy() {}

	@Override
	public void init(FilterConfig args) throws ServletException {
		this.servletContext = args.getServletContext();
		
		this.moduleIds = new HashMap<String, String>();
		this.moduleIds.put("wiki", "title");
		this.moduleIds.put("blog", "postId");
		this.moduleIds.put("document", "nodeRef");
		this.moduleIds.put("documentlibrary", "filter");
		this.moduleIds.put("calendar", "view");
		this.moduleIds.put("links", "linkId");
		this.moduleIds.put("discussions", "topicId");
		this.moduleIds.put("data", "list");
		this.moduleIds.put("site", "");//Members tab
	}

	private ApplicationContext getApplicationContext() {
		return WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
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
				// perform a "silent" init - i.e. no user creation or remote connections
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
		
		if(user != null){	
			String ref = request.getHeader("referer");
			String requestURL = request.getRequestURL().toString();
			if(requestURL != null)
			{		
				if(requestURL.endsWith("/dologin") && (ref != null)){
					requestURL = ref;
				}
				
				HashMap<String, String> auditData = getAuditData(request, requestURL);
				ApcaAuditEntry auditSample = new ApcaAuditEntry();
				auditSample.setAuditUserId(user.getId());
				auditSample.setAuditSite(auditData.get("site"));
				auditSample.setAuditAppName(auditData.get("module"));
				auditSample.setAuditActionName(auditData.get("action"));
				auditSample.setAuditObject(auditData.get("object"));
				auditSample.setAuditTime(System.currentTimeMillis());
				
				//Remote call for DB
				try {
					remoteCall(request,auditSample);
				} catch (JSONException e) {
					System.out.println("Error during a remote call ...");
					e.printStackTrace();
				}
				
		    }
		}
		chain.doFilter(sReq, sRes);
	}


	private void remoteCall(HttpServletRequest request, ApcaAuditEntry entry) throws JSONException, URIException, UnsupportedEncodingException {
		Connector connector;
		try {
			connector = FrameworkUtil.getConnector(request.getSession(true), entry.getAuditUserId(), AlfrescoUserFactory.ALFRESCO_ENDPOINT_ID);

			// En get
			// String query ="/db/connect?entry=" + entry.toJSON();
			// Response resp = connector.call(URIUtil.encodeQuery(query));
			
			// parameters = null, on passe par le inputstream.
			// Le webscript est appelé avec l'audit converti en JSON.
			ConnectorContext postContext = new ConnectorContext(null,buildDefaultHeaders());
			postContext.setMethod(HttpMethod.POST);
			postContext.setContentType("text/plain;charset=UTF-8");
			InputStream in = new ByteArrayInputStream(entry.toJSON().getBytes("UTF-8"));
			
			//Appel au webscript
			Response resp = connector.call("/db/connect",postContext,in);
			
			System.out.println("Response : " + resp.toString());
			/*if (resp.getStatus().getCode() == Status.STATUS_OK) {
				try {
					JSONObject json = new JSONObject(resp.getResponse());
					if (json.has("firstName")) {
						System.out.println(json.get("firstName"));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}*/
		} catch (ConnectorServiceException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, String> getAuditData(HttpServletRequest request, String requestURL){
		//HashMap<String, String[]> parameters = (HashMap<String, String[]>)request.getParameterMap();
		HashMap<String, String> auditData = new HashMap<String, String>();
		
		String[] urlTokens = requestURL.split("/");
		
		//# pour les anchors dans les discussions - # non passé ?
		//String[] parametersTokens = (splittedUrl.length == 2) ? splittedUrl[1].split("[#&]") : null;
		
		HashMap<String, String> urlData = getUrlData(urlTokens);
		auditData.putAll(urlData);
		
		try {
			//On récupère l'identifiant de l'objet consulté à partir de son module
			//En cas de null, on catch et on met une chaîne vide.
			String obj = request.getParameter(this.moduleIds.get(urlData.get("module")));
			if(obj != null){
				auditData.put("object", obj);
			}
			else{
				auditData.put("object", "");
			}
		}
		catch(Exception e){
			auditData.put("object", "");
		}
		return auditData;	
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 */
	public HashMap<String, String> getUrlData(String[] urlTokens){
		HashMap<String, String> urlData = new HashMap<String, String>();
		urlData.put("module","");
		urlData.put("action","");
		urlData.put("site", "");
		
		boolean siteFlag = false;
		for(int i=0;i<urlTokens.length;i++){
			if(urlTokens[i].equals("site") && !siteFlag){
				siteFlag=true;
			}
			else if(siteFlag && (urlData.get("site").equals(""))){
				urlData.put("site", urlTokens[i]);
				String[] splittedModuleAction = urlTokens[i+1].split("-");
				urlData.put("module", splittedModuleAction[0]);
				if(splittedModuleAction.length > 1){
					urlData.put("action", splittedModuleAction[1]);
				}
				else if (splittedModuleAction.length == 1) {
					urlData.put("action", "");
				}
			}
		}
		return urlData;
	}	

   /**
    * Helper to build a map of the default headers for script requests - we send over
    * the current users locale so it can be respected by any appropriate REST APIs.
    *  
    * @return map of headers
    */  
    private static Map<String, String> buildDefaultHeaders()
    {
        Map<String, String> headers = new HashMap<String, String>(1, 1.0f);
	    headers.put("Accept-Language", I18NUtil.getLocale().toString().replace('_', '-'));
        return headers;
    }

}
