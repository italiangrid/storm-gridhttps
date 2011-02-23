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
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import it.grid.storm.gridhttps.Configuration;

/**
 * @author Michele Dibenedetto
 */
public class StorageAreaContextTemplate
{


//    private static final Logger logger = Logger.getLogger(StorageAreaContextTemplate.class);
    /**
     * The name of the template to be used
     */
    private static final String TEMPLATE_FILE_NAME = "context.vm";
    private static final String SA_ROOT_PLACEHOLDER = "SA_Root";
    /**
     * The name of the file to be created
     */
    private static final String FILE_NAME_PREFIX = Configuration.CONTEXT_FILE_NAME_PREFIX + "#";
    private static final String FILE_NAME_SUFFIX = ".xml";
    private String SARoot = null;
    private final String fileName;
    /**
     * The directory where to store the generated file
     */
    private String directory;
    private String templateFolder;


    public StorageAreaContextTemplate(String directory, String SAstfnRoot, String templateFolder)
    {
        System.out.println("DEBUG: Creating storage area context template for stfnRoot \'" + SAstfnRoot + "\' in folder " + directory
                + " using teplate " + TEMPLATE_FILE_NAME + " in folder " + templateFolder);
        if (directory.charAt(directory.length() - 1) != File.separatorChar)
        {
            this.directory = directory + File.separatorChar;
        }
        String SARootPart = SAstfnRoot;
        if (SAstfnRoot.charAt(0) == File.separatorChar)
        {
            SARootPart = SAstfnRoot.substring(1, SAstfnRoot.length());
        }
        this.fileName = FILE_NAME_PREFIX + SARootPart + FILE_NAME_SUFFIX;
        if (templateFolder.charAt(templateFolder.length() - 1) != File.separatorChar)
        {
            this.templateFolder = templateFolder + File.separatorChar;
        }
        System.out.println("DEBUG: Context created");
    }


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
        this.buildFile(getSimpleVelocityEngine());
    }


    /**
     * Builds a file from the template using the VelocityEngine provided
     * 
     * @param engine
     *            a VelocityEngine
     * @throws UploadException
     */
    public void buildFile(VelocityEngine engine) throws TemplateException
    {
        VelocityContext context = new VelocityContext();
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
     * @throws eu.eticsproject.submission.SubmitFault
     */
    protected void buildFileFromTemplate(VelocityEngine engine, VelocityContext context, String filePath) throws TemplateException
    {
        System.out.println("DEBUG: Building file " + filePath + " from template " + StorageAreaContextTemplate.TEMPLATE_FILE_NAME);
        FileWriter fw = null;
        try
        {
            fw = new FileWriter(filePath);
        }
        catch (IOException exception)
        {
            System.out.println("ERROR: An I/O exception occurred in creating a filewriter " + "for the destination filepath " + filePath + " Error: "
                    + exception.getMessage());
            throw new TemplateException("An I/O exception occurred in creating a filewriter " + "for the destination filepath " + filePath
                    + " Error: " + exception.getMessage());
        }
        try
        {
            engine.mergeTemplate(StorageAreaContextTemplate.TEMPLATE_FILE_NAME, "UTF-8", context, fw);
        }
        catch (Exception e)
        {
            System.out.println("ERROR: An exception " + e.getClass() + " occurred during the template merge operation " + "in building file " + filePath
                    + ". Error: " + e.getMessage());
            throw new TemplateException("An exception " + e.getClass() + " occurred during the template merge operation "
                    + "in building file " + filePath + ". Error: " + e.getMessage());
        }
        try
        {
            fw.close();
        }
        catch (IOException exception)
        {
            System.out.println("ERROR: An I/O exception occurred in closing the filewriter " + "of the destination filepath " + filePath + " Error: "
                    + exception.getMessage());
            throw new TemplateException("An I/O exception occurred closing the filewriter " + "of the destination filepath " + filePath
                    + " Error: " + exception.getMessage());
        }
        System.out.println("DEBUG: template created");
    }


    /**
     * Create a VelocityEngine setting the correct resource path
     * 
     * @return the created VelocityEngine
     */
    protected VelocityEngine getSimpleVelocityEngine() throws TemplateException
    {
        System.out.println("DEBUG: Initializing a new Velocity engine");
        VelocityEngine engine = null;
        engine = new VelocityEngine();
        Properties propertyes = new Properties();
        propertyes.put("file.resource.loader.path", templateFolder);
        try
        {
            engine.init(propertyes);
        }
        catch (Exception e)
        {
            System.out.println("ERROR: An exception " + e.getClass() + " occurred when initializing velocity template engine! Error: " + e.getMessage());
            throw new TemplateException("An exception " + e.getClass() + " occurred when initializing velocity template engine! Error: "
                    + e.getMessage());
        }
        System.out.println("DEBUG: Velocity engine initialized");
        return engine;
    }


    public String getFilePath()
    {
        return this.directory + fileName;
    }
}
