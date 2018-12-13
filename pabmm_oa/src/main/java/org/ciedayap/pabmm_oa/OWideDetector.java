/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.pabmm_oa;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.math3.linear.RealMatrix;
import org.ciedayap.cincamimisconversor.RowVector;
import org.ciedayap.cincamimisconversor.RowVectorStream;
import org.ciedayap.distances.DistanceCalculator;

/**
 * It is responsible for the outlier detection based on the Clustream algorithm idea
 * 
 * @author Mario Diván
 * @version 1.0
 */
public class OWideDetector implements Runnable{
    private boolean activated=true;
    /**
     * The distance calculator instance
     */
    public final DistanceCalculator dc;
    /**
     * The Threshold related to the maximum difference tolerated in the given distance for the measurning
     */
    public final double threshold;    
    /**
     * The DateTime related to the first processed record
     */
    public java.time.ZonedDateTime starting;
    /**
     * The DateTime related to the last processed record
     */
    public java.time.ZonedDateTime ending;            
    /**
     * The processed records
     */
    public final ArrayBlockingQueue<RowVector> records;
    /**
     * The alarms associated with the atypical breadth
     */
    public final ArrayBlockingQueue<AtypicalBreadth> alarms;
    
    private final ConcurrentHashMap comparison=new ConcurrentHashMap<Integer,Integer>();
    /**
     * It represents the source used for continuously analyzing the values
     */
    private final RowVectorStream source;    
    private final ConcurrentHashMap<Integer,Integer> processed;
    /**
     * Constructor which establishes the realtion between the data source and the outlier algorithm
     * @param source The data source
     * @param threshold The threshold related to the distance calculation for determining the belonging to a given group
     * @param queueCapacity The max capacity for the queue in memory related to the outlier wide detector
     * @throws java.lang.Exception It is raised by the DistanceCalculator when a invalid distance is indicated
     */
    public OWideDetector(RowVectorStream source,double threshold,int queueCapacity) throws Exception
    {
  
        this.threshold=(threshold<0.1)?0.1:threshold;
  
        this.source=source;
        this.records=new ArrayBlockingQueue<>(queueCapacity);
        this.alarms=new ArrayBlockingQueue<>(queueCapacity);
        
        this.dc=new DistanceCalculator();
        this.dc.setSelectedDistance(DistanceCalculator.DISTANCE_MAHALANOBIS);  
        processed=new ConcurrentHashMap();
    }

     /**
     * Default Factory Method which establishes the realtion between the data source and the outlier algorithm
     * @param source The data source
     * @param threshold The threshold related to the distance calculation for determining the belonging to a given group
     * @param queueCapacity The max capacity for the queue in memory related to the outlier wide detector
     * @return A new instance OWideDetector
     * @throws java.lang.Exception It is raised by the DistanceCalculator when a invalid distance is indicated
     */
    public static synchronized OWideDetector create(RowVectorStream source,double threshold, int queueCapacity) throws Exception
    {
        return new OWideDetector(source,threshold,queueCapacity);        
    }
    
    @Override
    public synchronized void run() {
        while(activated)
        {            
            for(int i=0;i<source.currentSize();i++)
            {
                processItem(source.getRowVector(i));                
            }                        
        }
        
    }
        
    /**
     * It incorporates the clusterItem to a MicroCluster XOR the Outlier list
     * @param ci The item to be added
     * @return TRUE when the item was added, FALSE otherwise
     */
    protected synchronized boolean addTo(RowVector ci)
    {
        if(ci==null ||!ci.isConsistent()) return false;
        if(processed.containsKey(ci.hashCode())) return true;
        
        if(this.records.isEmpty())
        {
            records.add(ci);
            processed.put(ci.hashCode(), 1);
            return true;
        }
        
        RealMatrix covariance=source.getCovarianceMatrix();
        if(covariance==null)
        {
            return false;
        }
        
        double maxDistance=-1;
        java.time.ZonedDateTime max_init=null;
        java.time.ZonedDateTime max_end=null;
        RowVector init=null;        
        
        Iterator<RowVector> iterator=records.iterator();
        while(iterator.hasNext())
        {
            RowVector rv=iterator.next();
            double distance=dc.calculateDistance(covariance, ci, rv);
            if(maxDistance==-1 || maxDistance<distance)
            {
                maxDistance=distance;
                max_init=rv.getDatetime();
                max_end=ci.getDatetime();
                init=rv;                
            }
        }
            
        if(!records.offer(ci))
        {
            RowVector removed=records.poll();//it removes the first element (the oldest)
            processed.remove(removed.hashCode());
            
            records.offer(ci);//Then, it adds the new element to the end of the queue
            processed.put(ci.hashCode(), 1);
        }
        
        if(maxDistance>threshold && init!=null)
        {
            if(!comparison.containsKey(init.hashCode()+ci.hashCode()))
            {
                AtypicalBreadth alarm=AtypicalBreadth.create(init, ci, maxDistance, threshold);
                if(!alarms.offer(alarm))
                {
                    alarms.poll();
                    alarms.offer(alarm);
                }
                
                if(comparison.size()>1000) comparison.clear();//Restart the ConcurrentHashMap
                comparison.put(init.hashCode()+ci.hashCode(), 1);
            }
            //System.out.println("[Outlier Wide Detected] Between "+max_init+" and "+max_end+" the distance was "+
            //        maxDistance+" upper than the threshold ("+threshold+") ");            
        }
        
        return true;
    }
        
    protected synchronized boolean processItem(RowVector rv)            
    {
        if(rv==null || !rv.isConsistent())
        {
            return false;
        }

        return addTo(rv);
    }
    
    public void print(boolean detailed)
    {
        System.out.println("Last "+alarms.size()+" Detected...");
        Iterator<AtypicalBreadth> it=alarms.iterator();
        int i=0;
        
        double max=-1;
        while(it.hasNext())
        {
            AtypicalBreadth ab=it.next();
            if(detailed)
            {
                System.out.println("***Case "+i+"***");
                System.out.println("Item 1:"+ab.getStarting().toString()+" "+ab.getStarting().getDatetime());
                System.out.println("Item 2:"+ab.getEnding().toString()+" "+ab.getEnding().getDatetime());
                System.out.println("HashCode: "+(ab.getStarting().hashCode()+ab.getEnding().hashCode()));
                System.out.println("Computed Distance: "+ab.getDistance()+" Defined Threshold: "+ab.getDefinedThreshold());
            }
            else
            {
                
            }
            if(max==-1 || ab.getDistance()>max) max=ab.getDistance();
            i++;
        }
        
        System.out.println("Max Detected Difference: "+max);
        System.out.println("Remaining Processed: "+this.processed.size());
        System.out.println("Comparisons in Threshold: "+this.comparison.size());
        
    }

    /**
     * @return the activated
     */
    public boolean isActivated() 
    {
        return activated;
    }

    /**
     * @param activated the activated to set
     */
    public void setActivated(boolean activated) 
    {
        this.activated = activated;
    }    
}
