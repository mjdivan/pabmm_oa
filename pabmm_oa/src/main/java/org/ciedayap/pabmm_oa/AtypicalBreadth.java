/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.pabmm_oa;

import org.ciedayap.cincamimisconversor.RowVector;

/**
 * It represents the atypical breadth in memory
 * 
 * @author Mario Diván
 * @version 1.0
 */
public class AtypicalBreadth {
    private final RowVector starting;
    private final RowVector ending;
    private final double distance;
    private final double definedThreshold;
    
    public AtypicalBreadth(RowVector starting,RowVector ending,double dist, double threshold)
    {
        this.starting=starting;
        this.ending=ending;
        distance=dist;
        definedThreshold=threshold;        
    }
    
    public static AtypicalBreadth create(RowVector starting,RowVector ending,double dist, double threshold)
    {
        return new AtypicalBreadth(starting,ending,dist,threshold);
    }

    /**
     * @return the starting
     */
    public RowVector getStarting() {
        return starting;
    }

    /**
     * @return the ending
     */
    public RowVector getEnding() {
        return ending;
    }

    /**
     * @return the distance
     */
    public double getDistance() {
        return distance;
    }

    /**
     * @return the definedThreshold
     */
    public double getDefinedThreshold() {
        return definedThreshold;
    }
}
