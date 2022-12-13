package edu.yu.cs.com1320.project.stage1.impl;

import edu.yu.cs.com1320.project.stage1.Document;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class DocumentImplTest {

    URI txtUri = URI.create("doc1.txt");
    Document doc1 = new DocumentImpl(txtUri, "This is doc1");

    URI jpegUri = URI.create("doc2.jpeg");
    byte[] bytes = "This is an image".getBytes();
    Document doc2 = new DocumentImpl(jpegUri, bytes);

    @Test
    void testConstructor() {
        assertThrows(IllegalArgumentException.class, ()->{
            new DocumentImpl(txtUri, (String)null);
        });

        assertThrows(IllegalArgumentException.class, ()->{
            new DocumentImpl((URI)null, "Hello");
        });

        assertThrows(IllegalArgumentException.class, ()->{
            new DocumentImpl(jpegUri, (byte[])null );
        });

        assertThrows(IllegalArgumentException.class, ()->{
            new DocumentImpl((URI)null, "image");
        });
    }

    @Test
    void getDocumentTxt() {
        assertEquals("This is doc1", doc1.getDocumentTxt(), "The texts do not match");
    }

    @Test
    void getDocumentBinaryData() {
        assertEquals(bytes, doc2.getDocumentBinaryData(), "The byte data does not match");
    }

    @Test
    void getKey() {
        assertEquals(txtUri, doc1.getKey(), "The uri does not match");
        assertEquals(jpegUri, doc2.getKey(), "The uri does not match");
    }

    @Test
    void testEquals() {
        assertEquals(false, doc1.equals(doc2), "The docs are equal");
        assertEquals(false, doc2.equals(doc1), "The docs are equal");
        assertEquals(true, doc1.equals(doc1), "The docs are not equal");
        assertEquals(true, doc2.equals(doc2), "The docs are not equal");
    }

    @Test
    void testHashCode() {
    }
}