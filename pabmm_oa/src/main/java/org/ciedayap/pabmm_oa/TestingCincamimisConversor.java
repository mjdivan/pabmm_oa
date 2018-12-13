/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.pabmm_oa;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.DoubleStream;
import org.apache.commons.math3.linear.RealMatrix;
import org.ciedayap.cincamimis.Cincamimis;
import org.ciedayap.cincamimis.Measure;
import org.ciedayap.cincamimis.Measurement;
import org.ciedayap.cincamimis.MeasurementItem;
import org.ciedayap.cincamimis.MeasurementItemSet;
import org.ciedayap.cincamimisconversor.CincamimisQueue;
import org.ciedayap.cincamimisconversor.EntityRoute;
import org.ciedayap.cincamimisconversor.RowVector;
import org.ciedayap.pabmm.pd.CINCAMIPD;
import org.ciedayap.pabmm.pd.context.Context;
import org.ciedayap.pabmm.pd.context.ContextProperty;
import org.ciedayap.pabmm.pd.requirements.Attribute;
import org.ciedayap.pabmm.pd.requirements.Entities;
import org.ciedayap.pabmm.pd.requirements.Entity;
import org.ciedayap.utils.TranslateJSON;
import org.ciedayap.utils.bihash.AttributeCurrentState;
import org.ciedayap.utils.bihash.AttributeExtractor;
import org.ciedayap.utils.bihash.Bihash;
import org.ciedayap.utils.bihash.BihashException;
import org.ciedayap.utils.bihash.ExtractorException;
import org.ciedayap.utils.time.CPUtils;

/**
 *
 * @author Mario
 */
public class TestingCincamimisConversor {
    public static void main(String args[])  throws ExtractorException, BihashException, Exception
    {
        //testingBiHashFromCINCAMIPD();
        testOAQueueProcessor();
    }
    
