/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.cincamimisconversor;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;

/**
 * It is responsible for keeping the list of RowVector instances obtained from the 
 * CINCAMIMIS messages in the order in which each one was incorporated.
 * @author Mario Divan
 * @version 1.0
 */
public class RowVectorStream {
    /**
     * It is responsible for keeping the flow of RowVector instances obtained from the CINCAMI/MIS messages
     */
    private ArrayBlockingQueue<RowVector> stream;
    
    /**
     * It creates a strean with a given initial capacity
     * @param initialSize The initial capacity for the stream
     * @param fifo TRUE fifo, FALSE the order is unspecified
     * @throws RowVectorStreamException It is raised when the initial capacity is incorrectly defined (e.g. zero or negative values), or the stream
     * has not been created (e.g. insufficient memory)
     */
    public RowVectorStream(Integer initialSize, boolean fifo) throws RowVectorStreamException
    {
        if(initialSize==null) throw new RowVectorStreamException("The Initial Size is null");
        if(initialSize<=0) throw new RowVectorStreamException("The Initial Size is negative or zero");
        
        stream=new ArrayBlockingQueue(initialSize);
        
        if(stream==null) throw new RowVectorStreamException("The Vector Stream has not been created");
    } 
    
    /**
     * It creates a new instance of RowVectorStream with the @initialSize capacity and using the accesing method in terms of the @fifo parameter
     * @param initialSize The initial capacity related to the stream
     * @param fifo TRUE First Input, First Ouput, FALSE no order is established
     * @return A new instance of the RowVectorStream
     * @throws RowVectorStreamException It is raised when there exist inconvenient with the initial capacity for the stream
     */
    public synchronized static RowVectorStream create(Integer initialSize, boolean fifo) throws RowVectorStreamException
    {
       return new RowVectorStream(initialSize,fifo);
    }

    /**
     * It creates a new instance of RowVectorStream with the 1000 positions using FIFO
     * @return A new instance of the RowVectorStream
     * @throws RowVectorStreamException It is raised when there exist inconvenient with the initial capacity for the stream
     */
    public synchronized static RowVectorStream createBasicInstance() throws RowVectorStreamException
    {
       return new RowVectorStream(1000,true);
    }
    
    /**
     * Add a new RowVector instance at the end of the stream
     * @param item The instance to be incorporated
     * @return TRUE when the RowVector instance was incorporated, FALSE otherwise
     */
    public synchronized boolean addRowVector(RowVector item)
    {
        if(stream==null){
            return false;
        }
        if(item==null){
            return false;
        }
        if(!item.isConsistent()) {
            return false;
        }        
        
        if(!stream.offer(item))
        {
            stream.poll();
            stream.offer(item);
        }
        
        return true;
    }

    
    /**
     * It gives the first element from the queue without remove the element from the list
     * @return The first element in the queue without remove it. Additionally,
     * it returns null if the queue is empty.
     * @throws QueueException This exception is raised when there is not an initialized queue.
     */
    public synchronized RowVector firstAvailable() throws QueueException
    {
        if(stream==null) throw new QueueException("The Queue is not found");
        if(stream.isEmpty()) return null;
        
        return stream.peek();
    }    
    
    /**
     * It removes all the RowVector from the stream
     */
    public synchronized void clear()
    {
        if(stream==null) return;
        
        stream.clear();
    }
    
    /**
     * It takes the first element from the stream and remove it from the stream.
     * 
     * @return The first available elements in the stream
     * @throws QueueException It is raised when the queue is null
     */
    public synchronized RowVector firstAvailableandRemove() throws QueueException
    {
        if(stream==null) throw new QueueException("The Queue is not found");
        if(stream.isEmpty()) return null;
                
        return stream.poll();
    }
    
    /**
     * It obtains the RowVectir located at a given position
     * @param idx The position to be reached
     * @return The instance at the position "idx" or null when the index is invalid
     */
    public RowVector getRowVector(int idx)
    {
        if(idx<0 || idx>stream.size()) return null;
        
        return (RowVector) stream.toArray()[idx];
    }
    
    /**
     * It removes the instance from the stream located at a given position
     * @param idx The position in the stream to be removed
     * @return The removed instance from the stream
     */
    public synchronized boolean removeAt(int idx)
    {
        if(idx<0 || idx>stream.size()) return false;
        RowVector ptr=getRowVector(idx);
        return stream.remove(ptr);
    }
    
    /**
     * The current size related to the stream
     * @return The stream size
     */
    public synchronized Integer currentSize()
    {
        if(stream==null) return 0;
        
        return stream.size();
    }
    
    /**
     * It creates a RealMatrix from the contained data in the stream
     * @return A RealMatrix instance, null otherwise
     */
    public synchronized RealMatrix getAsRealMatrix()
    {
        if(stream==null || stream.isEmpty()) return null;
        RowVector rv=stream.peek();
        if(rv.dimensions()<1) return null;
        
        double data[][]=new double[stream.size()][rv.dimensions()];
                        
        Iterator<RowVector> list=stream.iterator();
        int i=0;
        while(list!=null && list.hasNext())
        {
            RowVector item=list.next();
            for(int j=0;j<rv.dimensions();j++)
                data[i][j]=item.getAt(j);
            
            i++;
        }
        
        return MatrixUtils.createRealMatrix(data);                
    }
    
    /**
     * It obtains the covariance matrix from the set of data for each variable in each typle along with the stream
     * @return The Covariance Matrix, null otherwise
     */
    public synchronized RealMatrix getCovarianceMatrix()
    {
        RealMatrix matrix=getAsRealMatrix();
        
        if(matrix==null) return null;
        if(matrix.getRowDimension()<=1) return null;
        
        Covariance covariance = new Covariance(matrix);
        
        return covariance.getCovarianceMatrix();        
    }
}
