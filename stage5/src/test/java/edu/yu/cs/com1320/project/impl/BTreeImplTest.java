package edu.yu.cs.com1320.project.impl;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class BTreeImplTest {

    @Test
    void get() throws Exception {
        URI uri = new URI("hello");
        BTreeImpl bTree = new BTreeImpl();
        bTree.put(uri, "hello");
        assertEquals("hello", bTree.get(uri));
    }

    @Test
    void put() throws Exception {
        URI uri = new URI("hello");
        BTreeImpl bTree = new BTreeImpl();
        bTree.put(uri, "hello");
        assertEquals("hello", bTree.put(uri, "hi"));
    }

    @Test
    void moveToDisk() {
    }

    @Test
    void setPersistenceManager() {
    }
}