    public static void testingBiHashFromCINCAMIPD() throws ExtractorException, BihashException
    {
        String jsonPD="{\"IDMessage\":\"1\",\"version\":\"1.0\",\"creation\":\"2018-11-01T22:05:24.916-03:00[America/Buenos_Aires]\",\"projects\":{\"projects\":[{\"ID\":\"PRJ_1\",\"name\":\"Outpatient Monitoring\",\"startDate\":\"2018-11-01T22:05:24.9-03:00[America/Buenos_Aires]\",\"infneed\":{\"ID\":\"IN_1\",\"purpose\":\"Avoid severe damages through the prevention of risks with direct impact in the outpatient health\",\"shortTitle\":\"Monitor the Outpatient\",\"specifiedEC\":{\"ID\":\"EC1\",\"name\":\"Outpatient\",\"superCategory\":{\"describedBy\":{\"characteristics\":[]},\"monitored\":{\"entitiesList\":[]}},\"describedBy\":{\"characteristics\":[{\"ID\":\"c_temp\",\"name\":\"The Corporal Temperature\",\"definition\":\"Value related to the axilar temperature in Celsius degree\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_ctemp\",\"name\":\"Value of Corporal Temperature\",\"version\":\"1.0\",\"IDAttribute\":\"ctemp\",\"scale\":{\"IDScale\":\"sca_temp\",\"name\":\"Environmental Temperature\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_temp\",\"name\":\"Celsius degreee\",\"symbol\":\"C\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_temp\",\"name\":\"Corporal Temperature\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_corptemp\",\"name\":\"Corporal Temperature\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"corptemp_normal\",\"name\":\"Corporal Temperature\",\"lowerThreshold\":36.0,\"upperThreshold\":37.1,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"Warning. The Corporal Temperature is Under 36 celsiud degree\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Warning. The Corporal Temperature is Above 37.1 celsius degree\"}]}},\"indicatorID\":\"ind_corpTemp\",\"name\":\"Level of the Corporal Temperature\",\"weight\":1}},{\"ID\":\"heart_rate\",\"name\":\"The Heart Rate\",\"definition\":\"Quantity of beats per minute (bpm)\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_heart\",\"name\":\"Value of Heart Rate\",\"version\":\"1.0\",\"IDAttribute\":\"heartrate\",\"scale\":{\"IDScale\":\"sca_heart\",\"name\":\"Heart Rate\\u0027s Scale\",\"scaleType\":\"RATIO\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_heart\",\"name\":\"Beats per minute\",\"symbol\":\"bpm\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_heart\",\"name\":\"Heart Rate\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_hearttemp\",\"name\":\"Heart Ratee\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"heartRate_normal\",\"name\":\"Heart Rate\",\"lowerThreshold\":62.0,\"upperThreshold\":75,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"Warning. The Heart Rate is under than 62 bpm\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Warning. The Heart Rate is upper than 75 bpm\"}]}},\"indicatorID\":\"ind_heartRate\",\"name\":\"Level of the Heart Rate\",\"weight\":1}}]},\"monitored\":{\"entitiesList\":[{\"ID\":\"Ent1\",\"name\":\"Outpatient A (Peter)\",\"relatedTo\":{\"entitiesList\":[]}}]}},\"describedBy\":{\"calculableConcepts\":[{\"ID\":\"calcon1\",\"name\":\"Health\",\"combines\":{\"characteristics\":[]},\"representedBy\":{\"representedList\":[{\"ID\":\"cmod\",\"name\":\"Outpatient Monitoring version 1.0\"}]},\"subconcepts\":{\"calculableConcepts\":[]}}]},\"characterizedBy\":{\"describedBy\":{\"contextProperties\":[{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_hum\",\"name\":\"The Environmental Humidity\",\"definition\":\"Volume of the water vapor in the air\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_humi\",\"name\":\"Value of Environmental Humidity\",\"version\":\"1.0\",\"IDAttribute\":\"pc_humi\",\"scale\":{\"IDScale\":\"sca_humi\",\"name\":\"Environmental Humidity\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_humi\",\"name\":\"Percentage\",\"symbol\":\"%\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_humi\",\"name\":\"Environmental Humidity\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_humidity\",\"name\":\"Environmental Humidity\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"humidity_low\",\"name\":\"Low Humidity\",\"lowerThreshold\":0,\"upperThreshold\":40.0,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"humidity_normal\",\"name\":\"Normal Humidity\",\"lowerThreshold\":40.01,\"upperThreshold\":60,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"The Environmental Humidity is upper than 60%\"},{\"idDecisionCriterion\":\"humidity_high\",\"name\":\"High Humidity\",\"lowerThreshold\":60.01,\"upperThreshold\":100,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":true,\"nbt_message\":\"The Environmental Humidity is High\",\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"The Environmental Humidity is High\"}]}},\"indicatorID\":\"ind_env_humidity\",\"name\":\"Level of the Environmental Humidity\",\"weight\":0.34}},{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_tem\",\"name\":\"The Environmental Temperature\",\"definition\":\"Quantity related to the environmental temperature in Celsius degree\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_temp\",\"name\":\"Value of Environmental Temperature\",\"version\":\"1.0\",\"IDAttribute\":\"pc_temp\",\"scale\":{\"IDScale\":\"sca_temp\",\"name\":\"Environmental Temperature\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_temp\",\"name\":\"Celsius degreee\",\"symbol\":\"C\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_temp\",\"name\":\"Environmental Temperature\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_env_temp\",\"name\":\"Environmental Temperature\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"temp_low\",\"name\":\"Low Temperature\",\"lowerThreshold\":10.0,\"upperThreshold\":18,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"The Environmental Temperature is under 10 celsius degree\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"temp_normal\",\"name\":\"Normal Temperature\",\"lowerThreshold\":18.01,\"upperThreshold\":29,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"temp_high\",\"name\":\"High Temperature\",\"lowerThreshold\":29.01,\"upperThreshold\":36,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":true,\"nbt_message\":\"Warning. High Temperature\",\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Alert. Very High Temperature\"}]}},\"indicatorID\":\"ind_env_temp\",\"name\":\"Level of the Environmental Temperature\",\"weight\":0.33}},{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_pressure\",\"name\":\"The Environmental Pressure\",\"definition\":\"Pressures derived from human activities which bring about changes in the state of the environment\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_press\",\"name\":\"Value of Environmental Pressure\",\"version\":\"1.0\",\"IDAttribute\":\"pc_press\",\"scale\":{\"IDScale\":\"sca_press\",\"name\":\"Environmental Pressure\\u0027s Scale\",\"scaleType\":\"RATIO\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_press\",\"name\":\"Hectopascals\",\"symbol\":\"hPa\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_press\",\"name\":\"Environmental Pressure\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_env_press\",\"name\":\"Environmental Pressure\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"press_normal\",\"name\":\"Normal Enviromental Pressure\",\"lowerThreshold\":900.0,\"upperThreshold\":1100,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true}]}},\"indicatorID\":\"ind_env_press\",\"name\":\"Level of the Environmental Pressure\",\"weight\":0.33}}]},\"ID\":\"ctx_outpatient\",\"name\":\"The Outpatient Context\",\"relatedTo\":{\"entitiesList\":[]}}},\"lastChange\":\"2018-11-01T22:05:24.9-03:00[America/Buenos_Aires]\"}]}}";
        CINCAMIPD lprj1=(CINCAMIPD)TranslateJSON.toObject(CINCAMIPD.class,jsonPD);
        
        Bihash bi=AttributeExtractor.fromCINCAMIPDPlain(jsonPD, "PRJ_1");
        
        if(bi!=null)
        {
            System.out.println("ID Project:"+bi.getIDProject());
            System.out.print("Entities: ");
            Enumeration<String> ents=bi.getMonitoredIDEntities();
            while(ents.hasMoreElements())
            {
                System.out.print(ents.nextElement()+" ");
            }
            System.out.println();
            
            ArrayList<Integer> orderedKeys=bi.getOrderedPositions();
            for(int i=0;i<orderedKeys.size();i++)
            {
                if(i==0) System.out.print("(");
                
                System.out.print(bi.getAtIdx(i).getID());
                        
                if(i==(orderedKeys.size()-1)) System.out.print(")");
                else System.out.print(",");
            }
            
            System.out.println();
            System.out.println("Dimension: "+bi.dimension());
            System.out.println("MinPos: "+bi.minPosition());
            System.out.println("MaxPos: "+bi.maxPosition());         
        }
        else
        {
            System.out.println("Bihash could not be created");
        }
    }        
    
