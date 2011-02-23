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

import java.io.File;


/**
 * @author Michele Dibenedetto
 *
 */
public class StorageArea
{


    private String name;
    private String FSRoot;
    private String stfnRoot;


    public StorageArea(String name, String FSRoot, String stfnRoot)
    {
        this.name = name;
        this.FSRoot = normalizePath(FSRoot);
        this.stfnRoot = normalizePath(stfnRoot);
    }


    /**
     * @return the name
     */
    public final String getName()
    {
        return name;
    }


    /**
     * @return the root
     */
    public final String getFSRoot()
    {
        return FSRoot;
    }

    /**
     * @param path
     * @return
     */
    private final String normalizePath(String path)
    {
        if(path.charAt(path.length() - 1) == File.separatorChar)
        {
            if(path.charAt(0) != File.separatorChar)
            {
                return File.separatorChar + path.substring(0, path.length() - 1);
            }
            else
            {
                return path.substring(0, path.length() - 1);
            }
        }
        else
        {
            if(path.charAt(0) != File.separatorChar)
            {
                return File.separatorChar + path;   
            }
            else
            {
                return path;
            }
        } 
    }

    /**
     * @return the stfnRoot
     */
    public final String getStfnRoot()
    {
        return stfnRoot;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "StorageArea [name=" + name + ", root=" + FSRoot + ", stfnRoot=" + stfnRoot + "]";
    }
}
