package edu.yu.cs.com1320.project.stage2.impl;

import edu.yu.cs.com1320.project.Command;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.stage2.Document;
import edu.yu.cs.com1320.project.stage2.DocumentStore;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.function.Function;

public class DocumentStoreImpl implements DocumentStore {
    HashTableImpl table;
    StackImpl commandStack;

    public DocumentStoreImpl() {
        this.table = new HashTableImpl<>();
        this.commandStack = new StackImpl<>();
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
            Document d = (Document)this.table.put(uri, doc);
            addCommand(uri, d);
        }
        if (format == DocumentFormat.TXT) {
            String text = new String(bytes);
            Document doc = new DocumentImpl(uri, text);
            if (getDocument(uri) != null) {
                hash = getDocument(uri).hashCode();
            }
            Document d = (Document)this.table.put(uri, doc);
            addCommand(uri, d);
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
            Document doc = (Document)this.table.put(uri, null);
            addCommand(uri, doc);
            return true;
        }
        return false;
    }

    @Override
    public void undo() throws IllegalStateException {
        if (this.commandStack.size() == 0) {
            throw new IllegalStateException();
        }
        Command com = (Command) this.commandStack.pop();
        com.undo();
    }

    @Override
    public void undo(URI uri) throws IllegalStateException {
        if (this.commandStack.size() == 0) {
            throw new IllegalStateException();
        }
        Stack helper = new StackImpl();
        Command com = (Command) commandStack.pop();
        while (com.getUri() != uri && this.commandStack.size() != 0) {
            helper.push(com);
            com = (Command) commandStack.pop();
        }
        if (com.getUri() != uri) {
            throw new IllegalStateException();
        }
        com.undo();
        while (helper.peek() != null) {
            this.commandStack.push(helper.pop());
        }
    }

    private void addCommand(URI uri, Document doc) {
        Function<URI, Boolean> undo = x -> {
                this.table.put(x, doc);
                return true;
            };
        Command com = new Command(uri, undo);
        this.commandStack.push(com);
    }
}
