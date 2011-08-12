package com.atolcd.apca;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
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
import org.apache.commons.io.IOUtils;
import org.hamcrest.core.Is;
import org.json.JSONException;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ByteArrayResource;
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
public class ProxyAuditFilter implements Filter {
	private ServletContext servletContext;

	@Override
	public void destroy() {}

	@Override
	public void init(FilterConfig args) throws ServletException {
		this.servletContext = args.getServletContext();
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
		
		System.out.println("Request : " + request.getRequestURL().toString());
		System.out.println("QueryString : " + request.getQueryString());
		
		if(user != null){	
			System.out.println("Request : " + request.getRequestURL().toString());
			System.out.println("QueryString : " + request.getQueryString());
			System.out.println("Method : " + request.getMethod());
			System.out.println("Content type = " + request.getContentType());
			
			
			int size = request.getContentLength();
			if(size > 0){
				System.out.println("ref : " + request.getHeader("referer"));
				System.out.println("Lenght : " + request.getContentLength());
				String tmp = request.getContentType().split(";")[0];
				if(tmp.equals("application/json")){

					StringWriter writer=new StringWriter();

					//BufferedInputStream is = new BufferedInputStream(request.getInputStream());
					// InputStreamReader streamReader=new InputStreamReader(is);
					//le buffer permet le readline
					//BufferedReader buffer=new BufferedReader(streamReader);
					//String dze = IOUtils.toString(request.getInputStream());
					//System.out.println(dze);
					
					/*InputStream bs = new BufferedInputStream(sReq.getInputStream());
					bs.mark(0);
					byte[] b = new byte[size];
					bs.read(b);
					bs.reset();*/

					//request.getReader()
					/*String line="";
					while ( null!=(line=buffer.readLine())){
					 writer.write(line); 
					}
					
					// Sortie finale dans le String
					System.out.println("Content : " + writer.toString());
					System.out.println(request.getInputStream().available());*/

				}
			}
		}

		chain.doFilter(sReq, sRes);
		
	}


	@SuppressWarnings("unused")
	private void remoteCall(HttpServletRequest request, ApcaAuditEntry entry) throws JSONException, URIException, UnsupportedEncodingException {
		Connector connector;
		try {
			connector = FrameworkUtil.getConnector(request.getSession(true), entry.getAuditUserId(), AlfrescoUserFactory.ALFRESCO_ENDPOINT_ID);
			
			// parameters = null, on passe par le inputstream.
			// Le webscript est appelé avec l'audit converti en JSON.
			ConnectorContext postContext = new ConnectorContext(null,buildDefaultHeaders());
			postContext.setMethod(HttpMethod.POST);
			postContext.setContentType("text/plain;charset=UTF-8");
			InputStream in = new ByteArrayInputStream(entry.toJSON().getBytes("UTF-8"));
			
			//Appel au webscript
			Response resp = connector.call("/db/connect",postContext,in);
			
			System.out.println("Response : " + resp.toString());

		} catch (ConnectorServiceException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 */
	public HashMap<String, String> getAuditData(HttpServletRequest request, String requestURL){
		HashMap<String, String> auditData = new HashMap<String, String>();
		
		String[] urlTokens = requestURL.split("/");
	
		HashMap<String, String> urlData = getUrlData(urlTokens);
		auditData.putAll(urlData);
		
		auditData.put("object", "");
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
