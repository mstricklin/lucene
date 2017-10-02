// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import java.io.IOException;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ReferenceManager;

public class CloseableIndexSearcher implements AutoCloseable {
    public static CloseableIndexSearcher acquire(ReferenceManager<IndexSearcher> sm) {
        return new CloseableIndexSearcher(sm);
    }
    private CloseableIndexSearcher(ReferenceManager<IndexSearcher> sm) {
        this.sm = sm;
    }
    public IndexSearcher get() throws IOException {
        is = sm.acquire();
        return is;
    }
    @Override
    public void close() throws IOException {
        sm.release(is);
    }
    private final ReferenceManager<IndexSearcher> sm;
    private IndexSearcher is;
}
