package servlets;

import indexer.Index;
import indexer.PDFIndexer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import resources.ConfigurationProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.IOException;

/**
 * Created by chris on 4/19/16.
 */
public class Startup extends HttpServlet {
    private final Log log = LogFactory.getLog(getClass());
    public Startup(){
        super();
        org.apache.log4j.BasicConfigurator.configure();
        org.apache.log4j.Logger.getLogger("org.apache.pdfbox").setLevel(Level.ERROR);
        org.apache.log4j.Logger.getLogger("org.apache.fontbox").setLevel(Level.ERROR);
    }

    public void init() throws ServletException{
        Index index = Index.createIndex(PDFIndexer.class, ConfigurationProvider.getInstance()
                .properties.getProperty(ConfigurationProvider.ConfigurationParameters.PDF_INDEX_DIR));
        index.getIndexer().start();
        try {
            ((PDFIndexer)index.getIndexer()).addCsvDocument(ConfigurationProvider.getInstance().properties.getProperty("PDF_CSV"));
        } catch (IOException | NullPointerException e) {
            log.error("Could not add CSV document.", e);
        }
    }
}
