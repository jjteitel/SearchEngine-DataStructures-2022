package edu.yu.cs.com1320.project.stage4.impl;

import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage4.Document;
import edu.yu.cs.com1320.project.stage4.DocumentStore;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.function.Function;

public class DocumentStoreImpl implements DocumentStore {
    HashTable<URI, Document> table;
    Stack<Undoable> commandStack;
    Trie<Document> trie;
    MinHeap<Document> heap;
    int docLimit;
    int byteLimit;
    int numOfDocs = 0;
    int totalBytes = 0;

    public DocumentStoreImpl() {
        this.table = new HashTableImpl<>();
        this.commandStack = new StackImpl<>();
        this.trie = new TrieImpl<>();
        this.heap = new MinHeapImpl<>();
    }

    @Override
    public int putDocument(InputStream input, URI uri, DocumentFormat format) throws IOException {
        if (uri == null || format == null) {
            throw new IllegalArgumentException();
        }
        int hash = 0;
        if (input == null) {
            hash = this.getDocument(uri).hashCode();
            this.deleteDocument(uri);
            return hash;
        }
        byte[] bytes = input.readAllBytes();
        if (bytes.length > byteLimit && byteLimit > 0) {
            throw new IllegalArgumentException();
        }
        Document doc;
        if (format == DocumentFormat.BINARY) {
            doc = new DocumentImpl(uri, bytes);
        }else {
            doc = new DocumentImpl(uri, new String(bytes));
            this.addToTrie(doc);
        }
        if (this.getDocument(uri) != null) {
            hash = this.getDocument(uri).hashCode();
        }
        this.updateMemory(bytes.length);
        Document d = this.table.put(uri, doc);
        this.updateHeap(doc, true, null);
        this.addCommand(uri, d);
        return hash;
    }

    private void addToTrie(Document doc) {
        Set<String> words = doc.getWords();
        for (String w : words) {
            this.trie.put(w, doc);
        }
    }

    private void updateMemory(int mem) {
        this.numOfDocs++;
        this.totalBytes += mem;
        while (this.numOfDocs > this.docLimit && this.docLimit > 0) {
            Document doc = this.heap.remove();
            this.heap.insert(doc);
            this.deleteDocument(doc.getKey());
            this.deleteFromTrie(doc);
            this.numOfDocs--;
        }
        while (this.totalBytes > this.byteLimit && this.byteLimit > 0) {
            Document doc = this.heap.remove();
            this.heap.insert(doc);
            this.deleteDocument(doc.getKey());
            this.deleteFromTrie(doc);
            if (doc.getDocumentTxt() != null) {
                this.totalBytes -= doc.getDocumentTxt().getBytes().length;
            }else {
                this.totalBytes -= doc.getDocumentBinaryData().length;
            }
        }
    }

    private void updateHeap(Document doc, boolean insert, Set<Document> docSet) {
        if (insert) {
            doc.setLastUseTime(System.nanoTime());
            this.heap.insert(doc);
            this.heap.reHeapify(doc);
        }else if (doc != null) {
            doc.setLastUseTime(Long.MIN_VALUE);
            this.heap.reHeapify(doc);
            this.heap.remove();
        }else {
            for (Document d : docSet) {
                d.setLastUseTime(Long.MIN_VALUE);
                this.heap.reHeapify(d);
                this.heap.remove();
            }
        }
    }

    @Override
    public Document getDocument(URI uri) {
        Document d = this.table.get(uri);
        if (d != null) {
            d.setLastUseTime(System.nanoTime());
            this.heap.reHeapify(d);
        }
        return d;
    }

    @Override
    public boolean deleteDocument(URI uri) {
        if (getDocument(uri) != null) {
            Document doc = this.table.put(uri, null);
            this.deleteFromTrie(doc);
            this.updateHeap(doc, false, null);
            this.addCommand(uri, doc);
            return true;
        }
        return false;
    }

