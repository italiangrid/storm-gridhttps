/*
 * Copyright (c) Istituto Nazionale di Fisica Nucleare (INFN). 2006-2010.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.grid.storm.gridhttps.template;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import it.grid.storm.gridhttps.Configuration;
import it.grid.storm.gridhttps.log.LoggerManager;

/**
 * @author Michele Dibenedetto
 */
public class StorageAreaContextTemplate
{


    private static Logger log = LoggerManager.getLogger(StorageAreaContextTemplate.class);
    /**
     * The name of the template to be used
     */
    private static final String TEMPLATE_FILE_NAME = "context.vm";
    private static final String SA_ROOT_PLACEHOLDER = "SA_Root";
    /**
     * The name of the file to be created
     */
    
    private static final char FILE_PATH_SEPARATOR = '#';
    private static final char STFN_SEPARATOR = '/';
    private static final String FILE_NAME_PREFIX = Configuration.CONTEXT_FILE_NAME_PREFIX + FILE_PATH_SEPARATOR;
    private static final String FILE_NAME_SUFFIX = ".xml";
    private static final String FILE_ENCODING = "UTF-8";
    private static final Object LOG_FILE_PATH = Configuration.LOG_FOLDER_PATH + File.separatorChar + "velocity-gridhttps.log";
    private String SARoot = null;
    private final String fileName;
    /**
     * The directory where to store the generated file
     */
    private String directory;
    private String templateFolder;


    /**
     * @param directory
     * @param SAstfnRoot
     * @param templateFolder
     * @throws IllegalArgumentException
     */
    public StorageAreaContextTemplate(String directory, String SAstfnRoot, String templateFolder) throws IllegalArgumentException
    {
        if(directory == null || directory.equals("") || SAstfnRoot == null || SAstfnRoot.equals("") || templateFolder == null || templateFolder.equals(""))
        {
            log.error("Unable to create StorageAreaContextTemplate, received invalid arguments : directory=" + directory + " , SAstfnRoot=" + SAstfnRoot + " , templateFolder=" + templateFolder);
            throw new IllegalArgumentException("Unable to create StorageAreaContextTemplate, received invalid arguments");
        }
        log.debug("Creating storage area context template for stfnRoot \'" + SAstfnRoot + "\' in folder " + directory
                + " using teplate " + TEMPLATE_FILE_NAME + " in folder " + templateFolder);
        if (directory.charAt(directory.length() - 1) != File.separatorChar)
        {
            this.directory = directory + File.separatorChar;
        }
        String SARootPart = SAstfnRoot;
        if (SAstfnRoot.charAt(0) == STFN_SEPARATOR)
        {
            SARootPart = SAstfnRoot.substring(1, SAstfnRoot.length());
        }
        String SARootPartNormalized = SARootPart.replace(STFN_SEPARATOR, FILE_PATH_SEPARATOR);
        this.fileName = FILE_NAME_PREFIX + SARootPartNormalized + FILE_NAME_SUFFIX;
        if (templateFolder.charAt(templateFolder.length() - 1) != File.separatorChar)
        {
            this.templateFolder = templateFolder + File.separatorChar;
        }
        log.debug("Context created");
    }


    /**
     * @param SARoot
     */
    public void setSARoot(String SARoot)
    {
        this.SARoot = SARoot;
    }


    /**
     * Builds a file from the template using a default provided VelocityEngine
     * 
     * @throws TemplateException
     */
    public void buildFile() throws TemplateException
    {
        log.debug("Building the template using a symple velocity engine");
        this.buildFile(getSimpleVelocityEngine());
    }


    /**
     * Builds a file from the template using the VelocityEngine provided
     * 
     * @param engine a VelocityEngine
     * @throws TemplateException
     */
    public void buildFile(VelocityEngine engine) throws TemplateException
    {
        VelocityContext context = new VelocityContext();
        log.debug("Adding actual parameters to VelocityContext");
        context.put(SA_ROOT_PLACEHOLDER, this.SARoot);
        buildFileFromTemplate(engine, context, this.getFilePath());
    }


    /**
     * Builds a file from the template using the VelocityEngine and the context
     * containing the template variable mapping provided
     * 
     * @param engine a VelocityEngine
     * @param context a VelocityContext containing the mapping for the template variables
     * @param filePath the path to the file to be created
     * @throws TemplateException
     */
    protected void buildFileFromTemplate(VelocityEngine engine, VelocityContext context, String filePath) throws TemplateException
    {
        log.debug("Building file " + filePath + " from template " + StorageAreaContextTemplate.TEMPLATE_FILE_NAME);
        FileWriter fw = null;
        try
        {
            log.debug("Getting a file writer for file " + filePath);
            fw = new FileWriter(filePath);
        }
        catch (IOException exception)
        {
            log.error("Error creating a filewriter for the destination filepath " + filePath + " IOException: "
                    + exception.getMessage());
            throw new TemplateException("Error creating the filewriter" , exception);
        }
        try
        {
            log.debug("merging the template file with the provided context");
            engine.mergeTemplate(StorageAreaContextTemplate.TEMPLATE_FILE_NAME, FILE_ENCODING, context, fw);
        }
        catch (Exception e)
        {
            log.error("Unable to perform the template merge operation "
                    + ". " + e.getClass() + ": " + e.getMessage());
            throw new TemplateException("Unable to perform the template merge operation", e);
        }
        try
        {
            log.debug("Closing the filewriter");
            fw.close();
        }
        catch (IOException e)
        {
            log.error("Error in closing the filewriter. IOException: "
                    + e.getMessage());
            throw new TemplateException("Error in closing the filewriter", e);
        }
        log.debug("Context file created");
    }


    /**
     * Create a VelocityEngine setting the correct resource path
     * 
     * @return the created VelocityEngine
     */
    protected VelocityEngine getSimpleVelocityEngine() throws TemplateException
    {
        log.debug("Initializing a new Velocity engine");
        VelocityEngine engine = null;
        engine = new VelocityEngine();
        Properties propertyes = new Properties();
        log.debug("Setting property \'file.resource.loader.path\' to \'" + templateFolder + "\'");
        propertyes.put("file.resource.loader.path", templateFolder);
        log.debug("Setting property \'runtime.log\' to \'" + LOG_FILE_PATH + "\'");
        propertyes.put("runtime.log", LOG_FILE_PATH);
        try
        {
            engine.init(propertyes);
        }
        catch (Exception e)
        {
            log.error("Unable to initialize velocity template engine! " + e.getClass() + ": " + e.getMessage());
            throw new TemplateException("Unable to initialize velocity template engine", e);
        }
        log.debug("Velocity engine initialized correctly");
        return engine;
    }


    /**
     * @return the file path
     */
    public String getFilePath()
    {
        return this.directory + fileName;
    }
}
