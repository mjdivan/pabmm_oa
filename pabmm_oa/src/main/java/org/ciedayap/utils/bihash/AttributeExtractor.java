/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.utils.bihash;

import java.io.Serializable;
import java.util.Optional;
import org.ciedayap.pabmm.pd.CINCAMIPD;
import org.ciedayap.pabmm.pd.MeasurementProject;
import org.ciedayap.pabmm.pd.MeasurementProjects;
import org.ciedayap.pabmm.pd.requirements.Attribute;
import org.ciedayap.utils.StringUtils;
import org.ciedayap.utils.TranslateJSON;
import org.ciedayap.utils.TranslateXML;

/**
 * It is resposible for extractint the attributes from different kind of schemas (e.g. CINCAMIPD)
 * @author Mario Diván
 * @version 1.0
 */
public class AttributeExtractor implements Serializable{
    /**
     * It is responsible for translating the JSON/XML data format to the CINCAMIPD Project Model, and then
     * It makes the neccesary processing for obtaining the Bihash instance. 
     * @param pdefinitionPlain The project definition as a UTF-8 String (it could be expressed in JSON or XML data format)
     * @param IDProject The ID of the project´s attributes which should be extracted in a Bihash instance 
     * @return The Bihash instances with the attributes for the given IDProject
     * @throws ExtractorException It happens when some formal aspect is not satisfied in the CINCAMIPD message (e.g. there not exist the Project ID in the message)
     * @throws BihashException It happens when the initial value assigned to the Bihash instance is invalid.
     */
    public static Bihash fromCINCAMIPDPlain(String pdefinitionPlain, String IDProject) throws ExtractorException, BihashException
    {
        if(StringUtils.isEmpty(IDProject)) throw new ExtractorException("The IDProject is invalid");
        if(StringUtils.isEmpty(pdefinitionPlain)) throw new ExtractorException("The CINCAMIPD plain content is empty");
                                
        boolean xml=pdefinitionPlain.toUpperCase().contains("CINCAMIPD");
              
        CINCAMIPD pdefinition=null;
       
        if(xml)
        {
            pdefinition=(CINCAMIPD) TranslateXML.toObject(CINCAMIPD.class, pdefinitionPlain);
        }
        else
        {
            pdefinition=(CINCAMIPD) TranslateJSON.toObject(CINCAMIPD.class, pdefinitionPlain);
        }
        
        if(pdefinition==null) throw new ExtractorException("The CINCAMI/Project Definition could not be obtained from the plain content");
        
        return fromCINCAMIPDOm(pdefinition,IDProject);
    }
    
    /**
     * It is responsible for extracting the attributes from the CINCAMIPD message to the Bihash instance.
     * The CINCAMIPD message is organized in terms of the associated object model.
     * @param pdefinition The CINCAMIPD message
     * @param IDProject The ID of the project´s attributes which should be extracted in a Bihash instance
     * @return The Bihash instance with the attributes associated with a given project
     * @throws org.ciedayap.utils.bihash.ExtractorException  It happens when some formal aspect is not satisfied in the CINCAMIPD message (e.g. there not exist the Project ID in the message)
     * @throws org.ciedayap.utils.bihash.BihashException It happens when the initial value assigned to the Bihash instance is invalid.
     */
    public static Bihash fromCINCAMIPDOm(CINCAMIPD pdefinition, String IDProject) throws ExtractorException, BihashException
    {
        if(StringUtils.isEmpty(IDProject)) throw new ExtractorException("The IDProject is invalid");
        if(pdefinition==null) throw new ExtractorException("The Project Definition is null");
        if(!pdefinition.isDefinedProperties()) throw new ExtractorException("The Mandatory Properties are not established for the Project Definition");
        
        MeasurementProjects projects=pdefinition.getProjects();
        if(projects==null || projects.getProjects()==null || projects.getProjects().size()<=0)
            throw new ExtractorException("There are not projects inside the CINCAMIPD message");
          
        Optional<MeasurementProject> project=projects.getProjects().stream().filter(p-> p.getID().equalsIgnoreCase(IDProject)).findFirst();
        if(project==null || !project.isPresent())
            throw new ExtractorException("The Project´s ID has not been found in the CINCAMIPD message");
        
        if(project.get().getInfneed()==null)
            throw new ExtractorException("[Project] The information need is not defined");
        
        if(project.get().getInfneed().getSpecifiedEC()==null)
            throw new ExtractorException("[Project] The Entity Category is not defined"); 
        
        if(project.get().getInfneed().getSpecifiedEC().getDescribedBy()==null || 
                project.get().getInfneed().getSpecifiedEC().getDescribedBy().getCharacteristics()==null ||
                project.get().getInfneed().getSpecifiedEC().getDescribedBy().getCharacteristics().isEmpty())
            throw new ExtractorException("[Project] The Attributes for the Entity Category is not defined");        
        
        if(project.get().getInfneed().getCharacterizedBy()==null)
            throw new ExtractorException("[Project] The Context for the Entity Category is not defined");
        
        if(project.get().getInfneed().getCharacterizedBy().getDescribedBy()==null ||
                project.get().getInfneed().getCharacterizedBy().getDescribedBy().getContextProperties()==null ||
                project.get().getInfneed().getCharacterizedBy().getDescribedBy().getContextProperties().isEmpty())
            throw new ExtractorException("[Project] The Context for the Entity Category has not the associated context properties");
        
        
        Bihash bi=Bihash.create(10);
            
        if(bi==null) throw new BihashException("The Bihash instance could not be created");
        
        //The Entity Attributes
        for(Attribute att:project.get().getInfneed().getSpecifiedEC().getDescribedBy().getCharacteristics())
        {
          if(!StringUtils.isEmpty(att.getID()) && !StringUtils.isEmpty(att.getName()))
          {
              bi.add(att);
          }
        }
        
        //The Context Properties
        for(Attribute att:project.get().getInfneed().getCharacterizedBy().getDescribedBy().getContextProperties())
        {
          if(!StringUtils.isEmpty(att.getID()) && !StringUtils.isEmpty(att.getName()))
          {
              bi.add(att);
          }  
        }
        
        return bi;
    }
}
