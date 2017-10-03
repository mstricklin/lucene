// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;

@Slf4j
public class GraphStore {
    static Query allDocsQ = new MatchAllDocsQuery();

    public static void main(String[] args) throws IOException, InterruptedException {
        Store s = new Store();
        XVertex v1 = s.addVertex("001");
        v1.setProperty("aaa", "aOne");
        s.rollback();
        XVertex v2 = s.addVertex("002");
        v2.setProperty("bbb", "bTwo");
        XVertex v3 = s.addVertex("003");
        v3.setProperty("bbb", "bThree");
        v3.setProperty("ccc", "cThree");

        Thread.sleep(500);
        log.info("==============");
        s.search(allDocsQ);
        log.info("==============");
        s.commit();
        s.search(allDocsQ);
        log.info("==============");
//        s.rmVertex(v2);
//        s.commit();
//        Thread.sleep(500);
//        s.search(allDocsQ);
//        log.info("==============");


        s.dump();
        s.shutdown();
    }
}
