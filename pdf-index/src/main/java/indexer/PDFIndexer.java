/*
 * Copyright (c) 2016 Chris Bellis
 * This software is subject to the MIT License, see LICENSE.txt in the root of the repository.
 */

package indexer;

import com.google.common.collect.Lists;
import documents.DocumentProvider;
import indexer_util.IndexerFields;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.FilteringTokenFilter;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.*;
import resources.StopwordsProvider;
import utils.StringManip;

import java.io.*;
import java.util.List;

/**
 * PDF Indexer, a hopefully self contained class to do all the PDF indexing.
 * Probably could use some refactoring, but overall, keeping this self-contained means we get less code sprawl
 * Created by chris on 4/18/16.
 */
public class PDFIndexer extends Indexer {
    private static final String INDEXER_NAME = "PDF Indexer";

    public PDFIndexer(String indexDirectory) throws IOException {
        super(INDEXER_NAME, new PDFProvider(), new PDFIndexerStrategy(indexDirectory));
    }

    /**
     * Add a CSV document to the provider so that it can be indexed
     * @param csvDocument the csv document to add from
     * @throws IOException exception is thrown if the file isn't there
     */
    public void addCsvDocument(String csvDocument) throws IOException{
        ((PDFProvider)provider).addDocumentsFromCsvFile(csvDocument);
    }

    /**
     * PDF Provided for the indexer, determines how PDFs will be provided to the indexer.
     */
    private static class PDFProvider extends DocumentProvider {
        void addDocumentsFromCsvFile(String file) throws IOException{
            Reader in = new FileReader(new File(file));
            CSVParser parser = CSVFormat.RFC4180.withHeader().parse(in);
            for(CSVRecord record : parser){
                addDocument(new PDFDocument(record));
            }
            newDocumentsAvailable();
        }
    }

    private static class PDFIndexerStrategy extends DocumentIndexerStrategy {
        private Analyzer pdfAnalyzer;
        private DirectoryReader reader;
        private IndexWriter writer;

        PDFIndexerStrategy(String indexDirectory) throws IOException {
            super(indexDirectory);
            pdfAnalyzer = new PDFAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(pdfAnalyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE); // TODO: Fix this to properly append
            writer = new IndexWriter(dir, iwc);
            reader = DirectoryReader.open(writer, true);
        }

        @Override
        public void indexDocument(documents.Document doc){
            super.indexDocument(doc);
            log.info("Indexed Document: " + doc.getName());
        }

        @Override
        protected org.apache.lucene.document.Document addCustomFields(org.apache.lucene.document.Document lDoc, documents.Document doc) {
            FieldType contentsType = getContentsFieldType();
            Field contents = new Field(IndexerFields.CONTENTS.name(), doc.getContents(), contentsType);
            lDoc.add(contents);
            return lDoc;
        }

        /**
         * Contents Field type, controls what is stored in the index part of "contents"
         * @return A Field type to be used for contents
         */
        private FieldType getContentsFieldType(){
            FieldType contentsType = new FieldType();
            contentsType.setStored(true);
            contentsType.setTokenized(true);
            contentsType.setStoreTermVectors(true);
            contentsType.setStoreTermVectorPositions(true);
            contentsType.setStoreTermVectorPayloads(true);
            contentsType.setStoreTermVectorOffsets(true);
            contentsType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
            return contentsType;
        }

        @Override
        protected IndexWriter getWriter() {
            return writer;
        }

        @Override
        protected IndexReader getReaderImpl() {
            try {
                if(!reader.isCurrent())
                    reader = DirectoryReader.openIfChanged(reader);
            } catch (IOException e) {
                log.error("Could not get new reader.");
            }
            return reader;
        }

        /**
         * Lucene Analyzer for PDF Documents, controls some of the details as to how the text is handled.
         */
        private static class PDFAnalyzer extends Analyzer{
            private final CharArraySet stop_set; // DO THIS ONCE!
            private static final Log log = LogFactory.getLog(PDFAnalyzer.class);

            PDFAnalyzer(){
                stop_set = StopFilter.makeStopSet(Lists.newArrayList(StopwordsProvider.getProvider().getStopwords()));
            }

            @Override
            protected Analyzer.TokenStreamComponents createComponents(String s) {
                StringReader reader = new StringReader(s);
                Tokenizer tokenizer = new StandardTokenizer();
                try {
                    tokenizer.setReader(reader);
                } catch (IOException e) {
                    log.error("Could not set reader on tokenizer. Threw IO exception");
                }

                TokenStream filter = new StandardFilter(tokenizer);
                filter = new LowerCaseFilter(filter);
                filter = new StopFilter(filter, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
                filter = new StopFilter(filter, stop_set);

                filter = new FilteringTokenFilter(filter) { // Number Filter
                    @Override
                    protected boolean accept() throws IOException {
                        String token =
                                this.input.getAttribute(CharTermAttribute.class).toString().trim();
                        return !StringManip.isNumeric(token);
                    }
                };

                filter = new FilteringTokenFilter(filter) { // Alphanumeric Filter
                    @Override
                    protected boolean accept() throws IOException {
                        String token =
                                this.input.getAttribute(CharTermAttribute.class).toString().trim();
                        return StringUtils.isAlphanumeric(token);
                    }
                };

                return new TokenStreamComponents(tokenizer, filter);
            }
        }

    }
}
