/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.utils.time;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * It contains utilities related to the time management to measure
 * @author Mario Divan 
 * @version 1.0
 */
public class CPUtils {
        /**
         * It is an auxilliar variable used for time measuring in MicroClusterImpl
         */
        public static double timeForIndexing = 0;
        
        /**
         * It obtains the current time from the CPU when it is present
         * @return The current time from the CPU when it is present, zero otherwise
         */
        public static long getCPUTime(){
            ThreadMXBean bean = ManagementFactory.getThreadMXBean();
            return bean.isCurrentThreadCpuTimeSupported()? bean.getCurrentThreadCpuTime(): 0L;
        }    
}
