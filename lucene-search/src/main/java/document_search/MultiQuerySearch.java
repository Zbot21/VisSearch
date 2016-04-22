/*
 * Copyright (c) 2016 Chris Bellis
 * This software is subject to the MIT License, see LICENSE.txt in the root of the repository.
 */

package document_search;

import indexer.Index;
import lucene_analyzers.SearchAnalyzer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import results.MultiQueryResults;
import results.QueryResults;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by chris on 4/20/16.
 */
public class MultiQuerySearch {
    private static final Log log = LogFactory.getLog(MultiQuerySearch.class);

    public static List<MultiQueryResults> search(Index i, int docLimit, String searchField, String... queries){
        return search(i, docLimit, searchField, new SearchAnalyzer(WhitespaceTokenizer.class), queries); // TODO: add ability to set default analyzer
    }

    public static List<MultiQueryResults> search(Index i, int docLimit, String searchField, Analyzer a, String... queries){
        ParseWrapper parser = new ParseWrapper(new QueryParser(searchField, a));

        // Parse all available results
        List<QueryPair> queryList = Arrays.asList(queries).stream()
                .map(parser::parseQuery)
                .filter(query -> query != null)
                .collect(Collectors.toList());

        // Create the overall query
        BooleanQuery query = new BooleanQuery();
        for(QueryPair pair : queryList){
            query.add(pair.query, BooleanClause.Occur.SHOULD);
        }

        // FIXME: We should not have index searchers here, but we still do! :-( Fuck Lucene and its vast plots of, features...
        IndexSearcher searcher = new IndexSearcher(i.getIndexer().getReader());

        // TODO: Refactor this because it looks terrible, functional style is better, but the functional isn't good...
        List<MultiQueryResults> queryResults = Collections.checkedList(new ArrayList<>(), MultiQueryResults.class);
        for(Index.IndexDocument doc : i.runQuery(query, docLimit)){
            // Create new multi query results
            MultiQueryResults results = new MultiQueryResults(doc.id, doc.score, Arrays.asList(queries));

            // Explain the individual query results
            for(QueryPair queryPair : queryList){
                try{
                    double score = searcher.explain(queryPair.query, doc.lucene_id).getValue();
                    QueryResults individualResults = new QueryResults(doc.id, queryPair.term, score);
                    results.addQueryResult(individualResults);
                }catch(IOException e){
                    e.printStackTrace();
                }
            }

            // Add the Multi-query results to the overall list
            queryResults.add(results);
        }
        return queryResults;
    }

    private static class ParseWrapper{
        private QueryParser parser;
        ParseWrapper(QueryParser parser){
            this.parser = parser;
        }
        QueryPair parseQuery(String q){
            try{
                return new QueryPair(q, parser.parse(q));
            } catch (ParseException e) {
                e.printStackTrace(); // TODO: Better error handling
            }
            return null;
        }
    }

    private static class QueryPair{
        final String term;
        final Query query;
        QueryPair(String term, Query q){
            this.term = term;
            this.query = q;
        }
    }
}
