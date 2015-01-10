package com.publicationmetasearchengine.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.log4j.Logger;

public class ZipUtils {
    public static final Logger LOGGER = Logger.getLogger(ZipUtils.class);

    public static File zipPDFStreams(Map<String, InputStream> files, String userName) throws ZipException {
        String publicationsFileName = userName+"_publications.zip";
        File publicationsFile = new File(publicationsFileName);
        if (publicationsFile.exists()) {
            LOGGER.debug(publicationsFileName + " exists");
            if (publicationsFile.delete())
                LOGGER.debug(publicationsFileName + " deleted");
            else
                LOGGER.warn(publicationsFileName + " not deleted");
        }

        ZipFile zipFile = new ZipFile(publicationsFile);
        try {
            LOGGER.info(String.format("Zip file %s created", publicationsFile.getCanonicalPath()));
        } catch (IOException ex) {
            LOGGER.info(String.format("Zip file %s created", publicationsFile.getAbsolutePath()));
        }

        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        parameters.setSourceExternalStream(true);

        for (Map.Entry<String, InputStream> file : files.entrySet()) {
            final String fileName = file.getKey()+".pdf";
            parameters.setFileNameInZip(fileName);
            zipFile.addStream(file.getValue(), parameters);
            LOGGER.debug(fileName + " added");
        }
        LOGGER.info("All required files added");
        return publicationsFile;
    }
}