    public static ArrayList<RowVector> generateIncomingData(int max) throws ExtractorException, BihashException
    {
        String jsonPD="{\"IDMessage\":\"1\",\"version\":\"1.0\",\"creation\":\"2018-11-01T22:05:24.916-03:00[America/Buenos_Aires]\",\"projects\":{\"projects\":[{\"ID\":\"PRJ_1\",\"name\":\"Outpatient Monitoring\",\"startDate\":\"2018-11-01T22:05:24.9-03:00[America/Buenos_Aires]\",\"infneed\":{\"ID\":\"IN_1\",\"purpose\":\"Avoid severe damages through the prevention of risks with direct impact in the outpatient health\",\"shortTitle\":\"Monitor the Outpatient\",\"specifiedEC\":{\"ID\":\"EC1\",\"name\":\"Outpatient\",\"superCategory\":{\"describedBy\":{\"characteristics\":[]},\"monitored\":{\"entitiesList\":[]}},\"describedBy\":{\"characteristics\":[{\"ID\":\"c_temp\",\"name\":\"The Corporal Temperature\",\"definition\":\"Value related to the axilar temperature in Celsius degree\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_ctemp\",\"name\":\"Value of Corporal Temperature\",\"version\":\"1.0\",\"IDAttribute\":\"ctemp\",\"scale\":{\"IDScale\":\"sca_temp\",\"name\":\"Environmental Temperature\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_temp\",\"name\":\"Celsius degreee\",\"symbol\":\"C\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_temp\",\"name\":\"Corporal Temperature\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_corptemp\",\"name\":\"Corporal Temperature\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"corptemp_normal\",\"name\":\"Corporal Temperature\",\"lowerThreshold\":36.0,\"upperThreshold\":37.1,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"Warning. The Corporal Temperature is Under 36 celsiud degree\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Warning. The Corporal Temperature is Above 37.1 celsius degree\"}]}},\"indicatorID\":\"ind_corpTemp\",\"name\":\"Level of the Corporal Temperature\",\"weight\":1}},{\"ID\":\"heart_rate\",\"name\":\"The Heart Rate\",\"definition\":\"Quantity of beats per minute (bpm)\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_heart\",\"name\":\"Value of Heart Rate\",\"version\":\"1.0\",\"IDAttribute\":\"heartrate\",\"scale\":{\"IDScale\":\"sca_heart\",\"name\":\"Heart Rate\\u0027s Scale\",\"scaleType\":\"RATIO\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_heart\",\"name\":\"Beats per minute\",\"symbol\":\"bpm\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_heart\",\"name\":\"Heart Rate\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_hearttemp\",\"name\":\"Heart Ratee\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"heartRate_normal\",\"name\":\"Heart Rate\",\"lowerThreshold\":62.0,\"upperThreshold\":75,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"Warning. The Heart Rate is under than 62 bpm\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Warning. The Heart Rate is upper than 75 bpm\"}]}},\"indicatorID\":\"ind_heartRate\",\"name\":\"Level of the Heart Rate\",\"weight\":1}}]},\"monitored\":{\"entitiesList\":[{\"ID\":\"Ent1\",\"name\":\"Outpatient A (Peter)\",\"relatedTo\":{\"entitiesList\":[]}}]}},\"describedBy\":{\"calculableConcepts\":[{\"ID\":\"calcon1\",\"name\":\"Health\",\"combines\":{\"characteristics\":[]},\"representedBy\":{\"representedList\":[{\"ID\":\"cmod\",\"name\":\"Outpatient Monitoring version 1.0\"}]},\"subconcepts\":{\"calculableConcepts\":[]}}]},\"characterizedBy\":{\"describedBy\":{\"contextProperties\":[{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_hum\",\"name\":\"The Environmental Humidity\",\"definition\":\"Volume of the water vapor in the air\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_humi\",\"name\":\"Value of Environmental Humidity\",\"version\":\"1.0\",\"IDAttribute\":\"pc_humi\",\"scale\":{\"IDScale\":\"sca_humi\",\"name\":\"Environmental Humidity\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_humi\",\"name\":\"Percentage\",\"symbol\":\"%\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_humi\",\"name\":\"Environmental Humidity\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_humidity\",\"name\":\"Environmental Humidity\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"humidity_low\",\"name\":\"Low Humidity\",\"lowerThreshold\":0,\"upperThreshold\":40.0,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"humidity_normal\",\"name\":\"Normal Humidity\",\"lowerThreshold\":40.01,\"upperThreshold\":60,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"The Environmental Humidity is upper than 60%\"},{\"idDecisionCriterion\":\"humidity_high\",\"name\":\"High Humidity\",\"lowerThreshold\":60.01,\"upperThreshold\":100,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":true,\"nbt_message\":\"The Environmental Humidity is High\",\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"The Environmental Humidity is High\"}]}},\"indicatorID\":\"ind_env_humidity\",\"name\":\"Level of the Environmental Humidity\",\"weight\":0.34}},{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_tem\",\"name\":\"The Environmental Temperature\",\"definition\":\"Quantity related to the environmental temperature in Celsius degree\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_temp\",\"name\":\"Value of Environmental Temperature\",\"version\":\"1.0\",\"IDAttribute\":\"pc_temp\",\"scale\":{\"IDScale\":\"sca_temp\",\"name\":\"Environmental Temperature\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_temp\",\"name\":\"Celsius degreee\",\"symbol\":\"C\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_temp\",\"name\":\"Environmental Temperature\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_env_temp\",\"name\":\"Environmental Temperature\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"temp_low\",\"name\":\"Low Temperature\",\"lowerThreshold\":10.0,\"upperThreshold\":18,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"The Environmental Temperature is under 10 celsius degree\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"temp_normal\",\"name\":\"Normal Temperature\",\"lowerThreshold\":18.01,\"upperThreshold\":29,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"temp_high\",\"name\":\"High Temperature\",\"lowerThreshold\":29.01,\"upperThreshold\":36,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":true,\"nbt_message\":\"Warning. High Temperature\",\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Alert. Very High Temperature\"}]}},\"indicatorID\":\"ind_env_temp\",\"name\":\"Level of the Environmental Temperature\",\"weight\":0.33}},{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_pressure\",\"name\":\"The Environmental Pressure\",\"definition\":\"Pressures derived from human activities which bring about changes in the state of the environment\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_press\",\"name\":\"Value of Environmental Pressure\",\"version\":\"1.0\",\"IDAttribute\":\"pc_press\",\"scale\":{\"IDScale\":\"sca_press\",\"name\":\"Environmental Pressure\\u0027s Scale\",\"scaleType\":\"RATIO\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_press\",\"name\":\"Hectopascals\",\"symbol\":\"hPa\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_press\",\"name\":\"Environmental Pressure\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_env_press\",\"name\":\"Environmental Pressure\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"press_normal\",\"name\":\"Normal Enviromental Pressure\",\"lowerThreshold\":900.0,\"upperThreshold\":1100,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true}]}},\"indicatorID\":\"ind_env_press\",\"name\":\"Level of the Environmental Pressure\",\"weight\":0.33}}]},\"ID\":\"ctx_outpatient\",\"name\":\"The Outpatient Context\",\"relatedTo\":{\"entitiesList\":[]}}},\"lastChange\":\"2018-11-01T22:05:24.9-03:00[America/Buenos_Aires]\"}]}}";
        CINCAMIPD lprj1=(CINCAMIPD)TranslateJSON.toObject(CINCAMIPD.class,jsonPD);
        
        Bihash bi=AttributeExtractor.fromCINCAMIPDPlain(jsonPD, "PRJ_1");
        Random r=new Random(10);
        System.out.println("Starting the Data Generating...");
        ArrayList<RowVector> list=new ArrayList<>();
        for(int i=0;i<max;i++)
        {
            RowVector rv=RowVector.create(bi);
            rv.setDatetime(ZonedDateTime.now());
            rv.setMeasurementAdapterID("ma1");

            for(int c=0;c<bi.dimension();c++)
            {
                rv.update(rv.getAttributes().get(c), r.doubles());
            }
            
            list.add(rv);
        }
        System.out.println("End Data Generation...");
        return list;
    } 

