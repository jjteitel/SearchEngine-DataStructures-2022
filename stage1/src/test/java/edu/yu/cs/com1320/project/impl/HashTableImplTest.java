package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.stage1.Document;
import edu.yu.cs.com1320.project.stage1.impl.DocumentImpl;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class HashTableImplTest {

    @Test
    void get() {
        HashTable table = new HashTableImpl();

        URI u = URI.create("doc1");
        Document doc1 = new DocumentImpl(u, "this is doc1");
        table.put(u, doc1);
        assertEquals(doc1, table.get(u), "This should be doc1");

        URI fakeU = URI.create("fakeDoc");
        assertEquals(null, table.get(fakeU), "This should return null");
    }

    @Test
    void put() {
        HashTable table = new HashTableImpl();

        URI u = URI.create("A");
        Document doc1 = new DocumentImpl(u, "this is doc1");
        System.out.println((u.hashCode() & 0x7fffffff) % 5);
        assertEquals(null, table.put(u, doc1), "This should be null");
        table.put(u, doc1);
        assert table.get(u) != null : "there is no doc stored at this index";
        Document doc2 = new DocumentImpl(u, "This is 1doc");
        System.out.println((u.hashCode() & 0x7fffffff) % 5);
        assertEquals(doc1, table.put(u, doc2), "This should be doc2");

        URI u2 = URI.create("G");
        Document doc3 = new DocumentImpl(u2, "this is doc 3");
        table.put(u2, doc3);
        assertEquals(doc3, table.get(u2), "This should be doc3");

        System.out.println((doc3.hashCode() & 0x7fffffff) % 5);

        table.put(u, doc2);
        assertEquals(doc2, table.get(u), "This should be doc2");

        URI u3 = URI.create("F");
        Document doc4 = new DocumentImpl(u3, "this is doc1");
        System.out.println((u3.hashCode() & 0x7fffffff) % 5);
        assertEquals(null, table.put(u3, doc4), "this should be null");
        table.put(u3, doc4);
        assertEquals(doc4, table.get(u3), "this should be doc4");
        table.put(u, null);
        assertEquals(null, table.get(u), "this should be null");
        assertEquals(doc4, table.get(u3), "this should be doc4");
    }
}