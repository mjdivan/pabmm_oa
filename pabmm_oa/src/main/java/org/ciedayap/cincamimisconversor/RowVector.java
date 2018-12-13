/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.cincamimisconversor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import org.ciedayap.cincamimis.LikelihoodDistributionException;
import org.ciedayap.cincamimis.Quantitative;
import org.ciedayap.utils.QuantitativeUtils;
import org.ciedayap.utils.StringUtils;
import org.ciedayap.utils.bihash.Bihash;
import org.ciedayap.utils.bihash.BihashException;

/**
 * It is a specialization from the Tuple class. It is responsible for informing the attributes
 * such as a mathematical vector 
 * @author mjdivan
 */
public class RowVector extends Tuple implements Comparable<RowVector>{
    /**
     * The time stamp associated with the row
     */
    private java.time.ZonedDateTime datetime;
    /**
     * The measurement adaper through which the data arrives to the gathering function
     */
    private String measurementAdapterID;
    /**
     * It incorporates the order of the attributes in consonance with the attributes instance of tuple, at the same time that
     * the metadata from the project definition is incorporated
     */
    private Bihash metadata;

    /**
     * 
     * @param at The attributes as columns
     */
    public RowVector(Attributes at) {
        super(at);
    }
    
    /**
     * Default factory method
     * @param bi The bihash structure with the metadata about the attributes
     * @return An instance of RowVector
     * @throws BihashException It is raised when the attributes cannot be obtained for the conversion
     */
    public static RowVector create(Bihash bi) throws BihashException
    {
        RowVector rv=new RowVector(bi.getAttributesForConversor());
        rv.setMetadata(bi);
        
        return rv;
    }
    /**
     * The default factory method
     * @param at The columns 
     * @return A new instance of RowVector
     */
    public static RowVector create(Attributes at)
    {
      return new RowVector(at);  
    }
    
    /**
     * It gets the Tuple as a double vector following the same stablished order than the attributes
     * @return A double vector following the same order than attributes
     * @throws LikelihoodDistributionException It is raised when some inconvenient is found at the moment in which a quantitative instance must be computed in an estimated value
     */
    public double[] getAsDoubleValues() throws LikelihoodDistributionException
    {
        if(!this.isConsistent()) return null;
        if(attributes.columnCount()<1) return null;
        double ret[]=new double[attributes.columnCount()];
        
        for(int i=0;i<this.attributes.columnCount();i++)
        {
            Attribute at=attributes.get(i);
            Object value=this.getValue(at);
            
            if(at.isQuantitative() && value!=null && value instanceof Quantitative)
            {
                if(QuantitativeUtils.isDeterministic(((Quantitative)value)))
                {//deterministic
                    ret[i]=((Quantitative)value).getDeterministicValue().doubleValue();
                }
                else
                {//estimated
                    ret[i]=QuantitativeUtils.mathematicalExpectation(((Quantitative)value)).doubleValue();                           
                }
             
            }
            else
            {
              ret[i]=0.0;
            }            
        }
        
        return ret;
    }

    /**
     * It gets the Tuple as a BigDecimal vector following the same stablished order than the attributes
     * @return A BigDecimal vector following the same order than attributes
     * @throws LikelihoodDistributionException It is raised when some inconvenient is found at the moment in which a quantitative instance must be computed in an estimated value
     */
    public BigDecimal[] getAsBigDecimalValues() throws LikelihoodDistributionException
    {
        if(!this.isConsistent()) return null;
        if(attributes.columnCount()<1) return null;
        BigDecimal ret[]=new BigDecimal[attributes.columnCount()];
        
        for(int i=0;i<this.attributes.columnCount();i++)
        {
            Attribute at=attributes.get(i);
            Object value=this.getValue(at);
            
            if(at.isQuantitative() && value!=null && value instanceof Quantitative)
            {
                if(QuantitativeUtils.isDeterministic(((Quantitative)value)))
                {//deterministic
                    ret[i]=((Quantitative)value).getDeterministicValue();
                }
                else
                {//estimated
                    ret[i]=QuantitativeUtils.mathematicalExpectation(((Quantitative)value));                           
                }
             
            }
            else
            {
              ret[i]=BigDecimal.ZERO;
            }            
        }
        
        return ret;
    }

    /**
     * @return the datetime
     */
    public java.time.ZonedDateTime getDatetime() {
        return datetime;
    }

    /**
     * @param datetime the datetime to set
     */
    public final void setDatetime(java.time.ZonedDateTime datetime) {
        this.datetime = datetime;
    }

