/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.cincamimisconversor;

/**
 * It is a specialization for the RowVectorStream class in terms of the Exceptions
 * @author Mario Diván
 * @version 1.0
 */
public class RowVectorStreamException extends Exception{
    /**
     * Default constructor
     */
    public RowVectorStreamException()
    {
        super();
    }

    /**
     * Constructor with a descriptive message for the Exception
     * @param msg The message to be informed
     */
    public RowVectorStreamException(String msg)
    {
        super(msg);
    }
}
