/*
 * Copyright (c) 2016 Chris Bellis
 * This software is subject to the MIT License, see LICENSE.txt in the root of the repository.
 */

package indexer;

import documents.Document;
import documents.DocumentProvidedListener;
import documents.DocumentProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import resources.ApplicationThreadPool;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by chris on 4/8/16.
 */
public class Indexer implements DocumentProvidedListener{
    private static final Log log = LogFactory.getLog(Indexer.class);
    private String indexerName;
    protected DocumentProvider provider;
    private BlockingQueue<Document> docs;
    private IndexThread indexerThread;
    private DocumentIndexerStrategy strategy;

    public Indexer(String indexerName,
                   DocumentProvider provider,
                   DocumentIndexerStrategy strategy){
        this.indexerName = indexerName;
        this.provider = provider;
        this.indexerThread = new IndexThread();
        this.strategy = strategy;
        provider.registerDocumentListener(this);
        docs = new LinkedBlockingQueue<>();
        docs.addAll(provider.getDocuments()); // Add all the documents initiallly
    }

    public void start(){
        indexerThread.start();
    }

    public void stopIndexer() {
        indexerThread.stopIndexing();
    }

    public boolean isIndexing() { return indexerThread.isIndexing(); }

    public DocumentProvider getProvider(){
        return provider;
    }

    public IndexReader getReader(){
        return strategy.getReader();
    }

    public int queuedDocuments() {
        return docs.size();
    }

    @Override
    public void documentAdded(DocumentProvidedEventType e) {
        if(e == DocumentProvidedEventType.NEW_DOCUMENT){
            List<Document> docs = provider.getDocuments();
            log.info("Added: " + docs.size() + " documents to index queue.");
            this.docs.addAll(docs);
        }
    }

    private class IndexThread extends Thread {
        private boolean running;
        IndexThread(){
            super(indexerName);
            running = false;
        }

        public void run(){
            running = true;
            // indexer.Index the documents
            while(running){
                try {
                    Document doc = docs.take();
                    ApplicationThreadPool.getInstance().getPool().submit(() -> strategy.indexDocument(doc));
                } catch (InterruptedException e) {
                    log.error("Error while generating task.", e);
                }
            }
        }

        boolean isIndexing(){
            return running;
        }

        void stopIndexing(){
            running = false;
        }
    }
}
