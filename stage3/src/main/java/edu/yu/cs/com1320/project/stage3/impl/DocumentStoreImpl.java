package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage3.Document;
import edu.yu.cs.com1320.project.stage3.DocumentStore;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.function.Function;

public class DocumentStoreImpl implements DocumentStore {
    HashTable<URI, Document> table;
    Stack<Undoable> commandStack;
    Trie<Document> trie;

    public DocumentStoreImpl() {
        this.table = new HashTableImpl<>();
        this.commandStack = new StackImpl<>();
        this.trie = new TrieImpl<>();
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
            return hash;
        }
        byte[] bytes;
        bytes = input.readAllBytes();
        Document doc;
        if (format == DocumentFormat.BINARY) {
            doc = new DocumentImpl(uri, bytes);
        }else {
            String text = new String(bytes);
            doc = new DocumentImpl(uri, text);
            addToTrie(doc);
        }
        if (getDocument(uri) != null) {
            hash = getDocument(uri).hashCode();
        }
        Document d = this.table.put(uri, doc);
        addCommand(uri, d);
        return hash;
    }

    private void addToTrie(Document doc) {
        Set<String> words = doc.getWords();
        for (String w : words) {
            this.trie.put(w, doc);
        }
    }

    @Override
    public Document getDocument(URI uri) {
        return this.table.get(uri);
    }

    @Override
    public boolean deleteDocument(URI uri) {
        if (getDocument(uri) != null) {
            Document doc = this.table.put(uri, null);
            deleteFromTrie(doc);
            addCommand(uri, doc);
            return true;
        }
        return false;
    }

    private void addCommand(URI uri, Document doc) {
        Function<URI, Boolean> undo = x -> {
            this.table.put(x, doc);
            if (doc != null && doc.getDocumentTxt() != null) {
                addToTrie(doc);
            }
            return true;
        };
        Undoable com = new GenericCommand<URI>(uri, undo);
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
        undo(uri, helper);
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
        return this.trie.getAllSorted(keyword, (o1, o2) -> {
            if (o1.wordCount(keyword) == o2.wordCount(keyword)) {
                return 0;
            }else if (o1.wordCount(keyword) < o2.wordCount(keyword)) {
                return 1;
            }else {
                return -1;
            }
        });
    }

    @Override
    public List<Document> searchByPrefix(String keywordPrefix) {
        return this.trie.getAllWithPrefixSorted(keywordPrefix, (o1, o2) -> {
            if (o1.wordCount(keywordPrefix) == o2.wordCount(keywordPrefix)) {
                return 0;
            }else if (o1.wordCount(keywordPrefix) < o2.wordCount(keywordPrefix)) {
                return 1;
            }else {
                return -1;
            }
        });
    }

    @Override
    public Set<URI> deleteAll(String keyword) {
        Set<Document> docSet = this.trie.deleteAll(keyword);
        Set<URI> uriSet = new HashSet<>();
        CommandSet<URI> cmdSet = new CommandSet<URI>();
        for (Document d : docSet) {
            uriSet.add(d.getKey());
        }
        for (URI u : uriSet) {
            if (getDocument(u) != null) {
                Document doc = this.table.put(u, null);
                fillCmdSet(u, doc, cmdSet);
            }
        }
        this.commandStack.push(cmdSet);
        return uriSet;
    }

    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        Set<Document> docSet = this.trie.deleteAllWithPrefix(keywordPrefix);
        Set<URI> uriSet = new HashSet<>();
        CommandSet<URI> cmdSet = new CommandSet<URI>();
        for (Document d : docSet) {
            uriSet.add(d.getKey());
        }
        for (URI u : uriSet) {
            if (getDocument(u) != null) {
                Document doc = this.table.put(u, null);
                fillCmdSet(u, doc, cmdSet);
            }
        }
        this.commandStack.push(cmdSet);
        return uriSet;
    }

    private void fillCmdSet(URI u, Document doc, CommandSet<URI> cmdSet) {
        Function<URI, Boolean> undo = x -> {
            this.table.put(x, doc);
            addToTrie(doc);
            return true;
        };
        GenericCommand<URI> cmd = new GenericCommand<>(u, undo);
        cmdSet.addCommand(cmd);
    }
}
