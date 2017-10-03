package edu.utexas.arlut.ciads;

import java.io.IOException;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

@Slf4j
public class App {
    public static void main(String[] args) throws IOException {

        StandardAnalyzer analyzer = new StandardAnalyzer();
        KeywordAnalyzer kwa = new KeywordAnalyzer();
        Directory index = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(kwa);
        IndexWriter writer = new IndexWriter(index, config);


//        TrackingIndexWriter trackingIndexWriter = new TrackingIndexWriter(writer);
        final ReferenceManager<IndexSearcher> sm = new SearcherManager(writer, true, null);
        log.info("ReferenceManager<IndexSearcher> {}", sm);
        writer.commit();
        addDoc(writer, sm, ImmutableMap.of("id", Integer.valueOf(1), "title", "A", "john", "193398817"));
//        writer.commit();
        addDoc(writer, sm, ImmutableMap.of("title", "B", "paul", "55320055Z"));


        dumpDynamic(sm);
//        writer.rollback();
//
//
//        writer = new IndexWriter(index, new IndexWriterConfig(kwa));
////        final ReferenceManager<IndexSearcher> sm0 = new SearcherManager(writer, true, null);
//        dumpDynamic(sm);

//        IndexReader reader = DirectoryReader.open(index);
//        IndexSearcher s0 = new IndexSearcher(reader);
//        dumpStatic(s0); // static


//        addDoc(writer, sm, ImmutableMap.of("title", "C", "ringo", "55063554A"));
//        addDoc(writer, sm, ImmutableMap.of("title", "D", "george", "9900333X"));
//        writer.close();


//        IndexReader reader = DirectoryReader.open(index);
//        IndexSearcher s0 = new IndexSearcher(reader);
//        dumpStatic(s0); // static
//        log.info("=====");
//        dumpDynamic(sm); // dynamic
//
//        log.info("");
//
//        writer.commit();
//        dumpStatic(new IndexSearcher(DirectoryReader.open(index))); // static
//        log.info("=====");
//        dumpDynamic(sm); // dynamic

        writer.close();


    }
    // =================================
    private static void addDoc(IndexWriter w, ReferenceManager<?> sm, Map<String, Object> d) throws IOException {
        Document doc = new Document();
        for (Map.Entry<String, Object> e: d.entrySet()) {
            Object val = e.getValue();
            if (val instanceof String) {
                doc.add(new StringField(e.getKey(), (String)e.getValue(), Field.Store.YES));
            } else if (val instanceof Float) {
                doc.add(new FloatField(e.getKey(), (Float)e.getValue(), Field.Store.YES));
            }
        }
//        doc.add(new TextField("title", title, Field.Store.YES));
//
//        // use a string field for isbn because we don't want it tokenized
//        doc.add(new StringField("isbn", isbn, Field.Store.YES));
        w.addDocument(doc);
        sm.maybeRefresh();
    }
    // =================================
    static Query allDocsQ = new MatchAllDocsQuery();
    private static void dumpStatic(IndexSearcher s) throws IOException {
        log.info("Dump: {}", s);
        TopDocs docs = s.search(allDocsQ, 10);
        for (ScoreDoc sd: docs.scoreDocs) {
            Document d = s.doc(sd.doc);
            log.info("ScoreDoc {}", d);
        }
    }
    private static void dumpDynamic(ReferenceManager<IndexSearcher> sm) throws IOException {
        log.info("ReferenceManager<IndexSearcher> {}", sm);
        try (CloseableIndexSearcher cis = CloseableIndexSearcher.acquire(sm)) {
            IndexSearcher s = cis.get();
            log.info("Dump: {}", s);
            TopDocs docs = s.search(allDocsQ, 10);
            for (ScoreDoc sd : docs.scoreDocs) {
                Document d = s.doc(sd.doc);
                log.info("\tScoreDoc {}", d);
            }
        }
    }


    private static final long serialVersionUID = 1L;
}
