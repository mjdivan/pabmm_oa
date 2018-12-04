/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.utils.bihash;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import org.ciedayap.pabmm.pd.requirements.Attributes;
import org.ciedayap.pabmm.pd.requirements.Attribute;
import org.ciedayap.utils.StringUtils;

/**
 * It is responsible for the implementation of the bihash strategy from Attribute instances.
 * 
 * @author Mario Diván
 * @version 1.0
 */
public class Bihash implements Serializable{
    /**
     * It keeps in memory the relationship between vector position and the attribute
     */
    private final java.util.concurrent.ConcurrentHashMap<Integer, Attribute> idxAtt;
    /**
     * It keeps in memory the relationship between the attributeID (String) and the vector position
     */
    private final java.util.concurrent.ConcurrentHashMap<String, Integer> AttIDidx;
    /**
     * It represents the last assigned position to an attribute in the vector (i.e. the current dimension)
     */
    private Integer lastAssignedPosition;
   
    /**
     * Constructor which receives the ConcurrentHashMap to be used. In case the HashMap is not empty,
     * they are cleaned;
     * @param pidxAtt It is associated with the relationship Position-Attribute
     * @param pAttIDidx It is associated with the relationship Attribute-Position
     * @throws BihashException An exception is thrown when some ConcurrentHashMap is null
     */
    public Bihash(ConcurrentHashMap pidxAtt,ConcurrentHashMap pAttIDidx) throws BihashException
    {
        if(pidxAtt==null) throw new BihashException("ConcurrentHashMap for position-Attribute is null");
        else pidxAtt.clear();
        if(pAttIDidx==null) throw new BihashException("ConcurrentHashMap for Attribute-position is null");
        else pAttIDidx.clear();
        this.idxAtt=pidxAtt;
        this.AttIDidx=pAttIDidx;
        
        lastAssignedPosition=-1;
    }
    
    /**
     * It indicates that the ConcurrentHashMap are availables
     * @return TRUE when both ConcurrentHashMap are initialized, FALSE otherwise
     */
    protected boolean isReady()
    {
        return (idxAtt!=null && AttIDidx!=null);
    }
    
    /**
     * In takes the last assigned position, and returns the next integer, keeping in mind the answer
     * for the future.
     * @return -1 is returned when some ConcurrentHashMap is not available, otherwise the corresponding position for the vector is returned.
     */
    protected synchronized Integer getNextAvailablePosition()
    {
        if(idxAtt==null || AttIDidx==null) return -1;
        
        if(lastAssignedPosition<0)
        {
            lastAssignedPosition=0;            
        }
        else
        {
            lastAssignedPosition++;
        }
        
        return lastAssignedPosition;
    }
    
    /**
     * It incorporates the attributes in the Bihash
     * @param att The attribute to be included
     * @return The assigned position (even when it previously exists), otherwise it will return -1
     * @throws BihashException It happens when there are not hash maps or the new position is invalid
     */
    public synchronized Integer add(Attribute att) throws BihashException
    {
        if(!isReady()) throw new BihashException("The Concurrent Hash Maps are not availables");
        if(StringUtils.isEmpty(att.getID()) || StringUtils.isEmpty(att.getName()))
            throw new BihashException("The ID Attribute or its name is not available");
        
        //When there exists the key, it returns the assigned position
        if(AttIDidx.containsKey(att.getID())) return AttIDidx.get(att.getID());
        
        Integer newPosition=getNextAvailablePosition();
        if(newPosition<0) throw new BihashException("Invalid New Position");
        
        AttIDidx.put(att.getID(), newPosition);
        idxAtt.put(newPosition, att);
        
        return newPosition;
    }
    
    /**
     * It incorporates a set of attribute in the HashMap
     * @param list The list of attributes to be incorporates
     * @return An array of Integers indicating the assigned positions for each one
     * @throws BihashException It happens when there are not hash maps or the returned possition is invalid
     */
    public synchronized Integer[] addCollection(Attributes list) throws BihashException
    {
        if(list==null) throw new BihashException("The List is null");
        if(list.getCharacteristics()==null || list.getCharacteristics().size()<1)
            throw new BihashException("There not exist the columns in the Attributes");
        
        ArrayList<Attribute> attlist=list.getCharacteristics();
        Integer ret[]=new Integer[attlist.size()];
        for(int i=0;i<attlist.size();i++)
        {
            ret[i]=add(attlist.get(i));
        }
        
        return ret;
    }
       
    /**
     * It gets the attribute related to the position indicated by idx
     * @param idx The position related to a given attribute
     * @return The corresponding attribute when the position exists, null otherwise
     */
    public Attribute getAtIdx(Integer idx)
    {
        return idxAtt.get(idx);
    }
    
    /**
     * It gets the assigned position to the attribute
     * @param att The attribute to be queried
     * @return The assigned position when it exists, null otherwise
     */
    public Integer getPosition(Attribute att)
    {
        if(att==null) return null;
        
        return getPosition(att.getID());
    }
    
    /**
     * It gets the assigned position to the attribute ID
     * 
     * @param attID The attribute ID to be reached
     * @return The assigned position when it exists, null otherwise
     */
    public Integer getPosition(String attID)
    {
        if(StringUtils.isEmpty(attID)) return null;
        
        return AttIDidx.get(attID);
    }

    /**
     * It cleans the hash maps and restarts the lastAssignedPosition variable
     * @return TRUE when the hash maps were restarted (cleaned), FALSE otherwise
     */
    public synchronized boolean restart()
    {
       if(!isReady()) return false;
       
       idxAtt.clear();
       AttIDidx.clear();
       this.lastAssignedPosition=-1;
       
       return true;
    }
}