    public static void testOAQueueProcessor() throws ExtractorException, BihashException, NoSuchAlgorithmException, Exception
    {
        //The arriving queue
        CincamimisQueue queue=new CincamimisQueue();
        //The Project definition to be monitored
        String jsonPD="{\"IDMessage\":\"1\",\"version\":\"1.0\",\"creation\":\"2018-11-01T22:05:24.916-03:00[America/Buenos_Aires]\",\"projects\":{\"projects\":[{\"ID\":\"PRJ_1\",\"name\":\"Outpatient Monitoring\",\"startDate\":\"2018-11-01T22:05:24.9-03:00[America/Buenos_Aires]\",\"infneed\":{\"ID\":\"IN_1\",\"purpose\":\"Avoid severe damages through the prevention of risks with direct impact in the outpatient health\",\"shortTitle\":\"Monitor the Outpatient\",\"specifiedEC\":{\"ID\":\"EC1\",\"name\":\"Outpatient\",\"superCategory\":{\"describedBy\":{\"characteristics\":[]},\"monitored\":{\"entitiesList\":[]}},\"describedBy\":{\"characteristics\":[{\"ID\":\"c_temp\",\"name\":\"The Corporal Temperature\",\"definition\":\"Value related to the axilar temperature in Celsius degree\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_ctemp\",\"name\":\"Value of Corporal Temperature\",\"version\":\"1.0\",\"IDAttribute\":\"ctemp\",\"scale\":{\"IDScale\":\"sca_temp\",\"name\":\"Environmental Temperature\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_temp\",\"name\":\"Celsius degreee\",\"symbol\":\"C\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_temp\",\"name\":\"Corporal Temperature\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_corptemp\",\"name\":\"Corporal Temperature\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"corptemp_normal\",\"name\":\"Corporal Temperature\",\"lowerThreshold\":36.0,\"upperThreshold\":37.1,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"Warning. The Corporal Temperature is Under 36 celsiud degree\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Warning. The Corporal Temperature is Above 37.1 celsius degree\"}]}},\"indicatorID\":\"ind_corpTemp\",\"name\":\"Level of the Corporal Temperature\",\"weight\":1}},{\"ID\":\"heart_rate\",\"name\":\"The Heart Rate\",\"definition\":\"Quantity of beats per minute (bpm)\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_heart\",\"name\":\"Value of Heart Rate\",\"version\":\"1.0\",\"IDAttribute\":\"heartrate\",\"scale\":{\"IDScale\":\"sca_heart\",\"name\":\"Heart Rate\\u0027s Scale\",\"scaleType\":\"RATIO\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_heart\",\"name\":\"Beats per minute\",\"symbol\":\"bpm\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_heart\",\"name\":\"Heart Rate\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_hearttemp\",\"name\":\"Heart Ratee\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"heartRate_normal\",\"name\":\"Heart Rate\",\"lowerThreshold\":62.0,\"upperThreshold\":75,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"Warning. The Heart Rate is under than 62 bpm\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Warning. The Heart Rate is upper than 75 bpm\"}]}},\"indicatorID\":\"ind_heartRate\",\"name\":\"Level of the Heart Rate\",\"weight\":1}}]},\"monitored\":{\"entitiesList\":[{\"ID\":\"Ent1\",\"name\":\"Outpatient A (Peter)\",\"relatedTo\":{\"entitiesList\":[]}}]}},\"describedBy\":{\"calculableConcepts\":[{\"ID\":\"calcon1\",\"name\":\"Health\",\"combines\":{\"characteristics\":[]},\"representedBy\":{\"representedList\":[{\"ID\":\"cmod\",\"name\":\"Outpatient Monitoring version 1.0\"}]},\"subconcepts\":{\"calculableConcepts\":[]}}]},\"characterizedBy\":{\"describedBy\":{\"contextProperties\":[{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_hum\",\"name\":\"The Environmental Humidity\",\"definition\":\"Volume of the water vapor in the air\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_humi\",\"name\":\"Value of Environmental Humidity\",\"version\":\"1.0\",\"IDAttribute\":\"pc_humi\",\"scale\":{\"IDScale\":\"sca_humi\",\"name\":\"Environmental Humidity\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_humi\",\"name\":\"Percentage\",\"symbol\":\"%\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_humi\",\"name\":\"Environmental Humidity\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_humidity\",\"name\":\"Environmental Humidity\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"humidity_low\",\"name\":\"Low Humidity\",\"lowerThreshold\":0,\"upperThreshold\":40.0,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"humidity_normal\",\"name\":\"Normal Humidity\",\"lowerThreshold\":40.01,\"upperThreshold\":60,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"The Environmental Humidity is upper than 60%\"},{\"idDecisionCriterion\":\"humidity_high\",\"name\":\"High Humidity\",\"lowerThreshold\":60.01,\"upperThreshold\":100,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":true,\"nbt_message\":\"The Environmental Humidity is High\",\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"The Environmental Humidity is High\"}]}},\"indicatorID\":\"ind_env_humidity\",\"name\":\"Level of the Environmental Humidity\",\"weight\":0.34}},{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_tem\",\"name\":\"The Environmental Temperature\",\"definition\":\"Quantity related to the environmental temperature in Celsius degree\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_temp\",\"name\":\"Value of Environmental Temperature\",\"version\":\"1.0\",\"IDAttribute\":\"pc_temp\",\"scale\":{\"IDScale\":\"sca_temp\",\"name\":\"Environmental Temperature\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_temp\",\"name\":\"Celsius degreee\",\"symbol\":\"C\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_temp\",\"name\":\"Environmental Temperature\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_env_temp\",\"name\":\"Environmental Temperature\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"temp_low\",\"name\":\"Low Temperature\",\"lowerThreshold\":10.0,\"upperThreshold\":18,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"The Environmental Temperature is under 10 celsius degree\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"temp_normal\",\"name\":\"Normal Temperature\",\"lowerThreshold\":18.01,\"upperThreshold\":29,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"temp_high\",\"name\":\"High Temperature\",\"lowerThreshold\":29.01,\"upperThreshold\":36,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":true,\"nbt_message\":\"Warning. High Temperature\",\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Alert. Very High Temperature\"}]}},\"indicatorID\":\"ind_env_temp\",\"name\":\"Level of the Environmental Temperature\",\"weight\":0.33}},{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_pressure\",\"name\":\"The Environmental Pressure\",\"definition\":\"Pressures derived from human activities which bring about changes in the state of the environment\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_press\",\"name\":\"Value of Environmental Pressure\",\"version\":\"1.0\",\"IDAttribute\":\"pc_press\",\"scale\":{\"IDScale\":\"sca_press\",\"name\":\"Environmental Pressure\\u0027s Scale\",\"scaleType\":\"RATIO\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_press\",\"name\":\"Hectopascals\",\"symbol\":\"hPa\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_press\",\"name\":\"Environmental Pressure\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_env_press\",\"name\":\"Environmental Pressure\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"press_normal\",\"name\":\"Normal Enviromental Pressure\",\"lowerThreshold\":900.0,\"upperThreshold\":1100,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true}]}},\"indicatorID\":\"ind_env_press\",\"name\":\"Level of the Environmental Pressure\",\"weight\":0.33}}]},\"ID\":\"ctx_outpatient\",\"name\":\"The Outpatient Context\",\"relatedTo\":{\"entitiesList\":[]}}},\"lastChange\":\"2018-11-01T22:05:24.9-03:00[America/Buenos_Aires]\"}]}}";
        Bihash bi=AttributeExtractor.fromCINCAMIPDPlain(jsonPD, "PRJ_1");
        ArrayList<Bihash> projects=new ArrayList();
        projects.add(bi);                
        
        //OAQueueProcessor
        OAQueueProcessor proc=new OAQueueProcessor(queue,projects,100);                        
        Cincamimis message=generateIncomingData(10,jsonPD);
        queue.add(message);
        
        ExecutorService executor=Executors.newFixedThreadPool(10);
        executor.execute(proc);
        //The Stream must be created in OAQueueProcessor for associating with it
        OWideDetector detector=OWideDetector.create(proc.getRouteByEntity("Ent1").getStream(),10,1000);                
        executor.execute(detector);        
        
        int qItems=1000;
        
        long a=System.nanoTime();
        message=generateIncomingData(qItems,jsonPD);
        long b=System.nanoTime();
        
        long before=System.nanoTime();
        queue.add(message);                                    

        while(!queue.isEmpty())
        {

        }

        detector.print(false);        
        detector.setActivated(false);
        
        proc.setActive(false);//Stop Thread        
        executor.shutdown();
        
        
        while(!executor.isTerminated()){}   

        long after=System.nanoTime();
        System.out.println(qItems+"\t"+(after-before)+" Data Generation Time: "+(b-a));            
        
        EntityRoute er=proc.getRouteByEntity("Ent1");
        org.ciedayap.cincamimisconversor.Attributes attList=bi.getAttributesForConversor();
        
       /* AttributeCurrentState acs=er.getCurrentState(attList.get(0).hashCode());
        System.out.println("Attribute: "+attList.get(0).getName());
        System.out.println("Mean: "+acs.getLastArithmeticMean());
        System.out.println("Max: "+acs.getMaxValue());
        System.out.println("Min: "+acs.getMinValue());
        RealMatrix rm=er.getStream().getAsRealMatrix();
        System.out.println("Matrix Rows: "+rm.getRowDimension()+" Columns: "+rm.getColumnDimension());
        detector.print(false);*/
    }
    
