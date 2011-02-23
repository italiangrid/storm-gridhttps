/*
 * Copyright (c) Istituto Nazionale di Fisica Nucleare (INFN). 2006-2010.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.grid.storm.gridhttps.filter;


import it.grid.storm.gridhttps.Configuration;
import it.grid.storm.gridhttps.remotecall.UserAuthzServiceConstants;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.glite.security.SecurityContext;
import org.glite.security.util.DN;
import org.glite.security.util.DNHandler;
import org.glite.voms.VOMSAttribute;
import org.glite.voms.VOMSValidator;

/**
 * @author Michele Dibenedetto
 */
public class RequestFilter implements Filter
{

    private FilterConfig filterConfig = null;
    private boolean started = false;
    /* Logging */
    /**
     * 
     */
    private static PrintWriter writer = null;
    /**
     * 
     */
    private String directory = "logs";
    /**
     * The prefix that is added to log file filenames.
     */
    private String prefix = "gridhttps.";
    /**
     * The suffix that is added to log file filenames.
     */
    private String suffix = "log";
    /**
     * The static validator object used to check VOMS attribute certificate
     * validity
     **/
    private static VOMSValidator validator = null;
    private static final String HTTP_SCHEMA = "http";
    private static final String HTTPS_SCHEMA = "https";
    private static final String READ_METHOD = "GET";
    private static final String WRITE_METHOD = "PUT";
    private static final String[] ALLOWED_METHODS = new String[] { READ_METHOD, WRITE_METHOD };
    private static String ENCODED_READ_OPERATION;
    private static String ENCODED_WRITE_OPERATION;
    /* BE Rest API URL */
    private static String SERVER_HOST_CONFIGURATION_KEY = "server.hostname";
    private static String SERVER_PORT_CONFIGURATION_KEY = "server.port";
    
    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException
    {
        System.out.println("The Filter Is Ready");
        this.filterConfig = filterConfig;
        if (started)
        {
            throw new ServletException("This filter has been already initialized!!");
        }
        started = true;
        open();
        initOperationNames();
        loadInitParameters();
        log("INFO: Filter initialized correctly");
    }

    /**
     * @throws ServletException
     */
    private void loadInitParameters() throws ServletException
    {
        String serverHost = filterConfig.getInitParameter(SERVER_HOST_CONFIGURATION_KEY);
        if (serverHost == null)
        {
            log("ERROR: No server host init parameter provided! Parameter name must be \'" + SERVER_HOST_CONFIGURATION_KEY + "\'");
            throw new ServletException("Error retrieving initialization parameters");
        }
        String serverPortString = filterConfig.getInitParameter(SERVER_PORT_CONFIGURATION_KEY);
        if (serverPortString == null)
        {
            log("ERROR: No server port init parameter provided! Parameter name must be \'" + SERVER_PORT_CONFIGURATION_KEY + "\'");
            throw new ServletException("Error retrieving initialization parameters");
        }
        int serverPort;
        try
        {
            serverPort = Integer.parseInt(serverPortString);
        }
        catch (NumberFormatException e)
        {
            log("ERROR: Unable to parse the server port init parameter \'" + serverPortString + "\'");
            throw new ServletException("Error parsing initialization parameters");
        }
        String containerContextDeployFolder = filterConfig.getInitParameter(Configuration.CONTEXT_DEPLOY_FOLDER_KEY);
        if (containerContextDeployFolder == null)
        {
            log("ERROR: No context deploy folder init parameter provided! Parameter name must be \'" + Configuration.CONTEXT_DEPLOY_FOLDER_KEY + "\'");
            throw new ServletException("Error retrieving initialization parameters");
        }
        try
        {
            Configuration.init(serverHost, serverPort, containerContextDeployFolder);
        }
        catch (UnknownHostException e)
        {
            log("ERROR: Unable initialize gridhttps configuration using host \'" + serverHost + "\' and port \'" + serverPort + "\'");
            throw new ServletException("Error configuring the service using provided initialization parameters");
        }
    }


