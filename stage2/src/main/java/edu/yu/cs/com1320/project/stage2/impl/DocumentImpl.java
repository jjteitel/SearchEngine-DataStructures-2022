package edu.yu.cs.com1320.project.stage2.impl;

import edu.yu.cs.com1320.project.stage2.Document;

import java.net.URI;
import java.util.Arrays;

public class DocumentImpl implements Document {
    private final URI uri;
    private String txt;
    private byte[] binaryData;

    public DocumentImpl(URI uri, String txt) {
        if (txt == null || txt.length() == 0) {
            throw new IllegalArgumentException();
        }else if (uri == null || uri.toString().length() == 0) {
            throw new IllegalArgumentException();
        }
        this.uri = uri;
        this.txt = txt;
    }

    public DocumentImpl(URI uri, byte[] binaryData) {
        if (binaryData == null || binaryData.length == 0) {
            throw new IllegalArgumentException();
        }else if (uri == null || uri.toString().length() == 0) {
            throw new IllegalArgumentException();
        }
        this.uri = uri;
        this.binaryData = binaryData;
    }

    @Override
    public String getDocumentTxt() {
        return this.txt;
    }

    @Override
    public byte[] getDocumentBinaryData() {
        return this.binaryData;
    }

    @Override
    public URI getKey() {
        return this.uri;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        DocumentImpl other = (DocumentImpl) o;
        return this.hashCode() == other.hashCode();
    }

    @Override
    public int hashCode() {
        int result = this.uri.hashCode();
        result = 31 * result + (this.txt != null ? this.txt.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(this.binaryData);
        return result;
    }
}
