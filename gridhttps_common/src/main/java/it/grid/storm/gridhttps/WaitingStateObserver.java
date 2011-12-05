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
package it.grid.storm.gridhttps;

import java.util.Observable;
import it.grid.storm.gridhttps.log.LoggerManager;
import org.apache.log4j.Logger;



/**
 * @author Michele Dibenedetto
 *
 */
public abstract class WaitingStateObserver extends StateObserver
{

    /**
     * 
     */
    private static Logger log = LoggerManager.getLogger(WaitingStateObserver.class);
    
    /**
     * 
     */
    private final Object lock = new Object();
    
    
    /**
     * @param observable
     */
    public WaitingStateObserver(StatefullObservable observable)
    {
        super(observable);
    }
    
    
    /**
     * 
     */
    protected void getObservableReady()
    {
        log.debug("Getting observable ready to proceed");
        if(!observableReady())
        {
            log.debug("Observable not ready, waiting for updates");
            waitForObservableInitialized();
        }
    }
    
    /**
     * 
     */
    private void waitForObservableInitialized()
    {
        synchronized(lock)
        {
            if(!observableReady())
            {
                try
                {
                    log.info("Configuration not yet initialized, registered as observer");
                    lock.wait();
                }
                catch (InterruptedException e)
                {
                    log.error("Waiting on the initialization lock interrupted abnormally. InterruptedException: " + e.getMessage());
                    return;
                }
            }
            log.info("Configuration initialized");
        }
    }


    /* (non-Javadoc)
     * @see it.grid.storm.gridhttps.SympleObserver#notifyEvent()
     */
    @Override
    public void update(Observable observable, Object arg)
    {
        synchronized(lock)
        {
            lock.notify();
        }
        super.update(observable, arg);
    }
}
