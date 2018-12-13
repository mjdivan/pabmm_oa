/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.utils.bihash;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import org.ciedayap.cincamimisconversor.ColumnFamily;
import org.ciedayap.cincamimisconversor.Tuple;
import org.ciedayap.pabmm.pd.requirements.Attributes;
import org.ciedayap.pabmm.pd.requirements.Attribute;
import org.ciedayap.pabmm.pd.requirements.Entities;
import org.ciedayap.pabmm.pd.requirements.Entity;
import org.ciedayap.utils.StringUtils;

/**
 * It is responsible for the implementation of the bihash strategy from Attribute instances.
 * It keeps in memory the part of the project definitions related to the attributes and context properties.
 * 
 * @author Mario Diván
 * @version 1.0
 */
public class Bihash implements Serializable{
    /**
     * The Set of entities related to the Project
     */
    private final java.util.concurrent.ConcurrentHashMap<String, Entity> entities;
    /**
     * The ID project
     */
    private String IDProject;
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
     * It keeps in memory the relationships between each metric and its attribute definition
     */
    private final java.util.concurrent.ConcurrentHashMap<String,Attribute> metricAttribute;
   
    /**
     * Constructor which receives the ConcurrentHashMap to be used.In case the HashMap is not empty,
 they are cleaned;
     * @param pidxAtt It is associated with the relationship Position-Attribute
     * @param pAttIDidx It is associated with the relationship Attribute-Position
     * @param pentities It is associated with the set of entities of the project
     * @param pmetrics It is associated with the set of metrics which quantify a given attribute
     * @throws BihashException An exception is thrown when some ConcurrentHashMap is null
     */
    public Bihash(ConcurrentHashMap pidxAtt,ConcurrentHashMap pAttIDidx,ConcurrentHashMap pentities,ConcurrentHashMap pmetrics) throws BihashException
    {
        if(pidxAtt==null) throw new BihashException("ConcurrentHashMap for position-Attribute is null");
        else pidxAtt.clear();
        if(pAttIDidx==null) throw new BihashException("ConcurrentHashMap for Attribute-position is null");
        else pAttIDidx.clear();
        if(pentities==null) throw new BihashException("ConcurrentHashMap for entities is null");
        else pentities.clear();
        if(pmetrics==null) throw new BihashException("ConcurrentHashMap for metrics is null");
        else pmetrics.clear();
        
        this.idxAtt=pidxAtt;
        this.AttIDidx=pAttIDidx;
        this.entities=pentities;
        this.metricAttribute=pmetrics;
        
        lastAssignedPosition=-1;
    }
    
    /**
     * It is a default factory method
     * @param size The size associated with the hash map
     * @return The new bihash instance
     * @throws BihashException It is raised when the size is invalid (i.e. null or under zero)
     */
    public synchronized static Bihash create(Integer size) throws BihashException
    {
        if(size==null || size<0) throw new BihashException("Invalida Size");
        
        if(size<10) size=10;
        
        return new Bihash(new ConcurrentHashMap(size),new ConcurrentHashMap(size),new ConcurrentHashMap(),new ConcurrentHashMap());        
    }
    
    /**
     * It indicates that the ConcurrentHashMap are availables
     * @return TRUE when both ConcurrentHashMap are initialized, FALSE otherwise
     */
    protected boolean isReady()
    {
        return (idxAtt!=null && AttIDidx!=null && entities!=null && metricAttribute!=null);
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
        if(att.getQuantifiedBy()==null || att.getQuantifiedBy().getRelated()==null || 
                att.getQuantifiedBy().getRelated().isEmpty())
            throw new BihashException("The attribute has not associated metrics");
        
        //When there exists the key, it returns the assigned position
        if(AttIDidx.containsKey(att.getID())) return AttIDidx.get(att.getID());
        
        Integer newPosition=getNextAvailablePosition();
        if(newPosition<0) throw new BihashException("Invalid New Position");
        
        AttIDidx.put(att.getID(), newPosition);                
        idxAtt.put(newPosition, att);
        
        /**
         * It incorporates each metricID for its association with the attribute metadata
         */
        att.getQuantifiedBy().getRelated().forEach((m) -> {
            metricAttribute.put(m.getIDmetric(), att);
        });
        
        return newPosition;
    }
    
