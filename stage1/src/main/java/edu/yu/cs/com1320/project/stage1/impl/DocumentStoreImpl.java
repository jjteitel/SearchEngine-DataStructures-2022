package edu.yu.cs.com1320.project.stage1.impl;

import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage1.Document;
import edu.yu.cs.com1320.project.stage1.DocumentStore;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class DocumentStoreImpl implements DocumentStore {
    HashTableImpl table;

    public DocumentStoreImpl() {
        this.table = new HashTableImpl<>();
    }

    @Override
    public int putDocument(InputStream input, URI uri, DocumentFormat format) throws IOException {
        if (uri == null || format == null) {
            throw new IllegalArgumentException();
        }
        int hash = 0;
        if (input == null) {
            hash = getDocument(uri).hashCode();
            deleteDocument(uri);
        }
        byte[] bytes;
        bytes = input.readAllBytes();
        if (format == DocumentFormat.BINARY) {
            Document doc = new DocumentImpl(uri, bytes);
            if (getDocument(uri) != null) {
                hash = getDocument(uri).hashCode();
            }
            this.table.put(uri, doc);
        }
        if (format == DocumentFormat.TXT) {
            String text = new String(bytes);
            Document doc = new DocumentImpl(uri, text);
            if (getDocument(uri) != null) {
                hash = getDocument(uri).hashCode();
            }
            this.table.put(uri, doc);
        }
        return hash;
    }

    @Override
    public Document getDocument(URI uri) {
        return (Document)this.table.get(uri);
    }

    @Override
    public boolean deleteDocument(URI uri) {
        if (getDocument(uri) != null) {
            this.table.put(uri, null);
            return true;
        }
        return false;
    }
}