    public static Cincamimis generateIncomingData(int max,String jsonPD) throws ExtractorException, BihashException, NoSuchAlgorithmException
    {
        CINCAMIPD lprj1=(CINCAMIPD)TranslateJSON.toObject(CINCAMIPD.class,jsonPD);
        Bihash bi=AttributeExtractor.fromCINCAMIPDPlain(jsonPD, "PRJ_1");
        HashMap map=AttributeExtractor.fromCINCAMIPDOmToHashMap(lprj1, "PRJ_1");
        Entities entities=(Entities) map.get(AttributeExtractor.KEY_HM_ENTITIES);
        
        Entity myEntity=entities.getEntitiesList().get(0);
        ArrayList<ContextProperty> ctxList=(ArrayList<ContextProperty>) map.get(AttributeExtractor.KEY_HM_CTXPROPERTIES);
        ArrayList<Attribute> attList=(ArrayList<Attribute>) map.get(AttributeExtractor.KEY_HM_ATTRIBUTES);
        
        Cincamimis mis=new Cincamimis();
        mis.setDsAdapterID("dsadapter1");
        mis.setVersion("1.0");                
        MeasurementItemSet set=new MeasurementItemSet();
                                 
        Random r=new Random(10);

        ArrayList<RowVector> list=new ArrayList<>();
        for(int i=0;i<max;i++)
        {          
            org.ciedayap.cincamimis.Context c=new org.ciedayap.cincamimis.Context();
            for (ContextProperty cp : ctxList) 
            {
                Measurement mea=new Measurement();
                mea.setDatetime(ZonedDateTime.now());
                mea.setIdMetric(cp.getQuantifiedBy().getRelated().get(0).getIDmetric());
                mea.setMeasure(Measure.factoryDeterministicMeasureWithoutCD(new BigDecimal(r.nextDouble())));
                
                c.add(mea);
            }           

            for(Attribute a:attList)
            {
                MeasurementItem mi=new MeasurementItem();
                mi.setContext(c);                                    
                mi.setDataSourceID("ds1");
                mi.setIdEntity(myEntity.getID());
                mi.setOriginalDataFormat("raw");                  
                
                Measurement mea=new Measurement();
                
                mea.setDatetime(ZonedDateTime.now());
                mea.setIdMetric(a.getQuantifiedBy().getRelated().get(0).getIDmetric());
                mea.setMeasure(Measure.factoryDeterministicMeasureWithoutCD(new BigDecimal(r.nextDouble())));
                
                mi.setMeasurement(mea);
                set.add(mi);
            }                       
        }
        
        mis.setMeasurements(set);

        return mis;
    } 
    
}

