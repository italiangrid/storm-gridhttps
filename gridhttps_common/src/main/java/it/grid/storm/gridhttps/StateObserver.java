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

import it.grid.storm.gridhttps.log.LoggerManager;
import java.util.Observable;
import java.util.Observer;
import org.apache.log4j.Logger;


/**
 * @author Michele Dibenedetto
 *
 */
public abstract class StateObserver implements Observer
{

    /**
     * 
     */
    private static Logger log = LoggerManager.getLogger(StateObserver.class);
    
    protected StatefullObservable observable = null;
    /**
     * @return
     */
    
    public StateObserver(StatefullObservable observable)
    {
        this.observable = observable;
    }
    
    @SuppressWarnings("unused")
    private StateObserver()
    {
        //forbidden!
    }
    
    /**
     * @return
     */
    protected boolean observableReady()
    {
        boolean response = false; 
        if(observable.getState() != null)
        {
            log.debug("Observable state is not null, ready");
            response = true;
        }
        else
        {
            if(observable.hasChanged())
            {
                log.debug("Observable is marched as changed, ready");
                response = true;
            }
            else
            {
                log.debug("Observable not changed registering as observer");
                observable.addObserver(this);
                if(observable.hasChanged())
                {
                    log.debug("Observable is marched as changed, this is an interliving trick");
                    Object state = observable.getState();
                    if(state == null)
                    {
                        log.warn("StatefullObservable marked as changed but its state is still null. Nothing to do. Unexpected situation!");
                        response = false;
                    }
                    else
                    {
                        log.debug("Removing from observers");
                        observable.deleteObserver(this);
                        response = true;                        
                    }
                }
            }
        }
        return response;
    }
    
    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable observable, Object arg)
    {
        log.debug("Receved event notification");
        if(!observable.equals(this.observable))
        {
            log.warn("Received a notification from an observable that is not the registered one. Unexpected situation! Class: " + observable.getClass());
            return;
        }
        Object state = ((StatefullObservable)observable).getState();
        if(state == null)
        {
            log.warn("Received an update from the a StatefullObservable but its state is still null. Nothing to do. Unexpected situation!");
            return;
        }
        this.update(state);
        observable.deleteObserver(this);
    }

    /**
     * @param state
     */
    protected abstract void update(Object state);
}
