/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.utils.bihash;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import org.ciedayap.cincamimis.LikelihoodDistributionException;
import org.ciedayap.cincamimis.Quantitative;
import org.ciedayap.pabmm.pd.requirements.Attribute;
import org.ciedayap.utils.QuantitativeUtils;

/**
 * It is responsible for keeping in memory the last processed values for the metric associated with the attribute
 * @author Mario Diván
 * @version 1.0
 */
public class AttributeCurrentState {
    /**
     * The target attribute
     */
    private final Attribute attribute;
    /**
     * The last values following the FIFO order
     */
    private final ArrayBlockingQueue<Quantitative> lastValues;
    /**
     * The maximum detected value at the present from its start time
     */
    private BigDecimal maxValue;
    /**
     * The maximum detected value at the present from its start time
     */
    private BigDecimal minValue;
    /**
     * The last arithmetic mean computed at the last adding
     */
    private BigDecimal lastArithmeticMean;
    /**
     * The last added value in the queue
     */
    private Quantitative lastAddedValue;
    
    /**
     * It creates a new instance indicating the attribute and the list in which keeps the last received values in memory
     * @param att The target attribute
     * @param list the list in which keep the last values in memory
     * @throws BihashException It is raised when the attribute or the list are null 
     */
    public AttributeCurrentState(Attribute att, ArrayBlockingQueue<Quantitative> list) throws BihashException
    {
        if(att==null || list==null) throw new BihashException("The Attribute or QuantitativeList are null");
        attribute=att;
        lastValues=list;
    }
    
    /**
     * Default factory method
     * @param at The attribute to monitor
     * @param capacity The capacity related to the last values for the metric values of the attribute
     * @return A new instance for tracing its state
     * @throws BihashException It is raised when the attribute or the list are null
     */
    public static synchronized AttributeCurrentState create(Attribute at, int capacity) throws BihashException
    {
        return new AttributeCurrentState(at,new ArrayBlockingQueue<>((capacity<10)?10:capacity,true));
    }
    
    public synchronized boolean addKeepingLast(Quantitative value) throws InterruptedException, LikelihoodDistributionException
    {
        if(value==null) return false;
        if(!QuantitativeUtils.isConsistent(value)) return false;
                
        boolean ret=lastValues.offer(value);
        if(!ret)
        {
            lastValues.take();
            ret=lastValues.offer(value);            
        }
        
        if(ret)
        {
            BigDecimal myValue=(QuantitativeUtils.isEstimated(value))?QuantitativeUtils.mathematicalExpectation(value):value.getDeterministicValue();
            if(this.getMaxValue()!=null)
            { 
                if(myValue.compareTo(this.getMaxValue())>0) this.maxValue=myValue;
            }
            else
                this.maxValue=myValue;
         
            if(this.getMinValue()!=null)
            {
                if(myValue.compareTo(this.getMinValue())<0) this.minValue=myValue;
            }
            else
                this.minValue=myValue;
                                   
            if(this.lastArithmeticMean!=null)
            {
                double acu=lastValues.stream()
                        .mapToDouble((v)->{
                            BigDecimal val=BigDecimal.ZERO;
                            boolean error=false;
                            try {
                                val=(QuantitativeUtils.isEstimated(v))?QuantitativeUtils.mathematicalExpectation(v):v.getDeterministicValue();
                            } catch (LikelihoodDistributionException ex) {
                                error=true;
                            }

                            if(error) return 0.0;
                            return val.doubleValue();
                        })
                        .sum();               

                double count=lastValues.stream()
                        .filter((v)->{
                            BigDecimal val=BigDecimal.ZERO;
                            boolean error=false;
                            try {
                                val=(QuantitativeUtils.isEstimated(v))?QuantitativeUtils.mathematicalExpectation(v):v.getDeterministicValue();
                            } catch (LikelihoodDistributionException ex) {
                                error=true;
                            }

                            return error;
                        })  
                        .count();    

                if(count>0)
                    this.lastArithmeticMean=BigDecimal.valueOf(acu/count);
            }
            else this.lastArithmeticMean=myValue;
                
            this.lastAddedValue=value;            
        }
        
        return ret;
    }
    
    /**
     * It gives the first element from the queue without remove the element from the list
     * @return The oldest value for the defined capacity
     */
    public synchronized Quantitative firstValueAvailable()
    {
        if(lastValues==null) return null;
        
        return lastValues.peek();
    }
   
    /**
     * It removes all the elements from the queue.
     */
    public synchronized void clear()
    {
         if(lastValues==null) return;

         lastValues.clear();
    }
    
    /**
     * It gives the first element from the queue, removing it.
     * @return The first element from the queue, removing it. Additionally, it returns
     * null if the queue is empty.
     */
    public synchronized Quantitative firstAvailableandRemove() 
    {
        if(lastValues==null) return null;
        if(lastValues.isEmpty()) return null;
        
        Quantitative element=lastValues.poll();        
        
        return element;    
    }
    
    /**
     * It returns the remaining capacity related to the queue
     * @return The remaining capacity of the queue
     * @throws BihashException This exception is raised when there is not an initialized queue
     */
    public  int remainingCapacity() throws BihashException
    {
        if(lastValues==null) throw new BihashException("LastValues Queue is not found");
        
        return lastValues.remainingCapacity();
    }

    /**
     * It returns the quantity of elements in the queue
     * @return The quantity of elements in the queue
     */
    public  int size() 
    {
        if(lastValues==null) return 0;
        
        return lastValues.size();
    }    
    
    
    /**
     * It indicates whether the queue is null or not.
     * @return TRUE the queue is null, FALSE otherwise.
     */
    public boolean isEmpty()
    {
        if(lastValues==null) return true;
        
        return lastValues.isEmpty();
    }

    /**
     * @return the maxValue
     */
    public BigDecimal getMaxValue() {
        return maxValue;
    }

    /**
     * @return the minValue
     */
    public BigDecimal getMinValue() {
        return minValue;
    }

    /**
     * @return the lastArithmeticMean
     */
    public BigDecimal getLastArithmeticMean() {
        return lastArithmeticMean;
    }

    /**
     * @return the lastAddedValue
     */
    public Quantitative getLastAddedValue() {
        return lastAddedValue;
    }
    
    /**
     * It returns the last added values for the defined capacity in the proper order
     * @return The last values in the proper order, NULL when there is not list.
     */
    public Iterator<Quantitative> lastOrderedValues()
    {
        if(lastValues==null) return null;
        
        return lastValues.iterator();
    }
}
