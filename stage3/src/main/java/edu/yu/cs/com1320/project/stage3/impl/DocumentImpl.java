package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.stage3.Document;

import java.net.URI;
import java.util.*;

public class DocumentImpl implements Document {
    private final URI uri;
    private String txt;
    private byte[] binaryData;
    private Map<String, Integer> numWords = new HashMap<>();
    private Set<String> wordSet = new HashSet<>();

    public DocumentImpl(URI uri, String txt) {
        if (txt == null || txt.length() == 0) {
            throw new IllegalArgumentException();
        }else if (uri == null || uri.toString().length() == 0) {
            throw new IllegalArgumentException();
        }
        this.uri = uri;
        this.txt = txt;
        String text = txt.replaceAll("[^a-zA-Z0-9\\s]", "");
        String[] words = text.split("\\s+");
        for (String w : words) {
            int counter = 0;
            if (!this.wordSet.contains(w)) {
                for (String otherW : words) {
                    if (w.equalsIgnoreCase(otherW)) {
                        counter++;
                    }
                }
                this.numWords.put(w.toLowerCase(), counter);
            }
            this.wordSet.add(w.toLowerCase());
        }
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
    public int wordCount(String word) {
        if (word == null) {
            throw new IllegalArgumentException();
        }
        if (this.wordSet.isEmpty()) {
            return 0;
        }
        int counter = 0;
        String w = word.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase();
        for (String wrd : this.wordSet) {
            if (wrd.contains(w)) {
                counter += this.numWords.get(wrd);
            }
        }
        return counter;
    }

    @Override
    public Set<String> getWords() {
        return this.wordSet;
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
