package indexer;

import indexer_util.IndexerFields;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by chris on 4/8/16.
 */
public abstract class DocumentIndexerStrategy {
    protected Log log;
    protected Directory dir;

    private static AtomicInteger nextId = new AtomicInteger(1);
    public DocumentIndexerStrategy(String indexDirectory) throws IOException{
        dir = FSDirectory.open(Paths.get(indexDirectory));
        log = LogFactory.getLog(getClass());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                log.info("Closing index writer.");
                getWriter().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    protected abstract Document addCustomFields(Document lDoc, documents.Document doc);
    public IndexReader getReader(){
        try{
            getWriter().commit();
        }catch(IOException e){
            log.error("Failed to commit changes.", e);
        }

        return getReaderImpl();
    }

    protected abstract IndexWriter getWriter();
    protected abstract IndexReader getReaderImpl();

    public void indexDocument(documents.Document doc){
        Document lDoc = indexMetadata(doc);
        lDoc.add(new IntField(IndexerFields.ID.name(), nextId.getAndIncrement(), Field.Store.YES));
        lDoc = addCustomFields(lDoc, doc);
        writeToIndex(lDoc);
    }

    protected Document indexMetadata(documents.Document doc){
        Document lDoc = new Document();
        for(String field : doc.getAllFields()){
            lDoc.add(new StringField(field, doc.getMetadata(field), Field.Store.YES));
        }
        return lDoc;
    }

    protected void writeToIndex(Document doc){
        try {
            getWriter().addDocument(doc);
        } catch (IOException e) {
            log.error("Could not add document to index.", e);
        }
    }
}
