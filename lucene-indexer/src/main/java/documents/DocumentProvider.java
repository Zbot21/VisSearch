/*
 * Copyright (c) 2016 Chris Bellis
 * This software is subject to the MIT License, see LICENSE.txt in the root of the repository.
 */

package documents;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by chris on 4/8/16.
 */
public class DocumentProvider {
    private List<DocumentProvidedListener> listeners;
    protected Log log;
    private Map<Document, Integer> docsAvailable;

    public DocumentProvider(){
        this.listeners = new ArrayList<>();
        docsAvailable = new HashMap<>();
        log = LogFactory.getLog(getClass());
    }

    public void newDocumentsAvailable(){
        for(DocumentProvidedListener listener : listeners){
            listener.documentAdded(DocumentProvidedListener.DocumentProvidedEventType.NEW_DOCUMENT);
        }
    }

    public void registerDocumentListener(DocumentProvidedListener listener){
        listeners.add(listener);
    }

    protected void addDocument(Document doc){
        addDocument(doc, false);
    }

    public void addDocument(Document doc, boolean notify){
        docsAvailable.put(doc, 0);

        if(notify)
            newDocumentsAvailable();
    }

    public void addDocuments(Collection<? extends Document> docs){
        docs.forEach(this::addDocument);
        newDocumentsAvailable();
    }


    public int documentsAvailable() {
        return docsAvailable.entrySet().stream()
                .collect(Collectors.summingInt(Map.Entry::getValue));
    }


    public void resetState() {
        docsAvailable.entrySet().stream().forEach(e -> docsAvailable.put(e.getKey(), 0));
    }


    public List<Document> getDocuments() {
        return getDocuments(docsAvailable.size());
    }


    public List<Document> getDocuments(long num) {
        return docsAvailable.entrySet().stream()
                .filter(e -> e.getValue() == 0)
                .limit(num)
                .map(m -> {
                    docsAvailable.put(m.getKey(), 1); // Tag the entries as being read
                    return m;
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
