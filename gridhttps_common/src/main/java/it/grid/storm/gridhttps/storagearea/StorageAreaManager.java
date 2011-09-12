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
package it.grid.storm.gridhttps.storagearea;


import it.grid.storm.gridhttps.log.LoggerManager;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * @author Michele Dibenedetto
 */
public class StorageAreaManager
{


    /**
     * 
     */
    private static Logger log = LoggerManager.getLogger(StorageAreaManager.class);

    /**
     * The list of all storage areas configured at storm backend
     * NOTE: if storm backend is restarted gridhttps server must be restarted too!
     */
    private static List<StorageArea> storageAreas = null;


    /**
     * Avoid instantiation
     */
    private StorageAreaManager()
    {
    }


    /**
     * @return true if a list of storage areas is available
     */
    public static boolean initialized()
    {
        return StorageAreaManager.storageAreas != null;
    }


    /**
     * Initializes the class with a list of storage areas
     * 
     * @param SAList
     */
    public static synchronized void init(List<StorageArea> SAList)
    {
        if (storageAreas == null)
        {
            log.info("Initializing the Storage Area manager with following StorageAreas: " + SAList.toString());
            storageAreas = SAList;
        }
    }


    /**
     * Searches for a storage area in the available list that has an FSRoot that is the longest
     * match with the provided file path
     * 
     * @param localFilePath must not be null
     * @return the best match StorageArea, null if none matches
     * @throws IllegalArgumentException if localFilePath is null
     */
    public static StorageArea getMatchingSA(String localFilePath) throws IllegalArgumentException
    {
        if (localFilePath == null)
        {
            log.error("Unable to match StorageArea, the provided localFilePath is null");
            throw new IllegalArgumentException("Provided localFilePath il null!");
        }
        if (!initialized())
        {
            log.error("Unable to match StorageArea, class not initialized. " + "Call init() first");
            throw new IllegalStateException("Unable to match any StorageArea, class not initialized.");
        }
        log.debug("Looking for a StorageArea that matches " + localFilePath);
        StorageArea mappedSA = null;
        int matchedSAFSRootLength = 0;
        for (StorageArea storageArea : storageAreas)
        {
            if (localFilePath.startsWith(storageArea.getFSRoot())
                    && (mappedSA == null || storageArea.getFSRoot().length() > mappedSA.getFSRoot().length()))
            {
                if (storageArea.getStfnRoot().length() > matchedSAFSRootLength)
                {
                    mappedSA = storageArea;
                    matchedSAFSRootLength = storageArea.getStfnRoot().length();
                }
            }
        }
        if(mappedSA == null)
        {
            log.debug("No match found");
        }
        else
        {
            log.debug("Matched StorageArea " + mappedSA.toString());
        }
        return mappedSA;
    }
}
