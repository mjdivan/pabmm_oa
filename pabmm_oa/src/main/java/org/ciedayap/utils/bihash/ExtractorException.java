/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.utils.bihash;

/**
 * It specializes the Exception for the Attribute Extracting
 * 
 * @author Mario Diván
 * @version 1.0
 */
public class ExtractorException extends Exception{
    /**
     * The default constructor
     */
    public ExtractorException()
    {
        super();
    }
    
    /**
     * It raises a specific message related to the attribute extraction
     * @param msg The message to be informed
     */
    public ExtractorException(String msg)
    {
        super(msg);
    }
    
}
