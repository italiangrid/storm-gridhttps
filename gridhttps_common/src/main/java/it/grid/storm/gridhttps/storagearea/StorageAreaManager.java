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


import java.util.List;

/**
 * @author Michele Dibenedetto
 */
public class StorageAreaManager
{


    /**
     * The list of all storage areas configured at storm backend
     * NOTE: if storm backend is restarted gridhttps must be restarted too!
     */
    private static List<StorageArea> storageAreas = null;


    private StorageAreaManager()
    {
    }


    public static boolean initialized()
    {
        return StorageAreaManager.storageAreas != null;
    }


    public static void init(List<StorageArea> SAList)
    {
        if (storageAreas == null)
        {
            storageAreas = SAList;
        }
    }


    public static StorageArea getSA(String localFilePath)
    {
        StorageArea mappedSA = null;
        int matchedSAFSRootLength = 0;
        for (StorageArea storageArea : storageAreas)
        {
            if (localFilePath.startsWith(storageArea.getFSRoot())
                    && (mappedSA == null || storageArea.getFSRoot().length() > mappedSA.getFSRoot().length()))
            {
                if(storageArea.getStfnRoot().length() > matchedSAFSRootLength)
                {
                    mappedSA = storageArea;
                    matchedSAFSRootLength = storageArea.getStfnRoot().length();
                }
            }
        }
        return mappedSA;
    }
}
