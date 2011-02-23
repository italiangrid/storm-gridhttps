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

/**
 * @author Michele Dibenedetto
 */
public class MapperServlet extends HttpServlet
{


    /**
     * 
     */
    private static final long serialVersionUID = 293463225950571516L;


    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        String path = req.getParameter("path");
        String pathDecoded;
        try
        {
            pathDecoded = URLDecoder.decode(path, Configuration.MAPPER_SERVLET_ENCODING_SCHEME);
        }
        catch (UnsupportedEncodingException e)
        {
            System.err.println("Unable to decode parameters. UnsupportedEncodingException : " + e.getMessage());
            throw new ServletException("Unable to decode parameters. UnsupportedEncodingException : " + e.getMessage() + " Use " + Configuration.MAPPER_SERVLET_ENCODING_SCHEME + " encoding scheme");
        }
        System.out.println("Decoded filePath = " + pathDecoded);
        StorageArea SA = StorageAreaManager.getSA(pathDecoded);
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        
        out.print(Configuration.SERVICE_PATH + getStfnPath(pathDecoded, SA));
    }

    private String getStfnPath(String path, StorageArea SA)
    {
        return SA.getStfnRoot() + path.substring(SA.getFSRoot().length(), path.length());
    }


    public String getServletInfo()
    {
        return "A servlet providing a mapping between a fisical file path and it\'s relative URL";
    }
}
