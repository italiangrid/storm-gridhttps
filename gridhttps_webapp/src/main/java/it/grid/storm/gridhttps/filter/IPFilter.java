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
package it.grid.storm.gridhttps.filter;


import it.grid.storm.gridhttps.Configuration;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Michele Dibenedetto
 */
public class IPFilter implements Filter
{

    @Override
    public void destroy()
    {
        
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        
        HttpServletResponse HTTPResponse = null;
        HttpServletRequest HTTPRequest = null;
        if (HttpServletRequest.class.isAssignableFrom(request.getClass())
                && HttpServletResponse.class.isAssignableFrom(response.getClass()))
        {
            HTTPRequest = (HttpServletRequest) request;
            HTTPResponse = (HttpServletResponse) response;
        }
        else
        {
            System.err.println("ERROR: received non HTTP request. Class is : " + request.getClass());
            throw new ServletException("Protocol not supported. Use HTTP(S)");
        }
        System.out.println("Remote host calling is : " + HTTPRequest.getRemoteAddr());
        if(!Configuration.getStormBackendIP().equals(HTTPRequest.getRemoteAddr()))
        {
            System.err.println("WARN: Service called by a machine tht is not configured storm backend : " + HTTPRequest.getRemoteAddr());
            HTTPResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not authorized to access this service");
            return;    
        }
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

   
}
