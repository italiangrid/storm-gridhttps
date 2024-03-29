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
public class UserAuthzServiceConstants
{


    public static final String ENCODING_SCHEME = "UTF-8";
    public static final String RESOURCE = "authorization";
    public static final String VERSION = "1.0";
    public static final String READ_OPERATION = "read";
    public static final String WRITE_OPERATION = "write";
    public static final String VOMS_EXTENSIONS = "voms";
    public static final String PLAIN = "plain";
    public static final String USER = "user";
    public static final String DN_KEY = "DN";
    public static final String FQANS_KEY = "FQANS";
    public static final String FQANS_SEPARATOR = ",";
    
    /*
     * Usage samples in an HTTP GET call
     * /RESOURCE/VERSION/path/READ_OPERATION/VOMS_EXTENSIONS/USER?DN_KEY=dn&FQANS_KEY=fquanFQANS_SEPARATORfquan
     * /RESOURCE/VERSION/path/WRITE_OPERATION/VOMS_EXTENSIONS/USER?DN_KEY=dn&FQANS_KEY=fquanFQANS_SEPARATORfquan
     * /RESOURCE/VERSION/path/READ_OPERATION/PLAIN/USER?DN_KEY=dn&FQANS_KEY=fquanFQANS_SEPARATORfquan
     * /RESOURCE/VERSION/path/WRITE_OPERATION/PLAIN/USER?DN_KEY=dn&FQANS_KEY=fquanFQANS_SEPARATORfquan
     * 
     * Response sample
     * true
     */
}
