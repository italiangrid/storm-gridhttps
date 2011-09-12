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
package it.grid.storm.gridhttps.log;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author Michele Dibenedetto
 */
public class LoggerManager
{

    private static Boolean initialized = new Boolean(Boolean.FALSE);
    
    private static Logger configurationLogger = null; 
    
    /**
     * Initialized log4j with the gridhttps server log4j properties file 
     */
    private static void initLoggers()
    {
        synchronized (initialized)
        {
            PropertyConfigurator.configure(it.grid.storm.gridhttps.Configuration.LOG4J_CONFIGURATION_FILE_PATH);
            initialized = new Boolean(Boolean.TRUE);
        }
    }


    /**
     * Provides a logger given the requested class
     * 
     * @param clazz a class 
     * @return the requested logger
     */
    public static Logger getLogger(Class clazz)
    {
        synchronized (initialized)
        {
            if (!initialized)
            {
                initLoggers();
            }
        }
        return Logger.getLogger(clazz);
    }


    /**
     * @return
     */
    public static Category getConfigurationLogger()
    {
        if(configurationLogger == null)
        {
            configurationLogger = getLogger(it.grid.storm.gridhttps.Configuration.class);
        }
        return configurationLogger;
    }
}
