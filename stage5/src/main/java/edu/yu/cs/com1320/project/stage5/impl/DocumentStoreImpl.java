package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.impl.BTreeImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import javax.print.Doc;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class DocumentStoreImpl implements DocumentStore {
    Stack<Undoable> undoStack;
    Trie<URI> trie;
    MinHeap<Document> minHeap;
    BTree<URI, Document> bTree;
    PersistenceManager<URI, Document> pm;
    int docLimit;
    int byteLimit;
    int docCount;
    int byteCount;

    public DocumentStoreImpl() {
        this.undoStack = new StackImpl<>();
        this.trie = new TrieImpl<>();
        this.minHeap = new MinHeapImpl<>();
        this.bTree = new BTreeImpl<>();
        this.pm = new DocumentPersistenceManager(null);
    }

    public DocumentStoreImpl(File basedir) {
        this.undoStack = new StackImpl<>();
        this.trie = new TrieImpl<>();
        this.minHeap = new MinHeapImpl<>();
        this.bTree = new BTreeImpl<>();
        this.pm = new DocumentPersistenceManager(basedir);
    }

    @Override
    public int putDocument(InputStream input, URI uri, DocumentFormat format) throws IOException {
        if (uri == null || format == null) {
            throw new IllegalArgumentException("URI and format cannot be null");
        }
        int hash = 0;
        if (input == null) {
            hash = this.getDocument(uri).hashCode();
            this.deleteDocument(uri);
            return hash;
        }
        byte[] bytes = input.readAllBytes();
        if (bytes.length > this.byteLimit && byteLimit > 0) {
            throw new IllegalArgumentException("Document is too large");
        }
        Document doc;
        if (format == DocumentFormat.BINARY) {
            doc = new DocumentImpl(uri, bytes);
        }else {
            doc = new DocumentImpl(uri, new String(bytes), null);
            this.addToTrie(doc);
        }
        if (this.getDocument(uri) != null) {
            hash = this.getDocument(uri).hashCode();
        }
        this.updateMemory(bytes.length);
        Document d = this.addToBtree(doc);
        this.updateHeap(doc, true, null);
        this.addCommand(uri, d);
        return hash;
    }

    private void addToTrie(Document doc) {
        Set<String> words = doc.getWords();
        for (String word : words) {
            this.trie.put(word, doc.getKey());
        }
    }

    private void updateMemory(int mem) {
        this.docCount++;
        this.byteCount += mem;
        while (this.docCount > this.docLimit && this.docLimit > 0) {
            Document doc = this.minHeap.remove();
            this.bTree.setPersistenceManager(this.pm);
            try {
                this.bTree.moveToDisk(doc.getKey());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            this.deleteFromTrie(doc.getKey());
            this.docCount--;
        }
        while (this.byteCount > this.byteLimit && this.byteLimit > 0) {
            Document doc = this.minHeap.remove();
            try {
                this.bTree.setPersistenceManager(this.pm);
                this.bTree.moveToDisk(doc.getKey());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            this.deleteFromTrie(doc.getKey());
            if (doc.getDocumentTxt() != null) {
                this.byteCount -= doc.getDocumentTxt().getBytes().length;
            }else {
                this.byteCount -= doc.getDocumentBinaryData().length;
            }
        }
    }

    private void updateHeap(Document doc, boolean insert, Set<Document> docSet) {
        if (insert) {
            doc.setLastUseTime(System.nanoTime());
            this.minHeap.insert(doc);
            this.minHeap.reHeapify(doc);
        }else if (doc != null) {
            doc.setLastUseTime(Long.MIN_VALUE);
            this.minHeap.reHeapify(doc);
            this.minHeap.remove();
        }else {
            for (Document d : docSet) {
                d.setLastUseTime(Long.MIN_VALUE);
                this.minHeap.reHeapify(d);
                this.minHeap.remove();
            }
        }
    }

    private Document addToBtree(Document doc) {
        Object entry = this.bTree.put(doc.getKey(), doc);
        if (entry == null) {
            return null;
        }
        assert entry != null : "Entry is null";
        if (entry instanceof Document) {
            return (Document) entry;
        }else {
            try {
                Document d = this.pm.deserialize((URI) entry);
                d.setLastUseTime(doc.getLastUseTime());
                this.minHeap.insert(d);
                this.minHeap.reHeapify(d);
                this.pm.delete(d.getKey());
                return d;
            }catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private Document getFromBtree(URI uri) {
        Object entry = this.bTree.get(uri);
        if (entry == null) {
            return null;
        }
        assert entry != null : "Document not found in btree";
        if (entry instanceof DocumentImpl) {
            return (Document) entry;
        }else {
            try {
                Document d = this.pm.deserialize((URI) entry);
                d.setLastUseTime(System.nanoTime());
                this.minHeap.insert(d);
                this.minHeap.reHeapify(d);
                this.pm.delete(d.getKey());
                return d;
            }catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private void deleteFromTrie(URI uri) {
        Document doc = this.getFromBtree(uri);
        Set<String> words = doc.getWords();
        for (String word : words) {
            this.trie.delete(word, uri);
        }
    }

    @Override
    public Document getDocument(URI uri) {
        Document doc = this.getFromBtree(uri);
        if (doc != null) {
            doc.setLastUseTime(System.nanoTime());
            this.minHeap.reHeapify(doc);
        }
        return doc;
    }

    @Override
    public boolean deleteDocument(URI uri) {
        if (this.getDocument(uri) != null) {
            Document doc = this.getFromBtree(uri);
            this.bTree.put(uri, null);
            this.deleteFromTrie(uri);
            this.updateHeap(doc, false, null);
            this.addCommand(uri, doc);
            return true;
        }
        return false;
    }

    private void addCommand(URI uri, Document doc) {
        Function<URI, Boolean> undo = x -> {
            this.bTree.put(x, doc);
            if (doc != null && doc.getDocumentTxt() != null) {
                this.addToTrie(doc);
            }
            return true;
        };
        Undoable com = new GenericCommand<>(uri, undo);
        this.undoStack.push(com);
    }

    @Override
    public void undo() throws IllegalStateException {
        if (this.undoStack.size() == 0) {
            throw new IllegalStateException("No commands to undo");
        }
        Undoable com = this.undoStack.pop();
        com.undo();
    }

    @Override
    public void undo(URI uri) throws IllegalStateException {
        if (this.undoStack.size() == 0) {
            throw new IllegalStateException("No commands to undo");
        }
        Stack<Undoable> helper = new StackImpl<>();
        this.undo(uri, helper);
    }

    private void undo(URI uri, Stack<Undoable> helper) {
        if (this.undoStack.size() == 0) {
            throw new IllegalStateException("No commands to undo");
        }
        Undoable com = this.undoStack.pop();
        try {
            if (((GenericCommand) com).getTarget() == uri) {
                com.undo();
                while (helper.peek() != null) {
                    this.undoStack.push(helper.pop());
                }
                return;
            }
        }catch (ClassCastException e) {
            if (((CommandSet<URI>)com).containsTarget(uri)) {
                ((CommandSet<URI>)com).undo(uri);
                while (helper.peek() != null) {
                    this.undoStack.push(helper.pop());
                }
                return;
            }
            helper.push(com);
            this.undo(uri, helper);
        }
    }

    @Override
    public List<Document> search(String keyword) {
        List<URI> uris = new ArrayList<>(this.trie.getAllSorted(keyword, (o1, o2) ->
                Integer.compare(this.bTree.get(o2).wordCount(keyword), this.bTree.get(o2).wordCount(keyword))));
        List<Document> docs = new ArrayList<>();
        for (URI uri : uris) {
            docs.add(this.getDocument(uri));
        }
        for (Document doc : docs) {
            doc.setLastUseTime(System.nanoTime());
            this.minHeap.reHeapify(doc);
        }
        return docs;
    }

    @Override
    public List<Document> searchByPrefix(String keywordPrefix) {
        List<URI> uris = new ArrayList<>(this.trie.getAllWithPrefixSorted(keywordPrefix, (o1, o2) ->
                Integer.compare(this.bTree.get(o2).wordCount(keywordPrefix), this.bTree.get(o2).wordCount(keywordPrefix))));
        List<Document> docs = new ArrayList<>();
        for (URI uri : uris) {
            docs.add(this.getDocument(uri));
        }
        for (Document doc : docs) {
            doc.setLastUseTime(System.nanoTime());
            this.minHeap.reHeapify(doc);
        }
        return docs;
    }

    @Override
    public Set<URI> deleteAll(String keyword) {
        Set<URI> uris = this.trie.deleteAll(keyword);
        Set<Document> docSet = new HashSet<>();
        for (URI uri : uris) {
            docSet.add(this.getDocument(uri));
        }
        this.updateHeap(null, false, docSet);
        Set<URI> uriSet = new HashSet<>();
        return getUris(docSet, uriSet);
    }

    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        Set<URI> uris = this.trie.deleteAllWithPrefix(keywordPrefix);
        Set<Document> docSet = new HashSet<>();
        for (URI uri : uris) {
            docSet.add(this.getDocument(uri));
        }
        this.updateHeap(null, false, docSet);
        Set<URI> uriSet = new HashSet<>();
        return getUris(docSet, uriSet);
    }

    private Set<URI> getUris(Set<Document> docSet, Set<URI> uriSet) {
        CommandSet<URI> comSet = new CommandSet<>();
        for (Document doc : docSet) {
            uriSet.add(doc.getKey());
        }
        for (URI uri : uriSet) {
            if (getDocument(uri) != null) {
                Object doc = this.bTree.put(uri, null);
                if (doc instanceof Document) {
                    this.fillComSet(uri, (Document) doc, comSet);
                }else {
                    try {
                        Document d = this.pm.deserialize((URI) doc);
                        d.setLastUseTime(System.nanoTime());
                        this.minHeap.insert(d);
                        this.minHeap.reHeapify(d);
                        this.pm.delete(uri);
                        this.fillComSet(uri, d, comSet);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        this.undoStack.push(comSet);
        return uriSet;
    }

    private void fillComSet(URI u, Document doc, CommandSet<URI> comSet) {
        Function<URI, Boolean> undo = x -> {
            this.bTree.put(x, doc);
            this.addToTrie(doc);
            return true;
        };
        GenericCommand<URI> com = new GenericCommand<>(u, undo);
        comSet.addCommand(com);
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
