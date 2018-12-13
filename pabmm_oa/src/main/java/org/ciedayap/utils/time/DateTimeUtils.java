/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.utils.time;

import java.time.ZonedDateTime;

/**
 *
 * @author mjdivan
 */
public class DateTimeUtils {
    /**
     * It allows comparing a ZonedDateTime in terms of its belonging to a DateTime range
     * @param start
     * @param end
     * @param point
     * @return 
     */
    public static boolean isBetween(ZonedDateTime start,ZonedDateTime end, ZonedDateTime point)
    {
        if(start== null || end==null || point==null) return false;
        
        if(point.isBefore(start)) return false;
        
        return !point.isAfter(end);                
    }
}
