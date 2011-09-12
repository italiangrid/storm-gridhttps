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

import it.grid.storm.gridhttps.log.LoggerManager;
import it.grid.storm.gridhttps.StatefullObservable;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Michele Dibenedetto
 *
 */
public class Configuration extends StatefullObservable
{
    
    private ConfigurationParameters parameters = null; 
    
    //gridhttps configuration
    
    public static final String CONTEXT_FILE_NAME_PREFIX = "storageArea";
    
    public static final String SERVICE_PATH = File.separator + Configuration.CONTEXT_FILE_NAME_PREFIX;
    
    public static final String CONTEXT_DEPLOY_FOLDER_KEY = "contextDeployFolder";
    
    public static final String SERVLET_CONTEXT_PATH = "/gridhttps";
    
    public static final String LOG_FOLDER_PATH = File.separatorChar + "var" + File.separatorChar + "log" + File.separatorChar + "storm";
    /**
     * The path of the lo4j configuration file for gridhttps server
     */
    public static final String LOG4J_CONFIGURATION_FILE_PATH = File.separatorChar + "etc" + File.separatorChar + "storm" + File.separatorChar +
    "gridhttps-server" + File.separatorChar + "log4j-gridhttps-server.properties";
    
    
    /* BE Rest API URL */
    public static final String SERVER_HOST_CONFIGURATION_KEY = "server.hostname";
    public static final String SERVER_PORT_CONFIGURATION_KEY = "server.port";
    
    public static final String MAPPER_SERVLET_ENCODING_SCHEME = "UTF-8";
    
    private static final Configuration instance = new Configuration();
    
    private Configuration()
    {}
    
    public static Configuration getInstance()
    {
        return instance;
    }
    
    /**
     * Initializes the configuration class and notifies eventually observers
     * 
     * @param hostname storm Backend hostname
     * @param port storm Backend rest port
     * @param containerContextDeployFolder the folder where the servlet container looks for contxt files
     * @throws UnknownHostException if the hostname cannot be resolved
     * @throws IllegalArgumentException if any of the parameters is null, an empty string or lower than 0
     */
    public synchronized void init(String hostname, int port, String containerContextDeployFolder) throws UnknownHostException, IllegalArgumentException
    {
        if(hostname == null || hostname.equals("") || port < 0 || containerContextDeployFolder == null || containerContextDeployFolder.equals(""))
        {
            LoggerManager.getConfigurationLogger().error("Unable to initialize Configuration! Provided some null/empty parameters: hostname =" + hostname + " , port=" + port + " , containerContextDeployFolder ="
                    + containerContextDeployFolder);
            throw new IllegalArgumentException("Unable to initialize Configuration! Provided some null/empty parameters");
        }
        LoggerManager.getConfigurationLogger().debug("Initializing...");
        String stormBackendIP = InetAddress.getByName(hostname).getHostAddress();
        this.parameters = new ConfigurationParameters(hostname, stormBackendIP, port, containerContextDeployFolder);
        LoggerManager.getConfigurationLogger().debug("Registering this observable as changed");
        this.setChanged();
        LoggerManager.getConfigurationLogger().debug("Notifying the observers");
        this.notifyObservers();
        LoggerManager.getConfigurationLogger().debug("Initialization completed");
    }
    
    public synchronized boolean isInitialized()
    {
        return parameters != null;
    }
    
    /* (non-Javadoc)
     * @see it.grid.storm.gridhttps.StatefullObservable#getState()
     */
    @Override
    public synchronized ConfigurationParameters getState()
    {
        return this.parameters;
    }

    public class ConfigurationParameters
    {
     // Backend configuration
        
        private String stormBackendHostname = null;
        private Integer stormBackendRestPort = null;
        private String stormBackendIP = null;
        private String contextDeployFolder = null;
        
        protected ConfigurationParameters(String hostname, String stormBackendIP, int port, String containerContextDeployFolder)
        {
            setStormBackendHostname(hostname);
            setStormBackendIP(stormBackendIP);
            setStormBackendRestPort(port);
            setContextDeployFolder(containerContextDeployFolder);
        }
        
        /**
         * @param stormBackendIP the stormBackendIP to set
         */
        private void setStormBackendIP(String stormBackendIP)
        {
            this.stormBackendIP = new String(stormBackendIP);
        }

        /**
         * @return the stormBackendIP
         */
        public String getStormBackendIP()
        {
            return stormBackendIP;
        }

        /**
         * @param hostname the stormBackendHostname to set
         */
        private void setStormBackendHostname(String hostname)
        {
            LoggerManager.getConfigurationLogger().debug("Setting stormBackendHostname to " + hostname);
            this.stormBackendHostname = new String(hostname);
        }

        /**
         * @return the stormBackendHostname
         */
        public String getStormBackendHostname()
        {
            return stormBackendHostname;
        }

        /**
         * @param stormBackendRestPort the stormBackendRestPort to set
         */
        private void setStormBackendRestPort(Integer port)
        {
            LoggerManager.getConfigurationLogger().debug("Setting stormBackendRestPort to " + port);
            this.stormBackendRestPort = new Integer(port);
        }

        /**
         * @return the stormBackendRestPort
         */
        public Integer getStormBackendRestPort()
        {
            return stormBackendRestPort;
        }
        
        /**
         * @param contextDeployFolder the containerContextDeployFolder to set
         */
        private void setContextDeployFolder(String contextDeployFolder)
        {
            LoggerManager.getConfigurationLogger().debug("Setting contextDeployFolder to " + contextDeployFolder);
            this.contextDeployFolder = contextDeployFolder;
        }
        
        /**
         * @return the contextDeployFolder
         */
        public String getContextDeployFolder()
        {
            return contextDeployFolder;
        }
    }
}
