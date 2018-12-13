/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.cincamimisconversor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import org.ciedayap.cincamimis.LikelihoodDistributionException;
import org.ciedayap.cincamimis.Quantitative;
import org.ciedayap.utils.QuantitativeUtils;
import org.ciedayap.utils.bihash.AttributeCurrentState;
import org.ciedayap.utils.bihash.Bihash;
import org.ciedayap.utils.bihash.BihashException;

/**
 * It encapsulates the Bihash related to the Project definition jointly with the 
 * Stream which will contain the Translated Tuples from the cincamimis message
 * @author Mario Diván
 */
public class EntityRoute {
    private final Bihash metadata;
    private final RowVectorStream stream;
    private final ConcurrentHashMap<Integer,AttributeCurrentState> attributeLastKnownState;
    
    public EntityRoute(Bihash m,Integer initialCapacity, boolean fifo,ConcurrentHashMap<Integer,AttributeCurrentState> map) throws Exception
    {
        if(m==null) throw new Exception("The Bihash or the RowVectorStream instances are null");
        if(map==null) throw new Exception("The Attribute Last Known State Map is null");
        if(initialCapacity==null || initialCapacity<=0) throw new Exception("The initialCapacity is so small");
        this.metadata=m;
        this.stream=RowVectorStream.create(initialCapacity, fifo);
        this.attributeLastKnownState=map;
    }
       
    /**
     * The default factory method
     * @param m The metadata related to the measurement project
     * @param initialCapacity The initial capacity for the stream
     * @param fifo The accessing order
     * @return A new EntityRoute instance
     * @throws Exception  It is raised when the metadata are not defined, or the initial capacity is invalid.
     */
    public synchronized static EntityRoute create(Bihash m,Integer initialCapacity, boolean fifo) throws Exception
    {
        return new EntityRoute(m,initialCapacity,fifo, new ConcurrentHashMap(m.getOrderedPositions().size()));
    }
    
    /**
     * It indicates whether this route corresponds to a given entity or not
     * @param id The ID entity to be verified
     * @return TRUE when the route is related to the entity, FALSE otherwise
     */
    public boolean isEntityPresent(String id)
    {
        return (getMetadata()==null)?false:getMetadata().isEntityPresent(id);
    }

    /**
     * @return the metadata
     */
    public Bihash getMetadata() {
        return metadata;
    }

    /**
     * It adds a new item in the corresponding stream. In addition, it updates the last known state for the corresponding attribute and replaces
     * the null values in the row with the last known value from each attribute.
     * @param item The row to be incorporated
     * @return the stream
     * @throws org.ciedayap.utils.bihash.BihashException It is raised when the state monitoring structure could not be created
     * @throws java.lang.InterruptedException It happens when the value could not be added to the list of the last values
     * @throws org.ciedayap.cincamimis.LikelihoodDistributionException It is raised when there exist a problem in the calculating of the  expectation mathematical in the estimated quantitative values
     */
    public synchronized boolean addToStream(RowVector item) throws BihashException, InterruptedException, LikelihoodDistributionException {
        if(item==null) 
        {            
            return false;
        }
        if(!item.isConsistent()) 
        {
            return false;
        }
        
        //Updating the last known State while checking each value (The nulls are replaced when there exist at least one value in the last known state)        
        ArrayList<Attribute> attIDList=item.getAttributesAsAList();
        for(Attribute att:attIDList)
        {
            Object ptr=item.getValue(att);
            
            if(ptr!=null && ptr instanceof Quantitative)
            {
                Quantitative value=(Quantitative)ptr; 
                if(QuantitativeUtils.isConsistent(value))
                {//The row has a value for the attribute
                    if(attributeLastKnownState.containsKey(att.hashCode()))
                    {//There is a previous tracing
                        AttributeCurrentState acs=attributeLastKnownState.get(att.hashCode());
                        acs.addKeepingLast(value);//Incorporates the value, update the state and compute min, max and the mean
                    }
                    else
                    {//New
                        AttributeCurrentState acs=AttributeCurrentState.create(metadata.getAttributeFromPD(att.getName()), 10);
                        acs.addKeepingLast(value);//Incorporates the value, update the state and compute min, max and the mean
                        attributeLastKnownState.put(att.hashCode(), acs);
                    }
                }
                else
                {//The row has not a value for the attribute
                    //Fill it with the last known
                    if(attributeLastKnownState.containsKey(att.hashCode()))
                    {
                        AttributeCurrentState acs=attributeLastKnownState.get(att.hashCode());
                        item.update(att, acs.getLastAddedValue());
                    }
                    else
                    {
                        AttributeCurrentState acs=attributeLastKnownState.get(att.hashCode());
                        item.update(att, Quantitative.factoryDeterministicQuantitativeMeasure(BigDecimal.ZERO));                        
                    }
                }
            }
            else
            {//ptr is null
                if(attributeLastKnownState.containsKey(att.hashCode()))
                {
                    AttributeCurrentState acs=attributeLastKnownState.get(att.hashCode());
                    item.update(att, acs.getLastAddedValue());
                }
                else
                {
                    AttributeCurrentState acs=attributeLastKnownState.get(att.hashCode());
                    item.update(att, Quantitative.factoryDeterministicQuantitativeMeasure(BigDecimal.ZERO));                        
                }                
            }
        }
        
        return getStream().addRowVector(item);
    }
    
    /**
     * It returns the current state for a given attribute
     * @param hashcode The hashcode related to the given attribute
     * @return The attribute current state instance, null otherwise
     */
    public synchronized AttributeCurrentState getCurrentState(int hashcode)
    {
        return attributeLastKnownState.get(hashcode);
    }

    /**
     * @return the stream
     */
    public RowVectorStream getStream() {
        return stream;
    }
    
    
}
