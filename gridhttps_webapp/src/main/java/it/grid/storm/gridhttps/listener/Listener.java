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

/**
 * @author Michele Dibenedetto
 *
 */
public class Listener implements ServletContextListener
{


    private String classesFolder = null;


    /*
     * This method is invoked when the Web Application has been removed
     * and is no longer able to accept requests
     */
    public void contextDestroyed(ServletContextEvent event)
    {
    }


    // This method is invoked when the Web Application
    // is ready to service requests
    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent event)
    {
        System.out.println("INFO: managing start event");
        ServletContext context = event.getServletContext();
        this.classesFolder = context.getRealPath("WEB-INF/classes/");
        if (!StorageAreaManager.initialized())
        {
            System.out.println("INFO: initializing the StorageAreaManager");
            try
            {
                LinkedList<StorageArea> storageAreas = populateStorageAreaConfiguration();
                createStorageAreaContext(storageAreas);
            }
            catch (ServletException e)
            {
                System.out.println("ERROR: error during StorageAreaManager initialization. ServletException: " + e.getMessage());
            }
        }
    }


    /**
     * @return
     * @throws ServletException
     */
    private synchronized LinkedList<StorageArea> populateStorageAreaConfiguration() throws ServletException
    {
        URI uri = buildConfigDiscoveryServiceUri();
        System.out.println("INFO: Configuration Discovery call uri = " + uri.toString());
        HttpGet httpget = new HttpGet(uri);
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse httpResponse;
        try
        {
            httpResponse = httpclient.execute(httpget);
        }
        catch (ClientProtocolException e)
        {
            System.out.println("ERROR: Error executing http call. ClientProtocolException " + e.getLocalizedMessage());
            throw new ServletException("Error contacting Configuration Discovery service.");
        }
        catch (IOException e)
        {
            System.out.println("ERROR: Error executing http call. IOException " + e.getLocalizedMessage());
            throw new ServletException("Error contacting Configuration Discovery service.");
        }
        StatusLine status = httpResponse.getStatusLine();
        if (status == null)
        {
            // never return null
            System.out.println("ERROR: Unexpected error! response.getStatusLine() returned null!");
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
                System.out.println("ERROR: unable to get the input content stream from server answer. IllegalStateException "
                        + e.getLocalizedMessage());
                throw new ServletException("Error comunicationg with the Configuration Discovery service.");
            }
            catch (IOException e)
            {
                System.out.println("ERROR: unable to get the input content stream from server answer. IOException "
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
                System.out.println("ERROR: Error reading from the connection error stream. IOException " + e.getMessage());
                throw new ServletException("Error comunicationg with the authorization service.");
            }
        }
        else
        {
            System.out.println("ERROR: no HttpEntity found in the response. Unable to determine the answer");
            throw new ServletException("Unable to get a valid configuration discovery response from the server.");
        }
        System.out.println("INFO: Response is : \'" + output + "\'");
        if (httpCode != HttpURLConnection.HTTP_OK)
        {
            System.out.println("WARN: Unable to get a valid response from server. Received a non HTTP 200 response from the server : \'"
                    + httpCode + "\' " + httpMessage);
            throw new ServletException("Unable to get a valid response from server. " + httpMessage);
        }
        LinkedList<StorageArea> storageAreaList = decodeStorageAreaList(output);
        StorageAreaManager.init(storageAreaList);
        System.out.println(storageAreaList.toString());
        return storageAreaList;
    }


    private static URI buildConfigDiscoveryServiceUri() throws ServletException
    {
        System.out.println("DEBUG: encoding parameters");
        String path = "/" + ConfigDiscoveryServiceConstants.RESOURCE + "/" + ConfigDiscoveryServiceConstants.VERSION + "/"
                + ConfigDiscoveryServiceConstants.LIST_ALL_KEY;
        URI uri;
        try
        {
            uri = new URI("http", null, Configuration.getStormBackendHostname(), Configuration.getStormBackendRestPort(), path, null, null);
        }
        catch (URISyntaxException e)
        {
            System.out.println("ERROR: Unable to create Configuration Discovery URI. URISyntaxException " + e.getLocalizedMessage());
            throw new ServletException("Unable to create Configuration Discovery URI");
        }
        return uri;
    }


    /**
     * @param output
     * @return
     */
    private static LinkedList<StorageArea> decodeStorageAreaList(String output)
    {
        LinkedList<StorageArea> local = new LinkedList<StorageArea>();
        String[] SAEncodedArray = output.trim().split("" + ConfigDiscoveryServiceConstants.VFS_LIST_SEPARATOR);
        for (String SAEncoded : SAEncodedArray)
        {
            local.addAll(decodeStorageArea(SAEncoded));
        }
        return local;
    }


    /**
     * @param sAEncoded
     * @return
     */
    private static List<StorageArea> decodeStorageArea(String sAEncoded)
    {
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
                continue;
            }
            if (ConfigDiscoveryServiceConstants.VFS_ROOT_KEY.equals(keyValue[0]))
            {
                root = keyValue[1];
                continue;
            }
            if (ConfigDiscoveryServiceConstants.VFS_STFN_ROOT_KEY.equals(keyValue[0]))
            {
                String[] stfnRootArray = keyValue[1].trim().split("" + ConfigDiscoveryServiceConstants.VFS_STFN_ROOT_SEPARATOR);
                for (String stfnRoot : stfnRootArray)
                {
                    stfnRootList.add(stfnRoot);
                }
                continue;
            }
        }
        for (String stfnRoot : stfnRootList)
        {
            producedList.add(new StorageArea(name, root, stfnRoot));
        }
        return producedList;
    }


    /**
     * @param storageAreas
     */
    public void createStorageAreaContext(LinkedList<StorageArea> storageAreas)
    {
        System.out.println("DEBUG: Creating context files");
        for (StorageArea storageArea : storageAreas)
        {
            try
            {
                createContextFile(storageArea);
            }
            catch (TemplateException e)
            {
                System.out.println("ERROR: Unable to create context file for storageArea \'" + storageArea + "\' TemplateException: "
                        + e.getMessage());
            }
        }
    }


    /**
     * @param storageArea
     * @throws TemplateException
     */
    private void createContextFile(StorageArea storageArea) throws TemplateException
    {
        System.out.println("DEBUG: Creating context file for storageArea \'" + storageArea + "\'");
        StorageAreaContextTemplate template = new StorageAreaContextTemplate(Configuration.getContextDeployFolder(),
                                                                             storageArea.getStfnRoot(), this.classesFolder);
        template.setSARoot(storageArea.getFSRoot());
        String contextFilePath = template.getFilePath();
        //TODO avoid this null!
        if (contextFilePath != null)
        {
            File oldContextFile = new File(contextFilePath);
            if (oldContextFile.exists())
            {
                System.out.println("DEBUG: Deleting old context file");
                if (oldContextFile.delete())
                {
                    System.out.println("DEBUG: Old context file deleted");
                }
                else
                {
                    System.out.println("WARN: Unable to delete old context file");
                }
            }
        }
        template.buildFile();
    }
}