    /**
     * @return the measurementAdapterID
     */
    public String getMeasurementAdapterID() {
        return measurementAdapterID;
    }

    /**
     * @param measurementAdapterID the measurementAdapterID to set
     */
    public final void setMeasurementAdapterID(String measurementAdapterID) {
        this.measurementAdapterID = measurementAdapterID;
    }

    /**
     * @return the metadata
     */
    public Bihash getMetadata() {
        return metadata;
    }

    /**
     * @param metadata the metadata to set
     */
    public final void setMetadata(Bihash metadata) {
        this.metadata = metadata;
    }
    /**
     * It is responsible for verifying the consistency from the Tuple class, but also
     * to verify the presence of the datetime, dataSourceID, measurementAdapterID and metadata 
     * related to the Measurement.
     * @return TRUE when all the metadata is correct and there exists at least one attribute with a value, FALSE otherwise.
     */
    @Override
    public boolean isConsistent()
    {
        if(!super.isConsistent()) return false;
                
        if(this.metadata==null || metadata.dimension()<=0){
            return false;
        }
        if(StringUtils.isEmpty(measurementAdapterID)) 
        {
            return false;
        }
        if(this.datetime==null) {            
            return false;
        }
       
        return true;
    }
    
    public ArrayList<Attribute> getAttributesAsAList()
    {
        if(attributes==null || attributes.columnCount()==0) return null;
        
        ArrayList<Attribute> list=new ArrayList();
        for(int i=0;i<this.attributes.columnCount();i++)
        {
           list.add(attributes.get(i));            
        }
        
        return list;
    }
    
    /**
     * It returns the value at the indicated position as a double
     * @param index The position to get the value
     * @return The double value when it is present, null otherwise
     */
    public Double getAt(int index)
    {                
        Attribute att=this.attributes.get(index);
        if(att.isQuantitative())
        {
            Object ptr=this.getValue(att);
            if(ptr==null) return null;
            Quantitative value=(Quantitative) ptr;
            if(QuantitativeUtils.isEstimated(value))
            {
                try {
                    return QuantitativeUtils.mathematicalExpectation(value).doubleValue();
                } catch (LikelihoodDistributionException ex) {
                    return null;
                }
            } else return value.getDeterministicValue().doubleValue();
            
        } else return null;
    }
    
    /**
     * The associated dimmension to the row
     * @return The int value for the dimmension
     */
    public int dimensions()
    {
        return metadata.dimension().intValue();
    }
    
    @Override
    public int hashCode()
    {
        double values[]=null;
        
        try {
            values=this.getAsDoubleValues();
        } catch (LikelihoodDistributionException ex) {
            values=null;
        }

        if(values==null) super.hashCode();
        
        int hashCode2=1;
        for(Double value:values)
        {
            hashCode2 = 31*hashCode2 + (int)value.hashCode() ;
        }
        
        return hashCode2;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj==null) return false;
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        if(obj instanceof RowVector)
        {
            final RowVector other=(RowVector)obj;
            if(this.dimensions()!=other.dimensions()) return false;
            if (!Objects.equals(this.datetime, other.datetime)) return false;
            ArrayList<Attribute> this_list=this.getAttributesAsAList();
            
            for(Attribute att:this_list)
            {
                if(this.getValue(att)!=other.getValue(att)) return false;
            }
            
            ArrayList<Attribute> o_list=other.getAttributesAsAList();
            for(Attribute att:o_list)
            {
                if(this.getValue(att)!=other.getValue(att)) return false;
            }
            
            return true;
        }
        else
        {
            return false;
        }      
    }

    @Override
    public int compareTo(RowVector o) {
       int dim=Math.min(this.dimensions(), o.dimensions());
       for(int i = 0; i < dim; i++) 
       {
            double v1 = this.getAt(i);
            double v2 = o.getAt(i);
            if(v1 > v2) 
            {
                return +1;
            }
            if(v1 < v2) 
            {
                return -1;
            }
        }
		
        if(this.dimensions() > dim) {
                return +1;
        }
		
        if(o.dimensions() > dim) {
                return -1;
        }
		
        return 0;       
    }
    
    @Override
   public String toString()
   {
       StringBuilder sb=new StringBuilder();
       sb.append(" (");
       for(int i=0;i<this.dimensions();i++)
       {
           sb.append(" ").append(this.getAt(i)).append(" ");
       }
       sb.append(")");       
       
       return sb.toString();
   }
}
