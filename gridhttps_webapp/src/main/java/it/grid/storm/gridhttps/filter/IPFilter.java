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
import it.grid.storm.gridhttps.StatefullObservable;
import it.grid.storm.gridhttps.WaitingStateObserver;
import it.grid.storm.gridhttps.Configuration.ConfigurationParameters;
import it.grid.storm.gridhttps.log.LoggerManager;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;

/**
 * @author Michele Dibenedetto
 */
public class IPFilter extends WaitingStateObserver implements Filter
{
    
    private static Logger log = LoggerManager.getLogger(IPFilter.class);
    
    private static String stormBackendIP = null; 
    
    /**
     * @param observable
     */
    public IPFilter(StatefullObservable observable)
    {
        super(observable);
    }

    /**
     * 
     */
    public IPFilter()
    {
        this(Configuration.getInstance());
    }
    
    /* (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy()
    {
        log.info("Servlet filter destroied");
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        log.debug("Filtering servlet request from host " + request.getRemoteHost());
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
            log.error("Received non HTTP servlet request. Class is : " + request.getClass());
            throw new ServletException("Protocol not supported. Use HTTP(S)");
        }
        log.debug("Remote host calling is : " + HTTPRequest.getRemoteAddr());
        if(stormBackendIP == null)
        {
            log.debug("Obtaining the StoRM backend IP from Configuration");
            getObservableReady();
            ConfigurationParameters stormBackendParameters = (ConfigurationParameters) ((StatefullObservable)observable).getState();
            stormBackendIP = stormBackendParameters.getStormBackendIP();
        }
        if(!stormBackendIP.equals(HTTPRequest.getRemoteAddr()))
        {
            log.warn("Servlet called by a machine that is not configured as a known storm backend : " + HTTPRequest.getRemoteAddr());
            HTTPResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not authorized to access this service from this host");
            return;    
        }
        log.debug("Requesting host is StoRM backend");
        chain.doFilter(request, response);
    }
    

    /* (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        log.info("Servlet filter initialized correctly on " + filterConfig.getServletContext().getServletContextName() + " context");
    }

    @Override
    protected void update(Object state)
    {
        // Nothing to do
        log.debug("Received an update notification from tyhe configuration. Nothing to do");
        
    }
}
