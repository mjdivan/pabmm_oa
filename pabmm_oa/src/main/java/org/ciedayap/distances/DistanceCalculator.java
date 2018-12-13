/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.distances;

import java.util.Arrays;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.ciedayap.cincamimisconversor.RowVector;

/**
 * It is responsible for implementing the distance calculating between tow RowVector instances
 * @author Mario Diván
 * @version 1.0
 */
public class DistanceCalculator {
    /**
     * It will compute the Euclidean distance when the calculateDistance() method is invoked.
     */
    public static final short DISTANCE_EUCLIDEAN=0;
    /**
     * It will compute the Manhattan distance when the calculateDistance() method is invoked.
     */
    public static final short DISTANCE_MANHATTAN=1;
    /**
     * It will compute the Chebyshev distance when the calculateDistance() method is invoked.
     */    
    public static final short DISTANCE_CHEBYSHEV=2;
    /**
     * It will compute the Canberra distance when the calculateDistance() method is invoked.
     */    
    public static final short DISTANCE_CANBERRA=3;
    /**
     * It will compute the Mahalanobis distance when the calculateDistance() method is invoked.
     */    
    public static final short DISTANCE_MAHALANOBIS=4;
    /**
     * It indicates the currently selected distance
     */
    private short selectedDistance;
    
    /**
     * Default Constructor. The Euclidean distance is chosen
     */
    public DistanceCalculator()
    {
        selectedDistance=DistanceCalculator.DISTANCE_EUCLIDEAN;       
    }
    
    public double calculateDistance(RealMatrix covariance,RowVector a,RowVector b) 
    {
        if(a==null || b==null) {
            return 0;
        }
        
        double distance=0;
        try{
            distance=calculateDistance(covariance,a.getAsDoubleValues(),b.getAsDoubleValues());
        }catch(Exception e)
        {
           distance=0; 
        }
        
        return distance;
    }
    /**
     * It calculates the distance between two vectors (a and b) using the chosen distance
     * @param covariance This parameter is just need for the Mahalanobis distance
     * @param a The first vector
     * @param b The second vector
     * @return It returns the selected distance
     * @throws Exception  It is raised when a not considered distance is indicated, or even, when the vectors are invalid.
     */
    public double calculateDistance(RealMatrix covariance,double a[], double b[]) throws Exception
    {
        switch(this.getSelectedDistance())
        {
            case DistanceCalculator.DISTANCE_CHEBYSHEV:
                return DistanceCalculator.chebyshevDistance(a, b);
            case DistanceCalculator.DISTANCE_EUCLIDEAN:
                return DistanceCalculator.euclideanDistance(a, b);
            case DistanceCalculator.DISTANCE_MANHATTAN:
                return DistanceCalculator.manhattanDistance(a, b);
            case DistanceCalculator.DISTANCE_CANBERRA:
                return DistanceCalculator.canberraDistance(a, b);
            case DistanceCalculator.DISTANCE_MAHALANOBIS:
                return DistanceCalculator.mahanalobisDistance(covariance,a, b);
            default:
                throw new Exception("The Indicated Distance is Invalid");
        }
        
    }
    /**
     * It computes the Euclidean distance between two double vectors.
     * This distance is the square root of the sum of the square difference of all the vector components.
     * The vectors must be the same dimension.
     * 
     * @param a The first double vector
     * @param b The second double vector
     * @return The euclidean distance between two vectors 
     * @throws Exception It is raised when the vectors have not the same dimension or they are nulls
     */    
    public static double euclideanDistance(double a[], double b[]) throws Exception
    {
        if(a==null || b==null || a.length!=b.length) throw new Exception("Invalid dimensions for the vectors a and b");
        double acu=0;
        for(int i=0;i<a.length;i++)
        {
            acu+=Math.pow(a[i]-b[i],2);
        }
        
        return Math.pow(acu, 0.5);
    }

    /**
     * It computes the Manhattan distance between two double vectors.
     * The vectors must be the same dimension.
     * 
     * @param a The first double vector
     * @param b The second double vector
     * @return The manhattan distance between two vectors 
     * @throws Exception It is raised when the vectors have not the same dimension or they are nulls
     */        
    public static double manhattanDistance(double a[], double b[]) throws Exception
    {
        if(a==null || b==null || a.length!=b.length) throw new Exception("Invalid dimensions for the vectors a and b");
        double acu=0;
        for(int i=0;i<a.length;i++)
        {
            acu+=Math.abs(a[i]-b[i]);
        }
        
        return acu;
    }

    /**
     * It computes the Canberra distance between two double vectors.
     * The Canberra distance is invariant in terms of the scale changing
     * The vectors must be the same dimension.
     * 
     * @param a The first double vector
     * @param b The second double vector
     * @return The canberra distance between two vectors 
     * @throws Exception It is raised when the vectors have not the same dimension or they are nulls
     */        
    public static double canberraDistance(double a[], double b[]) throws Exception
    {
        if(a==null || b==null || a.length!=b.length) throw new Exception("Invalid dimensions for the vectors a and b");
        double acu=0;
        for(int i=0;i<a.length;i++)
        {
            if((Math.abs(a[i])+Math.abs(b[i]))==0) throw new Exception("(Math.abs(a[i])+Math.abs(b[i]) is zero");
            
            acu+=Math.abs(a[i]-b[i]) / (Math.abs(a[i])+Math.abs(b[i]));
        }
        
        return acu;
    }
        

