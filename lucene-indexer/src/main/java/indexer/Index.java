package indexer;

import indexer_util.IndexerFields;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.lucene.search.NumericRangeQuery.*;

/**
 * Created by chris on 4/18/16.
 */
public abstract class Index {
    protected final Log log = LogFactory.getLog(getClass());

    public abstract Indexer getIndexer();
    public List<IndexDocument> runQuery(Query q, int maxResults){
        IndexReader reader = getIndexer().getReader();
        IndexSearcher searcher = new IndexSearcher(reader);
        try{
            TopDocs docs = searcher.search(q, maxResults);
            return Arrays.asList(docs.scoreDocs).stream()
                    .map(s -> {
                        try {
                            int uniqueId = Integer.parseInt(reader.document(s.doc).get(IndexerFields.ID.name()));
                            return new IndexDocument(uniqueId, s.doc, s.score);
                        } catch (IOException e) {
                            log.error("Error while converting document", e);
                            return null;
                        }
                    }).filter(d -> d != null)
                    .collect(Collectors.toList());

        }catch(IOException e){
            log.error("There was an error while attempting a search.", e);
            return Collections.checkedList(new ArrayList<>(), IndexDocument.class);
        }
    }

    public Document getDocument(int id){
        IndexReader reader = getIndexer().getReader();
        IndexSearcher searcher = new IndexSearcher(reader);
        Query q = newIntRange(IndexerFields.ID.name(), id, id, true, true);
        try {
            return reader.document(searcher.search(q, 1).scoreDocs[0].doc);
        } catch (IOException | IndexOutOfBoundsException e) {
            log.error("Could not obtain a document for id #" + id, e);
            return null;
        }
    }

    public static Index getIndex(Indexer i){
        return new Index() {
            @Override
            public Indexer getIndexer() {
                return i;
            }
        };
    }

    public static Index createIndex(Class<? extends Indexer> i, String indexDirectory){
        try {
            Indexer indexer = i.getConstructor(String.class).newInstance(indexDirectory);
            return getIndex(indexer);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LogFactory.getLog(Index.class).error("Could not create index.", e);
            return null;
        }
    }


    public class IndexDocument {
        public final int id;
        public final int lucene_id;
        public final double score;
        IndexDocument(int id, int lucene_id, double score){
            this.id = id;
            this.lucene_id = lucene_id;
            this.score = score;
        }
    }
}
