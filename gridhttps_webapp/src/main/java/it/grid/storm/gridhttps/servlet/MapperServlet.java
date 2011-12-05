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
package it.grid.storm.gridhttps.servlet;

import it.grid.storm.gridhttps.Configuration;
import it.grid.storm.gridhttps.log.LoggerManager;
import it.grid.storm.gridhttps.storagearea.StorageArea;
import it.grid.storm.gridhttps.storagearea.StorageAreaManager;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 * @author Michele Dibenedetto
 */
public class MapperServlet extends HttpServlet
{

    private static Logger log = LoggerManager.getLogger(MapperServlet.class);
    /**
     * 
     */
    private static final long serialVersionUID = 293463225950571516L;
    private static final String PATH_PARAMETER_KEY = "path";


    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        log.info("Serving a get request");
        String path = req.getParameter(PATH_PARAMETER_KEY);
        String pathDecoded;
        try
        {
            pathDecoded = URLDecoder.decode(path, Configuration.MAPPER_SERVLET_ENCODING_SCHEME);
        }
        catch (UnsupportedEncodingException e)
        {
            log.error("Unable to decode " + PATH_PARAMETER_KEY + " parameter. UnsupportedEncodingException : " + e.getMessage());
            throw new ServletException("Unable to decode " + PATH_PARAMETER_KEY + " parameter", e);
        }
        log.debug("Decoded filePath = " + pathDecoded + " . Retrieving matching StorageArea");
        StorageArea SA = null;
        try
        {
            SA = StorageAreaManager.getMatchingSA(pathDecoded);
        }catch(IllegalArgumentException e)
        {
            log.error("Unable to get matching SA for path " + pathDecoded + ". IllegalArgumentException : " + e.getMessage());
            throw new ServletException("Unable to get matching SA for path " + pathDecoded , e);
        }
        catch(IllegalStateException e)
        {
            log.error("Unable to get matching SA for path " + pathDecoded + ". IllegalStateException : " + e.getMessage());
            throw new ServletException("Unable to get matching SA for path " + pathDecoded , e);
        }
        if(SA == null)
        {
            log.error("No matching StorageArea found for path \'" + pathDecoded + "\' Unable to build http(s) relative path");
            throw new ServletException("No matching StorageArea found for the provided path");
        }
        res.setContentType("text/html");
        PrintWriter out;
        try
        {
            out = res.getWriter();
        }catch (IOException e)
        {
            log.error("Unable to obtain the PrintWriter for the response. IOException: " + e.getMessage());
            throw e;
        }
        String relativeUrl = Configuration.SERVICE_PATH + getStfnPath(pathDecoded, SA);
        log.debug("Writing in the response the relative URL : " + relativeUrl);
        out.print(relativeUrl);
    }

    /**
     * Removes from the given path the FSRoot and appends on its head the StfnRoot
     *  
     * @param path
     * @param SA
     * @return 
     */
    private String getStfnPath(String path, StorageArea SA)
    {
        log.debug("Building StfnPath for path " + path + " in StorageArea " + SA.getName());
        String Stfnpath = SA.getStfnRoot() + path.substring(SA.getFSRoot().length(), path.length());
        log.debug("Stfnpath is \'" + Stfnpath + "\'");
        return Stfnpath;
    }


    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#getServletInfo()
     */
    public String getServletInfo()
    {
        return "A servlet providing a mapping between a fisical file path and it\'s relative URL";
    }
}
