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
package it.grid.storm.gridhttps.listener;


import it.grid.storm.gridhttps.Configuration;
import it.grid.storm.gridhttps.StateObserver;
import it.grid.storm.gridhttps.StatefullObservable;
import it.grid.storm.gridhttps.Configuration.ConfigurationParameters;
import it.grid.storm.gridhttps.log.LoggerManager;
import it.grid.storm.gridhttps.remotecall.ConfigDiscoveryServiceConstants;
import it.grid.storm.gridhttps.storagearea.StorageArea;
import it.grid.storm.gridhttps.storagearea.StorageAreaManager;
import it.grid.storm.gridhttps.template.StorageAreaContextTemplate;
import it.grid.storm.gridhttps.template.TemplateException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

/**
 * @author Michele Dibenedetto
 *
 */
public class Listener extends StateObserver implements ServletContextListener 
{

    private static Logger log = LoggerManager.getLogger(Listener.class);
    
    private String classesFolder = null;
    
    private static final String CLASSES_FOLDER_RELATIVE_PATH = "WEB-INF" + File.separatorChar + "classes" + File.separatorChar;

    /**
     * @param observable
     */
    public Listener(StatefullObservable observable)
    {
        super(observable);
    }

    /**
     * 
     */
    public Listener()
    {
        this(Configuration.getInstance());
    }
    
    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent event)
    {
        log.info("Webapp Context destroied");
    }


    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent event)
    {
        log.info("Initializing Webapp Context");
        ServletContext context = event.getServletContext();
        this.classesFolder = context.getRealPath(CLASSES_FOLDER_RELATIVE_PATH);
        if (!StorageAreaManager.initialized())
        {
            log.debug("StorageAreaManager not initialized");
            if(observableReady())
            {
                initializeStorageAreas((ConfigurationParameters)observable.getState());
            }
        }
        else
        {
            log.debug("StorageAreaManager already initialized");
        }
    }
    
    /* (non-Javadoc)
     * @see it.grid.storm.gridhttps.WaitingObserver#update(java.lang.Object)
     */
    @Override
    protected void update(Object status)
    {
        log.debug("Received updated observable status");
        if(status.getClass().isAssignableFrom(ConfigurationParameters.class))
        {
            log.debug("It is the ConfigurationParameters status from Configuration");
            initializeStorageAreas((ConfigurationParameters)status);    
        }
        else
        {
            log.warn("Unable to managed the updated status, not instance of " + ConfigurationParameters.class);
        }
    }
    
    /**
     * @param stormBackendParameters
     */
    private void initializeStorageAreas(ConfigurationParameters stormBackendParameters)
    {
        try
        {
            log.info("Initializing the StorageAreaManager");
            LinkedList<StorageArea> storageAreas = populateStorageAreaConfiguration(stormBackendParameters);
            log.info("Creating context files for the retrieved Storage Areas");
            createStorageAreaContext(storageAreas, stormBackendParameters.getContextDeployFolder());
        }
        catch (ServletException e)
        {
            log.error("Error during StorageAreaManager initialization. ServletException: " + e.getMessage());
        }
        
    }

    
    /**
     * @param stormBackendParameters 
     * @return
     * @throws ServletException
     */
    private synchronized LinkedList<StorageArea> populateStorageAreaConfiguration(final ConfigurationParameters stormBackendParameters) throws ServletException
    {
        URI uri = buildConfigDiscoveryServiceUri(stormBackendParameters);
        log.info("Calling Configuration Discovery service at " + uri);
        HttpGet httpget = new HttpGet(uri);
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse httpResponse;
        try
        {
            httpResponse = httpclient.execute(httpget);
        }
        catch (ClientProtocolException e)
        {
            log.error("Error executing http call. ClientProtocolException " + e.getLocalizedMessage());
            throw new ServletException("Error contacting Configuration Discovery service.");
        }
        catch (IOException e)
        {
            log.error("Error executing http call. IOException " + e.getLocalizedMessage());
            throw new ServletException("Error contacting Configuration Discovery service.");
        }
        StatusLine status = httpResponse.getStatusLine();
        if (status == null)
        {
            // never return null
            log.error("Unexpected error! response.getStatusLine() returned null!");
            throw new ServletException("Unexpected error! response.getStatusLine() returned null! Please contact storm support");
        }
        int httpCode = status.getStatusCode();
        log.debug("Http call return code is: " + httpCode);
        String httpMessage = status.getReasonPhrase();
        log.debug("Http call return reason phrase is: " + httpMessage);
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
                log.error("Unable to get the input content stream from server answer. IllegalStateException "
                        + e.getLocalizedMessage());
                throw new ServletException("Error comunicationg with the Configuration Discovery service.");
            }
            catch (IOException e)
            {
                log.error("Unable to get the input content stream from server answer. IOException "
                        + e.getLocalizedMessage());
                throw new ServletException("Error comunicationg with the Configuration Discovery service.");
            }
            int l;
            byte[] tmp = new byte[1024];
            try
            {
                while ((l = responseIS.read(tmp)) != -1)
                {
                    output += new String(tmp, 0, l);
                }
            }
            catch (IOException e)
            {
                log.error("Error reading from the connection error stream. IOException " + e.getMessage());
                throw new ServletException("Error comunicationg with the authorization service.");
            }
        }
        else
        {
            log.error("No HttpEntity found in the response. Unable to determine the answer");
            throw new ServletException("Unable to get a valid configuration discovery response from the server.");
        }
        log.debug("Response is : \'" + output + "\'");
        if (httpCode != HttpURLConnection.HTTP_OK)
        {
            log.warn("Unable to get a valid response from server. Received a non HTTP 200 response from the server : \'"
                    + httpCode + "\' " + httpMessage);
            throw new ServletException("Unable to get a valid response from server. " + httpMessage);
        }
        log.debug("Decoding the receive response");
        LinkedList<StorageArea> storageAreaList = decodeStorageAreaList(output);
        log.debug("Initializing the Storage Area Manager");
        StorageAreaManager.init(storageAreaList);
        return storageAreaList;
    }


    /**
     * Builds the URI of the configuration discovery service
     * @param stormBackendParameters 
     * 
     * @return
     * @throws ServletException
     */
    private static URI buildConfigDiscoveryServiceUri(final ConfigurationParameters stormBackendParameters) throws ServletException
    {
        log.debug("Building configurationd discovery rest service URI");
        String path = "/" + ConfigDiscoveryServiceConstants.RESOURCE + "/" + ConfigDiscoveryServiceConstants.VERSION + "/"
                + ConfigDiscoveryServiceConstants.LIST_ALL_KEY;
        URI uri;
        try
        {
            uri = new URI("http", null, stormBackendParameters.getStormBackendHostname(), stormBackendParameters.getStormBackendRestPort(), path, null, null);
        }
        catch (URISyntaxException e)
        {
            log.error("Unable to create Configuration Discovery URI. URISyntaxException " + e.getLocalizedMessage());
            throw new ServletException("Unable to create Configuration Discovery URI");
        }
        log.debug("Built configuration discovery URI: " + uri);
        return uri;
    }


    /**
     * @param storageAreaListString
     * @return never null, a list that contains the decoded storage areas. None of the elements can be null
     */
    private static LinkedList<StorageArea> decodeStorageAreaList(String storageAreaListString)
    {
        if(storageAreaListString == null)
        {
            log.error("Decoding failed, received a null storage area list string!");
            throw new IllegalArgumentException("Received a null storage area list string");
        }
        LinkedList<StorageArea> local = new LinkedList<StorageArea>();
        String[] SAEncodedArray = storageAreaListString.trim().split("" + ConfigDiscoveryServiceConstants.VFS_LIST_SEPARATOR);
        log.debug("Decoding " + SAEncodedArray.length + " storage areas");
        for (String SAEncoded : SAEncodedArray)
        {
            local.addAll(decodeStorageArea(SAEncoded));
        }
        return local;
    }


    /**
     * Given a strings decodes the string in one or more StorageArea instances
     * 
     * @param sAEncoded
     * @return a list of StorageArea instances, never null. None of the elements can be null
     */
    private static List<StorageArea> decodeStorageArea(String sAEncoded)
    {
        if(sAEncoded == null)
        {
            log.error("Decoding failed, received a null encoded storage area!");
            throw new IllegalArgumentException("Received a null encoded storage area");
        }
        log.debug("Deconding storage area string \'" + sAEncoded + "\'");
        LinkedList<StorageArea> producedList = new LinkedList<StorageArea>();
        String name = null;
        String root = null;
        List<String> stfnRootList = new LinkedList<String>();
        String[] SAFields = sAEncoded.trim().split("" + ConfigDiscoveryServiceConstants.VFS_FIELD_SEPARATOR);
        for (String SAField : SAFields)
        {
            String[] keyValue = SAField.trim().split("" + ConfigDiscoveryServiceConstants.VFS_FIELD_MATCHER);
            if (ConfigDiscoveryServiceConstants.VFS_NAME_KEY.equals(keyValue[0]))
            {
                name = keyValue[1];
                log.debug("Found name: " + name);
                continue;
            }
            if (ConfigDiscoveryServiceConstants.VFS_ROOT_KEY.equals(keyValue[0]))
            {
                root = keyValue[1];
                log.debug("Found File System Root: " + name);
                continue;
            }
            if (ConfigDiscoveryServiceConstants.VFS_STFN_ROOT_KEY.equals(keyValue[0]))
            {
                String[] stfnRootArray = keyValue[1].trim().split("" + ConfigDiscoveryServiceConstants.VFS_STFN_ROOT_SEPARATOR);
                for (String stfnRoot : stfnRootArray)
                {
                    stfnRootList.add(stfnRoot);
                    log.debug("Found Storage File Name Root: " + stfnRoot);
                }
                continue;
            }
        }
        if(name == null || root == null ||stfnRootList.size() == 0)
        {
            log.warn("Unable to decode the storage area. Some fileds are missin: name=" + name + " FSRoot=" + root + " stfnRootList=" + stfnRootList);
            throw new IllegalArgumentException("");
        }
        for (String stfnRoot : stfnRootList)
        {
            log.debug("Decoded storage area: [" + name + "," + root + "," + stfnRoot + "]");
            producedList.add(new StorageArea(name, root, stfnRoot));
        }
        log.debug("Decoded " + producedList.size() + " storage areas");
        return producedList;
    }


    /**
     * Creates a servlet context file for each StorageArea in the provided storageAreas list
     * 
     * @param storageAreas
     * @param string 
     */
    private void createStorageAreaContext(LinkedList<StorageArea> storageAreas, final String contextDeployFolder)
    {
        log.debug("Creating StorageAreas context files");
        for (StorageArea storageArea : storageAreas)
        {
            try
            {
                createContextFile(storageArea, contextDeployFolder);
            }
            catch (TemplateException e)
            {
                log.error("Unable to create context file for storageArea \'" + storageArea + "\' TemplateException: "
                        + e.getMessage());
            }
        }
    }


    /**
     * @param storageArea the storage area for which the context file has to be created, never null
     * @throws TemplateException
     */
    private void createContextFile(StorageArea storageArea, final String contextDeployFolder) throws TemplateException
    {
        log.debug("Creating context file for storageArea \'" + storageArea + "\'");
        log.debug("Creating context template");
        StorageAreaContextTemplate template = new StorageAreaContextTemplate(contextDeployFolder,
                                                                             storageArea.getStfnRoot(), this.classesFolder);
        log.debug("Adding to context file template actual parameters");
        template.setSARoot(storageArea.getFSRoot());
        log.debug("Building context file template");
        template.buildFile();
    }
}
