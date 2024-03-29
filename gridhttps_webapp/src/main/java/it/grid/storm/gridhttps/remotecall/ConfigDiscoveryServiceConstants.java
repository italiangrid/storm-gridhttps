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
package it.grid.storm.gridhttps.remotecall;


/**
 * @author Michele Dibenedetto
 */
public class ConfigDiscoveryServiceConstants
{


    public static final String ENCODING_SCHEME = "UTF-8";
    public static final String RESOURCE = "configuration";
    public static final String VERSION = "1.0";
    public static final String LIST_ALL_KEY = "StorageAreaList";
    public static final char VFS_LIST_SEPARATOR = ':';
    public static final String VFS_NAME_KEY = "name";
    public static final char VFS_FIELD_MATCHER = '=';
    public static final char VFS_FIELD_SEPARATOR = '&';
    public static final String VFS_ROOT_KEY = "root";
    public static final String VFS_STFN_ROOT_KEY = "stfnRoot";
    public static final char VFS_STFN_ROOT_SEPARATOR = ';';
    /*
     * Usage samples in an HTTP GET call
     * /RESOURCE/VERSION/LIST_ALL_KEY
     * 
     * Response sample
     * name=DTEAMT0D1-FS&root=/storage/dteamt0d1&stfnRoot=/dteamt0d1;/dteam:name=ATLAST0D1-FS&root=/storage/atlast0d1&stfnRoot=/atlast0d1
     * 
     */
}
