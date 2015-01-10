package com.publicationmetasearchengine.dao.properties;

import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;
import org.apache.log4j.Logger;

public class PropertiesManager implements Serializable{
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(PropertiesManager.class);

    private static PropertiesManager instance = null;
    private final Properties properties;

    private PropertiesManager(){
        properties = new Properties();
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream("app.properties"));
            LOGGER.info("Initialized");
        } catch (IOException e) {
            LOGGER.fatal("Error reading propertirs", e);
        }
    }

    public String getProperty(String name){
        return properties.getProperty(name);
    }

    public String getProperty(String name, String defaultValue){
        return properties.getProperty(name, defaultValue);
    }

    public static PropertiesManager getInstance(){
        if (instance == null){
            instance = new PropertiesManager();
        }
        return instance;
    }
}
