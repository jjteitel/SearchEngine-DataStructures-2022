package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;

import java.net.URI;
import java.util.*;

public class DocumentImpl implements Document {
    private final URI uri;
    private String txt;
    private byte[] binaryData;
    private Map<String, Integer> wordCountMap;
    private final Set<String> wordSet = new HashSet<>();
    private long lastUseTime;

    public DocumentImpl(URI uri, String txt, Map<String, Integer> wordCountMap) {
        if (txt == null || txt.length() == 0) {
            throw new IllegalArgumentException();
        }else if (uri == null || uri.toString().length() == 0) {
            throw new IllegalArgumentException();
        }
        this.uri = uri;
        this.txt = txt;
        if (wordCountMap == null) {
            this.wordCountMap = new HashMap<>();
            String text = txt.replaceAll("[^a-zA-Z\\d\\s]", "");
            String[] words = text.split("\\s+");
            for (String w : words) {
                int counter = 0;
                if (!this.wordSet.contains(w)) {
                    for (String otherW : words) {
                        if (w.equalsIgnoreCase(otherW)) {
                            counter++;
                        }
                    }
                    this.wordCountMap.put(w.toLowerCase(), counter);
                }
                this.wordSet.add(w.toLowerCase());
            }
        }else {
            this.wordCountMap = wordCountMap;
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
        byte[] bytes = new byte[binaryData.length];
        System.arraycopy(binaryData, 0, bytes, 0, binaryData.length);
        return bytes;
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
        String w = word.replaceAll("[^a-zA-Z\\d\\s]", "").toLowerCase();
        for (String wrd : this.wordSet) {
            if (wrd.contains(w)) {
                counter += this.wordCountMap.get(wrd);
            }
        }
        return counter;
    }

    @Override
    public Set<String> getWords() {
        return new HashSet<>(this.wordSet);
    }

    @Override
    public long getLastUseTime() {
        return this.lastUseTime;
    }

    @Override
    public void setLastUseTime(long timeInNanoseconds) {
        this.lastUseTime = timeInNanoseconds;
    }

    @Override
    public Map<String, Integer> getWordMap() {
        return new HashMap<>(this.wordCountMap);
    }

    @Override
    public void setWordMap(Map<String, Integer> wordMap) {
        this.wordCountMap = wordMap;
    }

    @Override
    public int compareTo(Document o) {
        if (o == null) {
            return 1;
        }
        return Long.compare(this.getLastUseTime(), o.getLastUseTime());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (o.getClass() != this.getClass()) {
            return false;
        }
        Document other = (Document)o;
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