    private void addCommand(URI uri, Document doc) {
        Function<URI, Boolean> undo = x -> {
            this.table.put(x, doc);
            if (doc != null && doc.getDocumentTxt() != null) {
                this.addToTrie(doc);
            }
            return true;
        };
        Undoable com = new GenericCommand<>(uri, undo);
        this.commandStack.push(com);
    }

    private void deleteFromTrie(Document doc) {
        Set<String> words = doc.getWords();
        for (String w : words) {
            this.trie.delete(w, doc);
        }
    }

    @Override
    public void undo() throws IllegalStateException {
        if (this.commandStack.size() == 0) {
            throw new IllegalStateException();
        }
        Undoable cmd = this.commandStack.pop();
        cmd.undo();
    }

    @Override
    public void undo(URI uri) throws IllegalStateException {
        if (this.commandStack.size() == 0) {
            throw new IllegalStateException();
        }
        Stack<Undoable> helper = new StackImpl<>();
        this.undo(uri, helper);
    }

    private void undo(URI uri, Stack<Undoable> helper) {
        if (this.commandStack.size() == 0) {
            throw new IllegalStateException();
        }
        Undoable cmd = this.commandStack.pop();
        try {
            if (((GenericCommand<?>) cmd).getTarget() == uri) {
                cmd.undo();
                while (helper.peek() != null) {
                    this.commandStack.push(helper.pop());
                }
                return;
            }
        }catch (ClassCastException e) {
            if (((CommandSet<URI>)cmd).containsTarget(uri)) {
                ((CommandSet)cmd).undo(uri);
                while (helper.peek() != null) {
                    this.commandStack.push(helper.pop());
                }
                return;
            }
        }
        helper.push(cmd);
        this.undo(uri, helper);
    }

    @Override
    public List<Document> search(String keyword) {
        List<Document> docs = new ArrayList<>(this.trie.getAllSorted(keyword, (o1, o2) ->
                Integer.compare(o2.wordCount(keyword), o1.wordCount(keyword))));
        for (Document d : docs) {
            d.setLastUseTime(System.nanoTime());
            this.heap.reHeapify(d);
        }
        return docs;
    }

    @Override
    public List<Document> searchByPrefix(String keywordPrefix) {
        List<Document> docs = new ArrayList<>(this.trie.getAllWithPrefixSorted(keywordPrefix, (o1, o2) ->
                Integer.compare(o2.wordCount(keywordPrefix), o1.wordCount(keywordPrefix))));
        for (Document d : docs) {
            d.setLastUseTime(System.nanoTime());
            this.heap.reHeapify(d);
        }
        return docs;
    }

    @Override
    public Set<URI> deleteAll(String keyword) {
        Set<Document> docSet = this.trie.deleteAll(keyword);
        this.updateHeap(null, false, docSet);
        Set<URI> uriSet = new HashSet<>();
        return getUris(docSet, uriSet);
    }

    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        Set<Document> docSet = this.trie.deleteAllWithPrefix(keywordPrefix);
        this.updateHeap(null, false, docSet);
        Set<URI> uriSet = new HashSet<>();
        return getUris(docSet, uriSet);
    }

    private Set<URI> getUris(Set<Document> docSet, Set<URI> uriSet) {
        CommandSet<URI> cmdSet = new CommandSet<>();
        for (Document d : docSet) {
            uriSet.add(d.getKey());
        }
        for (URI u : uriSet) {
            if (getDocument(u) != null) {
                Document doc = this.table.put(u, null);
                this.fillCmdSet(u, doc, cmdSet);
            }
        }
        this.commandStack.push(cmdSet);
        return uriSet;
    }

    private void fillCmdSet(URI u, Document doc, CommandSet<URI> cmdSet) {
        Function<URI, Boolean> undo = x -> {
            this.table.put(x, doc);
            this.addToTrie(doc);
            return true;
        };
        GenericCommand<URI> cmd = new GenericCommand<>(u, undo);
        cmdSet.addCommand(cmd);
    }

    @Override
    public void setMaxDocumentCount(int limit) {
        this.docLimit = limit;
    }

    @Override
    public void setMaxDocumentBytes(int limit) {
        this.byteLimit = limit;
    }
}
