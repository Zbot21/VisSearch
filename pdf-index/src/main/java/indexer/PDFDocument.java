/*
 * Copyright (c) 2016 Chris Bellis
 * This software is subject to the MIT License, see LICENSE.txt in the root of the repository.
 */

package indexer;

import com.google.common.collect.Lists;
import documents.Document;
import indexer_util.IndexerFields;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by chris on 4/21/16.
 */
public class PDFDocument extends Document {
    private Log log = LogFactory.getLog(getClass());
    public static String[] HIDDEN_FIELDS = {"path"};
    private String path;
    private String name;

    PDFDocument(CSVRecord record) {
        record.toMap().forEach(this::addMetadata);
        path = record.get("file");
        name = record.get("title");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getContents() {
        PDFTextExtractor extractor = new PDFTextExtractor();
        try {
            return extractor.extractText(Paths.get(path).toString());
        } catch (IOException e) {
            log.error("Error while extracting text from: " + getName());
        }
        return StringUtils.EMPTY;
    }

    @Override
    public List<String> hiddenFields() {
        return Lists.newArrayList(HIDDEN_FIELDS);
    }
}