    /**
     * @throws ServletException
     */
    private void initOperationNames() throws ServletException
    {
        if (ENCODED_READ_OPERATION == null)
            try
            {
                ENCODED_READ_OPERATION = URLEncoder.encode(UserAuthzServiceConstants.READ_OPERATION, UserAuthzServiceConstants.ENCODING_SCHEME);
                ENCODED_WRITE_OPERATION = URLEncoder.encode(UserAuthzServiceConstants.WRITE_OPERATION, UserAuthzServiceConstants.ENCODING_SCHEME);
            }
            catch (UnsupportedEncodingException e)
            {
                log("ERROR: unable to encode operations! UnsupportedEncodingException " + e.getMessage());
                throw new ServletException("Internal error! Unable to encode authorization server operation!");
            }
    }


    /**
     * Open the new log file for the date specified by <code>dateStamp</code>.
     */
    private synchronized void open()
    {
        if (writer == null)
        {
            // Create the directory if necessary
            File dir = new File(directory);
            if (!dir.isAbsolute())
            {
                dir = new File(System.getProperty("catalina.base"), directory);
                dir.mkdirs();
            }
            // Open the current log file
            try
            {
                String pathname = dir.getAbsolutePath() + File.separator + prefix + suffix;
                writer = new PrintWriter(new FileWriter(pathname, true), true);
            }
            catch (IOException e)
            {
                writer = null;
                System.err.println("Unable to intialize log writer! IOException: " + e.getMessage());
            }
        }
    }


    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy()
    {
        // Validate and update our current component state
        if (!started)
        {
            return;
        }
        started = false;
        log("INFO: Destroing the filter completed");
        close();
    }


    /**
     * Close the currently open log file (if any)
     */
    private synchronized void close()
    {
        if (writer != null)
        {
            writer.flush();
            writer.close();
            writer = null;
        }
    }


