/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.pabmm_oa;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ciedayap.cincamimis.Cincamimis;
import org.ciedayap.cincamimis.Context;
import org.ciedayap.cincamimis.LikelihoodDistributionException;
import org.ciedayap.cincamimis.Measurement;
import org.ciedayap.cincamimis.MeasurementItem;
import org.ciedayap.cincamimis.MeasurementItemSet;
import org.ciedayap.cincamimis.Quantitative;
import org.ciedayap.cincamimisconversor.Attribute;
import org.ciedayap.cincamimisconversor.CincamimisQueue;
import org.ciedayap.cincamimisconversor.EntityRoute;
import org.ciedayap.cincamimisconversor.QueueException;
import org.ciedayap.cincamimisconversor.RowVector;
import org.ciedayap.utils.bihash.Bihash;
import org.ciedayap.utils.bihash.BihashException;

/**
 * It is responsible for processing the CINCAMIMIS Queue, translate it to RowVector Stream.
 * Then, from the RowVector Stream the outlier analysis could be applied.
 * 
 * @author Mario Diván
 */
public class OAQueueProcessor implements Runnable{
    /**
     * It indicates the flag which allows stoping the thread
     */
    private boolean active=true;
    /**
     * It represents que queue from which the messages will be obtained
     */
    private final CincamimisQueue queue;
    /**
     * It represents the set of ouput routes for each idEntity. For each stream related to a given entity,
     * the converted cincamimis messages are incorporated under the way of a set of tuples.
     */
    private final ConcurrentHashMap<String,EntityRoute> routes;
    
    /**
     * Default Constructor
     * @param queue The queue to be associated with the Processor
     * @param projects It contains the set of active projects
     * @param initialCapacity The initial capacity for the stream related to the route
     * @throws java.lang.Exception It is raised when the queue is not defined or the list of projects is empty
     */
    public OAQueueProcessor(CincamimisQueue queue,ArrayList<Bihash> projects, Integer initialCapacity) throws Exception
    {
        if(queue==null) throw new Exception("The informed CincamimisQueue is null");
        if(projects==null || projects.isEmpty()) throw new Exception("There are not projects");
        
        int counter=0;
        for(Bihash p:projects)
        {
            Integer val=p.getQuantityOfMonitoredEntities();
            if(val!=null && val>=0) counter+=val;
        }
        
        this.queue=queue;
        if(counter==0) throw new Exception("The quantity of monitored entities is zero");
        routes=new ConcurrentHashMap(counter);
         
        for(Bihash p:projects)
        {
            Enumeration<String> entities=p.getMonitoredIDEntities();
                        
            while(entities.hasMoreElements())
            {
                String identity=entities.nextElement();
                EntityRoute route=EntityRoute.create(p, initialCapacity, true);
                routes.put(identity, route);
            }
        }     

    }

    @Override
    public void run() {
        while(isActive())
        {
            while(!queue.isEmpty())
            {
                Cincamimis msg;
                try {
                    msg = queue.firstAvailableandRemove();
                } catch (QueueException ex) {
                    msg=null;
                    ex.printStackTrace();
                }
                
                if(msg!=null)
                {
                    try {
                        processingCincamimisMessage(msg);
                    } catch (BihashException ex) {
                 
                    }
                }
            }
        }
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public synchronized void setActive(boolean active) {
        this.active = active;
    }

    /**
     * It is responsible for taking each message from the CINCAMI/MIS Queue and translate it to
     * a RowVector instance for incorporating in its corresponding route
     * @param message The message to be trasnlated
     * @return TRUE in case of a correct translation, FALSE otherwise
     * @throws BihashException  When some internal data structure is not ready for holding attributes or its associated metadata
     */
    private boolean processingCincamimisMessage(Cincamimis message) throws BihashException {
        MeasurementItemSet mis=message.getMeasurements();
        if(mis==null) return false;
        
        ArrayList<MeasurementItem> lista=mis.getMeasurementItems();
        if(lista==null || lista.isEmpty()) return false;
        
        int counter=0;
        
        for(MeasurementItem item:lista)
        {
            EntityRoute route=routes.get(item.getIdEntity());//Obtaining the metadata for the entity from the Project Definition

            if(route!=null)
            {
                RowVector row=RowVector.create(route.getMetadata());
                
                //Verifying the Context
                Context myContext=item.getContext();
                if(myContext!=null)
                {//There is defined context
                    for(Measurement myMeasurement:myContext.getMeasurements())
                    {
                        if(myMeasurement!=null)
                        {           
                            org.ciedayap.pabmm.pd.requirements.Attribute attributeForMetric = route.getMetadata().getAttributeForMetric(myMeasurement.getIdMetric());                           
                            Optional<Attribute> ats=null;
                            
                            if(attributeForMetric!=null) ats=row.getAttributesByName(attributeForMetric.getID());

                            if(ats!=null && ats.isPresent())
                            {
                                ZonedDateTime zdt=myMeasurement.getDatetime();
                                row.setDatetime(zdt);
                                row.setMeasurementAdapterID(message.getDsAdapterID());

                                Quantitative value=myMeasurement.getMeasure().getQuantitative();

                                if(value!=null)
                                {//Measure
                                    row.update(ats.get(), value);
                                }
                            }
                        }
                    }
                }

                //Verifying the measurement itself
                Measurement myMeasurement= item.getMeasurement();
                if(myMeasurement!=null)
                {            
                    org.ciedayap.pabmm.pd.requirements.Attribute attributeForMetric = route.getMetadata().getAttributeForMetric(myMeasurement.getIdMetric());                           
                    Optional<Attribute> ats=null;
                    
                    if(attributeForMetric!=null) ats=row.getAttributesByName(attributeForMetric.getID());

                    if(ats!=null && ats.isPresent())
                    {
                        ZonedDateTime zdt=myMeasurement.getDatetime();
                        row.setDatetime(zdt);
                        Quantitative value=myMeasurement.getMeasure().getQuantitative();

                        if(value!=null)
                        {//Measure
                            row.update(ats.get(), value);
                        }
                    } 
                }

                try {
                    //It sends the RowVector by the corresponding output stream (Updating the last known state and completing the null values)
                    route.addToStream(row);          
                    counter++;
                } catch (InterruptedException | LikelihoodDistributionException ex) {
                    Logger.getLogger(OAQueueProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        System.out.println("Added Records from the CINCAMI/MIS Message: "+counter);

        return true;
    }
    
    /**
     * It returns the reference to the route related to the entity
     * @param idEntity The entity to be reached
     * @return The Entity Route related to the idEntity
     */
    public synchronized EntityRoute getRouteByEntity(String idEntity)
    {
        return this.routes.get(idEntity);
    }
}
