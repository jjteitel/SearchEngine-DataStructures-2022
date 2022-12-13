package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class DocumentPersistenceManagerTest {

    @Test
    void serialize() throws IOException {
        PersistenceManager pm = new DocumentPersistenceManager(null);
        URI uri = URI.create("http://www.yu.edu/documents/doc1");
        Document doc = new DocumentImpl(uri, "google", null);
        pm.serialize(uri, doc);
    }

    @Test
    void deserialize() throws IOException {
        PersistenceManager pm = new DocumentPersistenceManager(null);
        URI uri = URI.create("http://www.google.com");
        Document doc = new DocumentImpl(uri, "google", null);
        pm.serialize(uri, doc);
        Document doc2 = (Document) pm.deserialize(uri);
        assertEquals(doc, doc2);
    }

    @Test
    void delete() throws IOException {
        PersistenceManager pm = new DocumentPersistenceManager(null);
        URI uri = URI.create("http://www.yu.edu/documents/doc1");
        Document doc = new DocumentImpl(uri, "google", null);
        pm.serialize(uri, doc);
        pm.delete(uri);
    }
}