    /**
     * @param message
     */
    private synchronized void log(String message)
    {
        synchronized (this)
        {
            if (writer != null)
            {
                writer.println(message);
            }
            else
            {
                System.err.println("Unable to log, the log file write is null! The filter " + (started ? "is" : "is not") + " initialized");
            }
        }
    }


    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {  
        log("DEBUG: Filtering request from host " + request.getRemoteHost());
        if (!started)
        {
            throw new ServletException("Filter not started!");
        }
        HttpServletResponse HTTPResponse = null;
        HttpServletRequest HTTPRequest = null;
        if (HttpServletRequest.class.isAssignableFrom(request.getClass())
                && HttpServletResponse.class.isAssignableFrom(response.getClass()))
        {
            HTTPRequest = (HttpServletRequest) request;
            HTTPResponse = (HttpServletResponse) response;
        }
        else
        {
            log("ERROR: received non HTTP request. Class is : " + request.getClass());
            throw new ServletException("Protocol not supported. Use HTTP(S)");
        }
        
        String schema = request.getScheme(); 
        log("DEBUG: Requested method is : " + schema);
        if (!checkSchema(schema))
        {
            log("INFO: Received a request with au unknown schema. Schema is : " + schema);
            throw new ServletException("Schema not supported. Use HTTP(S)");
        }
        String method = HTTPRequest.getMethod();
        log("DEBUG: Requested method is : " + method);
        if (!methodAllowed(method))
        {
            log("INFO: Received a request for a not allowed method : " + method);
            HTTPResponse.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method " + method + " not allowed! Allowed methods: "
                    + Arrays.toString(ALLOWED_METHODS));
            return;
        }
        String contextPath = HTTPRequest.getContextPath();
        if(checkServletContext(contextPath))
        {
            log("DEBUG: Received a request for the servlet");
            chain.doFilter(request, response);
            return;
        }
        log("DEBUG: Served context path is : " + contextPath);
        if (!validateContextPath(contextPath))
        {
            log("INFO: serving a request on an servlet path not related to our service \'" + Configuration.SERVICE_PATH + "\' : " + contextPath);
            HTTPResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Requests must start with service path \'" + Configuration.SERVICE_PATH + "\'");
            return;
        }
        String requestedURI = HTTPRequest.getRequestURI();
        log("DEBUG: Requested resource is : " + requestedURI);
        if (!validateUri(requestedURI))
        {
            log("INFO: reveived a request on an URI not related to our service \'" + Configuration.SERVICE_PATH + "\' : " + requestedURI);
            HTTPResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Requests must start with service path \'" + Configuration.SERVICE_PATH + "\'");
            return;
        }
        String resourcePath = null;
        try
        {
            resourcePath = parseURI(requestedURI, contextPath);
        }
        catch (IllegalArgumentException e)
        {
            log("ERROR: Error parsing request URI. IllegalArgumentException : " + e.getMessage());
            HTTPResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error parsing request URI");
            return;
        }
        log("DEBUG: Resource path is : " + resourcePath);
        if (request.isSecure())
        {
            log("DEBUG: Request has been sent over a secure connection");
            try
            {
                setContextFromRequest(request);
            }
            catch (ServletException e)
            {
                log("ERROR: Unable to set security context for the request. ServletException : " + e.getMessage());
                HTTPResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to set security context for the request: "
                        + e.getMessage());
                return;
            }
            String subjectDNX500 = getDNX500FromSecurityContext();
            log("DEBUG: User DN is : " + subjectDNX500);
            String[] fquans = null;
            try
            {
                fquans = getFQANsFromSecurityContext();
            }
            catch (ServletException e)
            {
                log("ERROR: Unable to get FQANS from security context. ServletException : " + e.getMessage());
                HTTPResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to get FQANS from security context: "
                        + e.getMessage());
                return;
            }
            if (fquans == null || fquans.length == 0)
            {
                fquans = new String[0];
                log("INFO: No fquans found in request context");
            }
            else
            {
                log("DEBUG: retrieved FQUANS : " + Arrays.toString(fquans));
            }
            boolean isAuthorized = false;
            try
            {
                isAuthorized = isUserAuthorized(resourcePath, method, subjectDNX500, fquans);
            }
            catch (IllegalArgumentException e)
            {
                log("ERROR: Unable to verify user authorization. IllegalArgumentException" + e.getMessage());
                HTTPResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error testing user authorization");
                return;
            }
            catch (ServletException e)
            {
                log("ERROR: Unable to verify user authorization. ServletException : " + e.getMessage());
                HTTPResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error testing user authorization: " + e.getMessage());
                return;
            }
            if (isAuthorized)
            {
                chain.doFilter(request, response);
            }
            else
            {
                log("INFO: User is not authorized to access the requested resource");
                HTTPResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not authorized to access the requested resource");
                return;
            }
        }
        else
        {
            log("DEBUG: Received a request via a non secure connection (HTTP). No checks to perform");
            chain.doFilter(request, response);
        }
    }


    private boolean checkServletContext(String contextPath)
    {
        return(contextPath.startsWith("/gridhttps"));
    }


    /**
     * @param resourcePath
     * @param method
     * @param subjectDN
     * @param fqans
     * @return
     * @throws ServletException
     * @throws IllegalArgumentException
     */
    private boolean isUserAuthorized(String resourcePath, String method, String subjectDN, String[] fqans) throws ServletException,
                                                                                                           IllegalArgumentException
    {
        if (resourcePath == null || method == null || subjectDN == null || fqans == null)
        {
            log("ERROR: Received null parameter(s) at isUserAuthorized: resourcePath=" + resourcePath + " method=" + method + " subjectDN="
                    + subjectDN + " fqans=" + fqans);
            throw new IllegalArgumentException("Received null parameter(s)");
        }
        URI uri = prepareURI(resourcePath, method, subjectDN, fqans);
        log("INFO: uri = " + uri.toString());
        HttpGet httpget = new HttpGet(uri);
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse httpResponse;
        try
        {
            httpResponse = httpclient.execute(httpget);
        }
        catch (ClientProtocolException e)
        {
            log("ERROR: Error executing http call. ClientProtocolException " + e.getLocalizedMessage());
            throw new ServletException("Error contacting authorization service.");
        }
        catch (IOException e)
        {
            log("ERROR: Error executing http call. IOException " + e.getLocalizedMessage());
            throw new ServletException("Error contacting authorization service.");
        }
        StatusLine status = httpResponse.getStatusLine();
        if (status == null)
        {
            // never return null
            log("ERROR: Unexpected error! response.getStatusLine() returned null!");
            throw new ServletException("Unexpected error! response.getStatusLine() returned null! Please contact storm support");
        }
        int httpCode = status.getStatusCode();
        String httpMessage = status.getReasonPhrase();
        HttpEntity entity = httpResponse.getEntity();
        String output = "";
        if (entity != null)
        {
            InputStream responseIS;
            try
            {
                responseIS = entity.getContent();
            }
            catch (IllegalStateException e)
            {
                log("ERROR: unable to get the input content stream from server answer. IllegalStateException " + e.getLocalizedMessage());
                throw new ServletException("Error comunicationg with the authorization service.");
            }
            catch (IOException e)
            {
                log("ERROR: unable to get the input content stream from server answer. IOException " + e.getLocalizedMessage());
                throw new ServletException("Error comunicationg with the authorization service.");
            }
            int l;
            byte[] tmp = new byte[512];
            try
            {
                while ((l = responseIS.read(tmp)) != -1)
                {
                    output += new String(tmp, 0, l);
                }
            }
            catch (IOException e)
            {
                log("ERROR: Error reading from the connection error stream. IOException " + e.getMessage());
                throw new ServletException("Error comunicationg with the authorization service.");
            }
        }
        else
        {
            log("ERROR: no HttpEntity found in the response. Unable to determine the answer");
            throw new ServletException("Unable to get a valid authorization response from the server.");
        }
        log("INFO: Response is : \'" + output + "\'");
        if (httpCode != HttpURLConnection.HTTP_OK)
        {
            log("WARN: Unable to get a valid response from server. Received a non HTTP 200 response from the server : \'" + httpCode
                    + "\' " + httpMessage);
            throw new ServletException("Unable to get a valid response from server. " + httpMessage);
        }
        Boolean response = new Boolean(output);
        log("INFO: Boolean response: \'" + response + "\'");
        return response.booleanValue();
    }


    /**
     * @param resourcePath
     * @param method
     * @param subjectDN
     * @param fqans
     * @return
     * @throws ServletException
     * @throws IllegalArgumentException
     */
    private URI prepareURI(String resourcePath, String method, String subjectDN, String[] fqans) throws ServletException,
                                                                                                 IllegalArgumentException
    {
        if (resourcePath == null || method == null || subjectDN == null || fqans == null)
        {
            log("ERROR: Received null parameter(s) at prepareURL: resourcePath=" + resourcePath + " method=" + method + " subjectDN="
                    + subjectDN + " fqans=" + fqans);
            throw new IllegalArgumentException("Received null parameter(s)");
        }
        log("DEBUG: encoding parameters");
        String path;
        try
        {
            path = buildpath(URLEncoder.encode(resourcePath, UserAuthzServiceConstants.ENCODING_SCHEME), method, fqans.length > 0);
        }
        catch (UnsupportedEncodingException e)
        {
            log("ERROR: Exception encoding the path \'" + resourcePath + "\' UnsupportedEncodingException: " + e.getMessage());
            throw new ServletException("Unable to encode resourcePath paramether, unsupported encoding \'" + UserAuthzServiceConstants.ENCODING_SCHEME
                    + "\'");
        }
        String fqansList = null;
        if (fqans.length > 0)
        {
            fqansList = "";
            for (int i = 0; i < fqans.length; i++)
            {
                if (i > 0)
                {
                    fqansList += UserAuthzServiceConstants.FQANS_SEPARATOR;
                }
                fqansList += fqans[i];
            }
        }
        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair(UserAuthzServiceConstants.DN_KEY, subjectDN));
        if (fqansList != null)
        {
            qparams.add(new BasicNameValuePair(UserAuthzServiceConstants.FQANS_KEY, fqansList));
        }
        URI uri;
        try
        {
            uri = new URI("http", null,Configuration.getStormBackendHostname(), Configuration.getStormBackendRestPort(), path, URLEncodedUtils.format(qparams, "UTF-8"), null);
        }
        catch (URISyntaxException e)
        {
            log("createURI URISyntaxException " + e.getLocalizedMessage());
            throw new ServletException("createURI URISyntaxException " + e.getLocalizedMessage());
        }
        return uri;
    }


    /**
     * @param resourcePath
     * @param method
     * @param b
     * @return
     * @throws UnsupportedEncodingException
     */
    private String buildpath(String resourcePath, String method, boolean b) throws UnsupportedEncodingException
    {
        String operation;
        if (method.equals(READ_METHOD))
        {
            operation = ENCODED_READ_OPERATION;
        }
        else
        {
            operation = ENCODED_WRITE_OPERATION;
        }
        String path = "/" + UserAuthzServiceConstants.RESOURCE + "/" + UserAuthzServiceConstants.VERSION + "/" + resourcePath + "/" + operation + "/";
        if (b)
        {
            path += UserAuthzServiceConstants.VOMS_EXTENSIONS + "/";
        }
        else
        {
            path += UserAuthzServiceConstants.PLAIN + "/";
        }
        return path + UserAuthzServiceConstants.USER;
    }


    /**
     * Checks if the request schema is either http or https
     * 
     * @param request
     * @return
     */
    private boolean checkSchema(String schema)
    {
        boolean response = false;
        if (schema != null && (schema.equals(HTTP_SCHEMA) || schema.equals(HTTPS_SCHEMA)))
        {
            response = true;
        }
        return response;
    }


    /**
     * Checks if the requested HTTP/1.1 method is allowed
     * 
     * @param method
     * @return
     */
    private boolean methodAllowed(String method)
    {
        boolean response = false;
        for (String allowedMethod : ALLOWED_METHODS)
        {
            if (allowedMethod.equals(method))
            {
                response = true;
                break;
            }
        }
        return response;
    }


    /**
     * Checks if the provided string represents a path served context path (starting with our service path)
     * 
     * @param requestedURI
     * @return
     */
    private boolean validateContextPath(String contextPath)
    {
        return validateUri(contextPath);
    }


    /**
     * Checks if the provided string represents a path starting with our service path
     * 
     * @param requestedURI
     * @return
     */
    private boolean validateUri(String requestedURI)
    {
        boolean response = false;
        if (requestedURI != null && requestedURI.startsWith(Configuration.SERVICE_PATH + File.separatorChar))
        {
            response = true;
        }
        return response;
    }


    /**
     * Extracts from the URI the relative local file path
     * 
     * @param requestedURI
     * @return the absolute path of local file
     */
    private String parseURI(String requestedURI, String contextpath)
    {
        if (requestedURI == null || requestedURI.length() < contextpath.length())
        {
            throw new IllegalArgumentException("The provided argument string is too short");
        }
        int offset = 1;
        if (contextpath.charAt(contextpath.length() - 1) != '/')
        {
            offset = 0;
        }
        String relativePath = requestedURI.substring(contextpath.length() - offset, requestedURI.length());
        return this.filterConfig.getServletContext().getRealPath(relativePath);
    }


    /**
     * Initializes the SecurityContext and extracts from it the client subject DN
     * 
     * @param request
     * @return
     * @throws ServletException
     */
    private void setContextFromRequest(final ServletRequest request) throws ServletException
    {
        SecurityContext sc = new SecurityContext();
        SecurityContext.setCurrentContext(sc);
        X509Certificate[] certChain = null;
        certChain = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        if (certChain == null)
        {
            log("ERROR: No certificate found in request, even if it is under https!");
            throw new ServletException("No certificate found in request!");
        }
        sc.setClientCertChain(certChain);
        X509Certificate certificate = sc.getClientCert();
        DN subjectDN = DNHandler.getSubject(certificate);
        DN issuerDN = DNHandler.getIssuer(certificate);
        if (sc.getClientName() != null)
        {
            sc.setClientName(subjectDN.getX500());
        }
        if (sc.getIssuerName() != null)
        {
            sc.setIssuerName(issuerDN.getX500());
        }
    }


    /**
     * @return
     */
    private String getDNX500FromSecurityContext()
    {
        SecurityContext currentContext = SecurityContext.getCurrentContext();
        if (currentContext.getClientDN() == null)
        {
            log("ERROR: current context return null client DN");
            return null;
        }
        return currentContext.getClientDN().getX500();
    }


    /**
     * Extracts FQANS from user certificate available in current context
     * 
     * @return
     * @throws ServletException
     */
    private String[] getFQANsFromSecurityContext() throws ServletException
    {
        String[] fqans = null;
        log("DEBUG: Fectching FQANs out of the security context");
        SecurityContext currentContext = SecurityContext.getCurrentContext();
        if (validator == null)
        {
            log("DEBUG: Initializing VOMS validator object...");
            validator = new VOMSValidator(currentContext.getClientCertChain());
        }
        else
        {
            validator.setClientChain(currentContext.getClientCertChain());
        }
        try
        {
            validator.validate();
        }
        catch (IllegalArgumentException e)
        {
            log("ERROR: Error validating voms attributes out of the cert chain. IllegalArgumentException : " + e.getMessage());
            throw new ServletException("Error validating voms attributes out of the cert chain");
        }
        catch (Throwable e)
        {
            log("Error validating voms attributes out of the cert chain. Throwable : " + e.getMessage());
            throw new ServletException("Error validating voms attributes out of the cert chain");
        }
        List<VOMSAttribute> attrs = validator.getVOMSAttributes();
        if (attrs == null)
        {
            log("ERROR: Retrieved null voms attributes from voms validator");
            throw new ServletException("Retrieved null voms attributes from voms validator");
        }
        for (VOMSAttribute vomsAttr : attrs)
        {
            List<String> fqanAttrs = null;
            try
            {
                fqanAttrs = vomsAttr.getFullyQualifiedAttributes();
            }
            catch (IllegalArgumentException e)
            {
                log("Error validating voms attributes out of the cert chain. Throwable : " + e.getMessage());
                throw new ServletException("Error validating voms attributes out of the cert chain");
            }
            log("DEBUG: found " + fqanAttrs.size() + " FQANS");
            if (fqanAttrs != null && fqanAttrs.size() > 0)
            {
                fqans = new String[fqanAttrs.size()];
                for (int i = 0; i < fqanAttrs.size(); i++)
                {
                    fqans[i] = getFQAN(fqanAttrs.get(i));
                }
            }
            else
            {
                log("INFO: No VOMS AC found in client certificate chain");
                fqans = new String[0];
            }
        }
        return fqans;
    }


    /**
     * Removes empty fqans's attributes from the provided string
     * 
     * @param fqan
     * @return
     */
    private static String getFQAN(String fqan)
    {
        fqan = fqan.replaceAll("\\/Role=NULL", "");
        fqan = fqan.replaceAll("\\/Capability=NULL", "");
        return fqan;
    }
}
