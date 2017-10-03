// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

@Slf4j
public class Store {
    Store() throws IOException {

        index = new RAMDirectory();
//        index = FSDirectory.open(Paths.get("d"));


        writer = getWriter();
        writer.commit();

//        sm = new SearcherManager(getWriter(), true, null);
        log.info("ctor {} {}", getWriter(), sm);

    }

    // =================================
    Directory index;
    private IndexWriter writer;
    KeywordAnalyzer kwa = new KeywordAnalyzer();
    ReferenceManager<IndexSearcher> sm;
    // =================================
    private IndexWriter getWriter() throws IOException {
        if (null != writer && writer.isOpen())
            return writer;

        log.info("creating new IndexWriter");
        IndexWriterConfig config = new IndexWriterConfig(kwa);
//        config.setCodec(new SimpleTextCodec());
        writer = new IndexWriter(index, config);
        sm = new SearcherManager(writer, true, null);
        return writer;
    }
    // =================================

    void dump() {
        log.info("baseline");
        for (Map.Entry<String, XVertex> e : vCache.entrySet())
            log.info("\t{} => {}", e.getKey(), e.getValue());
        log.info("XAction");
        for (Map.Entry<String, XVertex> e : mutatedVertices.entrySet())
            log.info("\t+ {} => {}", e.getKey(), e.getValue());
        log.info("\t- {}", deletedVertices);
    }

    XVertex addVertex(final String id) throws IOException {
        XVertex v = new XVertex(this, id);
        mutatedVertices.put(id, v);
//        Document d = index(v);
//        log.info("Document {}", d);
//        log.info("Writer {}", getWriter());

        indexID(id);
        return v;
    }
    void rmVertex(final XVertex v) throws IOException {
        deletedVertices.add(v.id);
        Term t = new Term("id", v.getId());
        getWriter().deleteDocuments(t);
        log.info("delete via {}", t);
        sm.maybeRefresh();
    }
    XVertex getVertex(String id) {
        if (deletedVertices.contains(id))
            return null;
        if (mutatedVertices.containsKey(id))
            return mutatedVertices.get(id);
        return vCache.get(id);
    }
    Iterable<XVertex> search(Query q) throws IOException {
        log.info("Writer {} {}", getWriter(), sm);
//        sm = new SearcherManager(getWriter(), true, null);
//        log.info("Writer {} {}", getWriter(), sm);


        try (CloseableIndexSearcher cis = CloseableIndexSearcher.acquire(sm)) {
            IndexSearcher s = cis.get();
            log.info("Dump: {}", s);
            TopDocs docs = s.search(q, 10);
            for (ScoreDoc sd : docs.scoreDocs) {
                Document d = s.doc(sd.doc);
                log.info("\tScoreDoc {}", d);
            }
        } catch (IOException e) {

        }
        return Collections.emptyList();
    }
    <T extends XElement> void indexID(String id) throws IOException {
        Document doc = new Document();
        doc.add(new StringField("id", id, Field.Store.YES));
        getWriter().addDocument(doc);
        sm.maybeRefresh();
    }
    <T extends XElement> void index(T v) throws IOException {
        log.info("index of type {}", v.getClass().getSimpleName());
        List<IndexableField> fields = newArrayList();
        fields.add(new StringField("id", v.getId(), Field.Store.YES));
        for (Map.Entry<String, Object> e : v) {
            Object val = e.getValue();
            if (e.getKey().equalsIgnoreCase("id")) {
                // pass

            } else if (val instanceof String) {
                fields.add(new StringField(e.getKey(), (String)e.getValue(), Field.Store.NO));

            } else if (val instanceof Float) {
                fields.add(new FloatField(e.getKey(), (Float)e.getValue(), Field.Store.NO));

            } else if (val instanceof Long) {
                fields.add(new LongField(e.getKey(), (Long)e.getValue(), Field.Store.NO));

            } else if (val instanceof Integer) {
                fields.add(new IntField(e.getKey(), (Integer)e.getValue(), Field.Store.NO));

            } else if (val instanceof Double) {
                fields.add(new DoubleField(e.getKey(), (Double)e.getValue(), Field.Store.NO));

            } else if (val instanceof Number) {
                fields.add(new LongField(e.getKey(), (Long)e.getValue(), Field.Store.NO));
            }
        }
        getWriter().updateDocument(new Term("id", v.getId()), fields);
//        getWriter().addDocument(doc);
        sm.maybeRefresh();
    }

    private void clear() {
        mutatedVertices.clear();
        deletedVertices.clear();
    }
    void commit() throws IOException {
        vCache.keySet().removeAll(deletedVertices);
        vCache.putAll(mutatedVertices);
        getWriter().commit();
        sm.maybeRefresh();
        clear();
    }
    void rollback() throws IOException {
        getWriter().rollback();
        getWriter();
        sm.maybeRefresh();
        clear();
    }
    void shutdown() throws IOException {
        writer.close();
    }

    Map<String, XVertex> vCache = newHashMap();

    Map<String, XVertex> mutatedVertices = newHashMap();
    Set<String> deletedVertices = newHashSet();

}
