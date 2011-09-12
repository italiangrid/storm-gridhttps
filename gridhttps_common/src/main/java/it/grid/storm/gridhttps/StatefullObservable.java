package it.grid.storm.gridhttps;

import java.util.Observable;



/**
 * @author Michele Dibenedetto
 *
 */
public abstract class StatefullObservable extends Observable
{
    /**
     * @return
     */
    public abstract Object getState();
}
