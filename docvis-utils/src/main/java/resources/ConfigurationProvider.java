package resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Created by chris on 4/18/16.
 */
public class ConfigurationProvider {
    private static final Log log = LogFactory.getLog(ConfigurationProvider.class);
    private static ConfigurationProvider ourInstance = new ConfigurationProvider();

    public static ConfigurationProvider getInstance() {
        return ourInstance;
    }
    private static final String NOT_AVAILABLE = "NOT_AVAILABLE";
    public final Properties properties;

    private ConfigurationProvider() {
        String configFile = System.getProperty("docvis.config", NOT_AVAILABLE);
        if(configFile.equals(NOT_AVAILABLE) || !Files.exists(Paths.get(configFile))){
            log.fatal("Configuration Not Present!");
            System.exit(1); // If the configuration is not present, literally blow the computer to pieces
        }
        properties = new Properties();
        try{
            InputStream in = new FileInputStream(configFile);
            properties.load(in);
            in.close();
        } catch (IOException e) {
            log.fatal("There was an error loading the configuration file", e);
            System.exit(1);
        }
    }

    public class ConfigurationParameters {
        public static final String STOPWORDS_FILE = "STOPWORDS_FILE";
        public static final String PDF_INDEX_DIR = "PDF_INDEX_DIR";
    }
}

