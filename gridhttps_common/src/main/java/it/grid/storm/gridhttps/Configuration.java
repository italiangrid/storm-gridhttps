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
package it.grid.storm.gridhttps;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;



/**
 * @author Michele Dibenedetto
 *
 */
public class Configuration
{
    // Backend configuration
    
    private static String stormBackendHostname = null;
    private static int stormBackendRestPort = -1;
    private static String stormBackendIP = null;
    private static String contextDeployFolder = null;
    
    //gridhttps configuration
    public static final String CONTEXT_FILE_NAME_PREFIX = "storageArea";
    
    public static final String SERVICE_PATH = File.separator + Configuration.CONTEXT_FILE_NAME_PREFIX;
    
    public static final String CONTEXT_DEPLOY_FOLDER_KEY = "contextDeployFolder";
    
    /* BE Rest API URL */
    public static final String SERVER_HOST_CONFIGURATION_KEY = "server.hostname";
    public static final String SERVER_PORT_CONFIGURATION_KEY = "server.port";
    
    public static final String MAPPER_SERVLET_ENCODING_SCHEME = "UTF-8";
    
    public static void init(String hostname, int port, String containerContextDeployFolder) throws UnknownHostException
    {
        setStormBackendHostname(hostname);
        setStormBackendIP(InetAddress.getByName(hostname).getHostAddress());
        setStormBackendRestPort(port);
        setContextDeployFolder(containerContextDeployFolder);
    }

    /**
     * @param stormBackendIP the stormBackendIP to set
     */
    private static void setStormBackendIP(String stormBackendIP)
    {
        Configuration.stormBackendIP = stormBackendIP;
    }

    /**
     * @return the stormBackendIP
     */
    public static String getStormBackendIP()
    {
        return stormBackendIP;
    }

    /**
     * @param stormBackendHostname the stormBackendHostname to set
     */
    private static void setStormBackendHostname(String stormBackendHostname)
    {
        Configuration.stormBackendHostname = stormBackendHostname;
    }

    /**
     * @return the stormBackendHostname
     */
    public static String getStormBackendHostname()
    {
        return stormBackendHostname;
    }

    /**
     * @param stormBackendRestPort the stormBackendRestPort to set
     */
    private static void setStormBackendRestPort(int port)
    {
        Configuration.stormBackendRestPort = port;
    }

    /**
     * @return the stormBackendRestPort
     */
    public static int getStormBackendRestPort()
    {
        return stormBackendRestPort;
    }
    
    private static void setContextDeployFolder(String containerContextDeployFolder)
    {
        Configuration.contextDeployFolder = containerContextDeployFolder;
        
    }
    
    public static String getContextDeployFolder()
    {
        return contextDeployFolder;
    }

}