    /**
     * It computes the Chebysched distance between two double vectors.
     * This distance is the max difference in the vector components.
     * The vectors must be the same dimension.
     * 
     * @param a The first double vector
     * @param b The second double vector
     * @return The max diference between the vector components
     * @throws Exception It is raised when the vectors have not the same dimension or they are nulls
     */
    public static double chebyshevDistance(double a[], double b[]) throws Exception
    {
        if(a==null || b==null || a.length!=b.length) throw new Exception("Invalid dimensions for the vectors a and b");
        Double max=null;
        for(int i=0;i<a.length;i++)
        {
            if(max!=null)
            {
                double differ=Math.abs(a[i]-b[i]);
                if(max<differ) max=differ;
            }
            else
            {
                max=Math.abs(a[i]-b[i]);
            }
        }
        
        return max;
    }

    /**
     * It computes the Mahalanobis distance between two double vectors.This distance is the max difference in the vector components.
     * The vectors must be the same dimension.
     * 
     * @param covariance The covariance matrix for the data set
     * @param a The first double vector
     * @param b The second double vector
     * @return The mahalanobis distance
     * @throws Exception It is raised when the vectors have not the same dimension or they are nulls
     * @author José María Mateos (jmmateos@mce.hggm.es)
     */
    public static double mahanalobisDistance(RealMatrix covariance,double a[], double b[]) throws Exception
    {
        if(a==null || b==null || a.length!=b.length) throw new Exception("Invalid dimensions for the vectors a and b");
        if(covariance.getColumnDimension()!=a.length) throw new Exception("The number of columns in the covariance matrix is different of the vector dimension");

        if(Arrays.equals(a, b)) 
        {
            return 0.0;
        }
        
        double diff[]=new double[a.length];
        for(int i=0;i<a.length;i++)
        {
            diff[i]=a[i]-b[i];
        }
        
        RealMatrix invcov=new LUDecomposition(covariance).getSolver().getInverse();
        
        if(invcov==null) throw new Exception("The inverse of the covariance matrix could not be computed");
        
        double left[]=invcov.preMultiply(diff);
        
        //Dot Product
        double res=0.0;
        for(int i=0;i<diff.length;i++)
            res+= left[i]*diff[i];
        
        return res;
    }
    

    public static void main(String args[]) throws Exception
    {
        double a[]={1.0, 2.0, 3.0, 7.0,5.5};
        double b[]={1.0, 7.0, 5.0, 7.0,5.5};
        double c[]={1.0, 0.0, 8.0, 6.0,5.5};
        double matrix[][]={{11.0, 12.0, 3.0, 7.0, 5.5},
        {1.0, 2.0, 3.0, 7.0, 5.5},
        {2.0, 91.0, 3.0, 6.0, 2.5},
        {3.0, 2.0, 23.0, 5.0, 2.5},
        {4.0, 3.0, 3.0, 17.5, 7.5},
        {1.0, 4.0, 3.0, 27.5, 7.5},        
        };
        System.out.println(DistanceCalculator.chebyshevDistance(a, b));
        System.out.println(DistanceCalculator.euclideanDistance(a, b));
        System.out.println(DistanceCalculator.manhattanDistance(a, c));
        System.out.println(DistanceCalculator.canberraDistance(a, c));
        
        RealMatrix rm=MatrixUtils.createRealMatrix(matrix);
        Covariance covariance = new Covariance(rm);
        System.out.println(DistanceCalculator.mahanalobisDistance(covariance.getCovarianceMatrix(),a, c));
    }

    /**
     * @return the selected Distance
     */
    public short getSelectedDistance() {
        return selectedDistance;
    }

    /**
     * It establishes or changes the distance to be used in the distance calculation
     * @param selectedDistance the selectedDistance to set: DISTANCE_CHEBYSHEV, DISTANCE_EUCLIDEAN and DISTANCE_MANHATTAN 
     * @throws java.lang.Exception It is raised when a not-contempled distance is indicated
     */
    public void setSelectedDistance(short selectedDistance) throws Exception {
        switch(selectedDistance)
        {
            case DistanceCalculator.DISTANCE_CHEBYSHEV:
            case DistanceCalculator.DISTANCE_EUCLIDEAN:
            case DistanceCalculator.DISTANCE_MANHATTAN:
            case DistanceCalculator.DISTANCE_CANBERRA:
            case DistanceCalculator.DISTANCE_MAHALANOBIS:
                this.selectedDistance = selectedDistance;
                break;
            default:
                throw new Exception("The Indicated Distance is Invalid");
        }
        
    }

   
}