    /**
     * It returns the attribute metadata from the informed metricID
     * @param metricID The metricID to which the attribute metadata is required
     * @return The attribute for the informed metricID in case of presence, null otherwise
     */
    public Attribute getAttributeForMetric(String metricID)
    {
        if(StringUtils.isEmpty(metricID)) return null;
        
        return metricAttribute.get(metricID);
    }
    
    /**
     * Using the metadata information this method is responsible for answering
     * whether the attribute is quantified for a given metric or not.
     * 
     * @param patt The attribute to be verified
     * @param metricID The mertic to be contrasted
     * @return TRUE when the attribute is quantified by the informed metric, FALSE
     * otherwise
     */
    public boolean isAttributeQuantifiedByThisMetric(String patt,String metricID)
    {
        if(StringUtils.isEmpty(patt)) return false;
        if(StringUtils.isEmpty(metricID)) return false;

        Attribute at=metricAttribute.get(metricID);
        if(at==null) return false;
                
        if(StringUtils.isEmpty(at.getID())) return false;
        
        return at.getID().equalsIgnoreCase(patt);
    }
    
    /**
     * It incorporates a collection of Attribute instances in the hash maps.
     * @param list The collection to be incorporated
     * @return An Integer Array with the respective positions of each attribute in the vector
     * @throws BihashException It is raised when the list to be processed is null or empty
     */
    public synchronized Integer[] addCollection(Collection<Attribute> list) throws BihashException
    {
        if(list==null || list.isEmpty()) throw new BihashException("The list is null or empty");
        Iterator<Attribute> it=list.iterator();
        Integer ret[]=new Integer[list.size()];
        int i=0;
        while(it!=null && it.hasNext())
        {
         ret[i]=add(it.next());
        }
        
        return ret;
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
    
    /**
     * It informs the total number of variable between attributes and context properties in the bihash table
     * @return The dimension for a given vector
     */
    public Long dimension()
    {
        if(!this.isReady()) return (long)0;
        
        return this.idxAtt.mappingCount();
    }
    
    /**
     * The min position related to the representative vector of each tuple related to the Project Definition.
     * When at least there exist one dimension, the min position is 0 (zero).
     * @return The min position is zero when the vector has upper or equal than one dimension, null otherwise.
     */
    public Long minPosition()
    {
        return (dimension()>0)?(long)0:null;
    }
    
    /**
     * The max position related to the representative vector of each tuple associated with the Project Definition.
     * It should be coincident with the (dimension()-1) because the initial position is zero
     * @return The max position for the representative vector of each "tuple" from the project definition
     */
    public long maxPosition()
    {
        return lastAssignedPosition;
    }
    
    /**
     * It returns the ordered vector possitions following the ascending order
     * @return The ordered keys from the Bihash instance, null otherwise
     */
    public ArrayList<Integer> getOrderedPositions()
    {
        if(!this.isReady()) return null;
        if(dimension()==0) return null;
        
        Enumeration<Integer> list=idxAtt.keys();
        ArrayList<Integer> keys=new ArrayList<>();
        while(list.hasMoreElements())
        { 
          keys.add(list.nextElement());
        }
        
        Collections.sort(keys);
        
        return keys;
    }

    /**
     * It incorporates the entities under monitoring in the ConcurrentHashMap for future queries
     * @param enl The entities to be incorporated
     * @return TRUE when the entities were incorporated, false otherwise
     * @throws org.ciedayap.utils.bihash.BihashException It is raised when the Bihash instance is not ready or there are some problem with the informed entities (e.g. The entity have not ID or name)
     */
    public boolean replaceEntities(Entities enl) throws BihashException
    {
        if(!this.isReady()) throw new BihashException("Bihash instance is not ready because some ConcurrentHashMap is not available");
        if(enl==null) throw new BihashException("Entities instance is null");
        if(enl.getEntitiesList()==null) throw new BihashException("Entities list is null");
        if(enl.getEntitiesList().isEmpty()) throw new BihashException("Entities list is empty");
        
        enl.getEntitiesList()
                .stream()
                .filter((e) -> (!(StringUtils.isEmpty(e.getID()) || StringUtils.isEmpty(e.getName()))))
                .forEachOrdered((e) -> {entities.put(e.getID(), e);});
        
        return true;
    }

    /**
     * It indicates whether the ID entity is monitored in this project or not
     * @param ID The ID entity to be queried
     * @return TRUE when the entity is present in this project, FALSE otherwise
     */
    public boolean isEntityPresent(String ID)
    {
        if(StringUtils.isEmpty(ID)) return false;
        if(entities==null) return false;
        
        return entities.containsKey(ID);
    }
    
    /**
     * It returns an enumeration with the ID Entities being monitored in the IDProject
     * @return A list with the ID Entities being monitored.
     */
    public Enumeration<String> getMonitoredIDEntities()
    {
        return (entities==null)?null:entities.keys();
    }
    
    /**
     * It returns the number of entities being actively monitored in this project
     * @return the number of entities being actively monitored in this project
     */
    public Integer getQuantityOfMonitoredEntities()
    {
        return (entities==null)?0:entities.size();
    }
    
    /**
     * @return the IDProject
     */
    public String getIDProject() {
        return IDProject;
    }

    /**
     * @param IDProject the IDProject to set
     */
    public void setIDProject(String IDProject) {
        this.IDProject = IDProject;
    }
    
    /**
     * It obtains the attributes under the organization required by the cincamimisConversor library
     * @return An Attributes instance in terms of the cincamimisConversor library
     * @throws BihashException It is raised when the Bihash instance is not ready or there are not positions.
     */
    public org.ciedayap.cincamimisconversor.Attributes getAttributesForConversor() throws BihashException
    {
        if(!this.isReady()) throw new BihashException("The ConcurrentHashMaps are not availables");
        ArrayList<Integer> positions=this.getOrderedPositions();
        if(positions==null) throw new BihashException("There are not positions associated with the ConcurrentHashMaps");
       
        org.ciedayap.cincamimisconversor.Attributes myAttributes=new org.ciedayap.cincamimisconversor.Attributes();
        
        ColumnFamily measurefamily=ColumnFamily.create("measurement");
        ColumnFamily contextfamily=ColumnFamily.create("context");

        positions.forEach((Integer position) -> 
        {
            Attribute at=this.getAtIdx(position);
            if(at!=null)
            {
                org.ciedayap.cincamimisconversor.Attribute dt=new org.ciedayap.cincamimisconversor.Attribute();

                if(at instanceof org.ciedayap.pabmm.pd.context.ContextProperty)
                    dt.setColumnFamily(contextfamily);
                else
                    dt.setColumnFamily(measurefamily);

                dt.setName(at.getID());            
                dt.setKind(BigDecimal.class);
                dt.setOrder((long)position);
                dt.setQuantitative(true);
                
                myAttributes.add(dt);
            }                       
        });       

        return myAttributes;
    }
    
    /**
     * It allows creating an empty tuple
     * 
     * @return An instance of Tuple ready to be completed
     * @throws BihashException It is raised when the Bihash instance is not ready or there are not positions.
     */
    public Tuple createAnEmptyTuple() throws BihashException
    {        
        return Tuple.create(this.getAttributesForConversor());
    }
    
    /**
     * It gets the Attribute information from the Project Definition
     * @param attID The requested attribute
     * @return The Attribute from the Project Definition when it is present, null otherwise
     */
    public Attribute getAttributeFromPD(String attID)
    {
        if(StringUtils.isEmpty(attID)) return null;
        
        Integer pos=this.AttIDidx.get(attID);
        if(pos==null) return null;
        
        return this.idxAtt.get(pos);
    }
}