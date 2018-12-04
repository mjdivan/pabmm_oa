/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.utils.bihash;

/**
 * It is a specialization from Exception class for the Bihash class
 * @author Mario Diván
 * @version 1.0
 */
public class BihashException extends Exception {
    /**
     * Default constructor
     */
    public BihashException()
    {
        super();
    }
    
    /**
     * Constructor which receives a given message
     * @param mess The message to be informed in the exception
     */
    public BihashException(String mess)
    {
        super(mess);
    }
}
