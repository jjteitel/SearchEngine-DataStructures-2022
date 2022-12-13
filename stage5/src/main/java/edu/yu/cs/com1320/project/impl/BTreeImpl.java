package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import java.util.Arrays;

public class BTreeImpl<Key extends Comparable<Key>, Value> implements BTree {
    private static final int MAX = 4;
    private Node root;
    private Node leftMostExternalNode;
    private int height;
    private int n;
    private PersistenceManager pm;

    private static final class Node {
        private int entryCount;
        private Entry[] entries = new Entry[BTreeImpl.MAX];
        private Node next;
        private Node prev;

        private Node(int k) {
            this.entryCount = k;
        }

        private void setNext(Node next) {
            this.next = next;
        }

        private Node getNext() {
            return this.next;
        }

        private void setPrev(Node prev) {
            this.prev = prev;
        }

        private Entry[] getEntries() {
            return Arrays.copyOf(this.entries, this.entryCount);
        }
    }

    private static class Entry {
        private Comparable key;
        private Object val;
        private Node child;

        private Entry(Comparable key, Object val, Node child) {
            this.key = key;
            this.val = val;
            this.child = child;
        }

        private Object getValue() {
            return this.val;
        }

        private Comparable getKey() {
            return this.key;
        }
    }

    public BTreeImpl() {
        this.root = new Node(0);
        this.leftMostExternalNode = this.root;
        this.pm = null;
    }


    @Override
    public Value get(Comparable k) {
        if (k == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        Entry entry = this.get(this.root, k, this.height);
        if (entry != null) {
            return (Value)entry.val;
        }
        return  null;
    }

    private Entry get(Node node, Comparable key, int h) {
        if (h == 0) {
            for (int i = 0; i < node.entryCount; i++) {
                if (this.isEqual(key, node.entries[i].key)) {
                    return node.entries[i];
                }
            }
            return null;
        }
        for (int i = 0; i < node.entryCount; i++) {
            if (i + 1 == node.entryCount || this.less(key, node.entries[i + 1].key)) {
                return get(node.entries[i].child, key, h - 1);
            }
        }
        return null;
    }

    @Override
    public Value put(Comparable k, Object v) {
        if (k == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        Entry entry = this.get(this.root, k, this.height);
        assert this.root.entryCount <= MAX : "root.entryCount <= MAX";
        Node newNode = this.put(this.root, k, v, this.height);
        this.n++;
        if (entry == null) {
            return null;
        }
        Node newRoot = new Node(2);
        newRoot.entries[0] = new Entry(this.root.entries[0].key, null, this.root);
        newRoot.entries[1] = new Entry(newNode.entries[0].key, null, newNode);
        this.root = newRoot;
        this.height++;
        if (entry != null) {
            return (Value)entry.val;
        }
        return null;
    }

    private Node put(Node node, Comparable k, Object v, int h) {
        int i;
        Entry newEntry = new Entry(k, v, null);
        if (h == 0) {
            for (i = 0; i < node.entryCount; i++) {
                if (this.less(k, node.entries[i].key)) {
                    break;
                }
            }
        } else {
            for (i = 0; i < node.entryCount; i++) {
                if (i + 1 == node.entryCount || this.less(k, node.entries[i + 1].key)) {
                    Node newNode = this.put(node.entries[i++].child, k, v, h - 1);
                    if (newNode == null) {
                        return null;
                    }
                    newEntry.key = newNode.entries[0].key;
                    newEntry.val = null;
                    newEntry.child = newNode;
                    break;
                }
            }
        }
        assert i <= node.entryCount : "i <= node.entryCount";
        assert node.entryCount < BTreeImpl.MAX : "node.entryCount < BTreeImpl.MAX";
        for (int j = node.entryCount; j > i; j--) {
            node.entries[j] = node.entries[j - 1];
        }
        node.entries[i] = newEntry;
        node.entryCount++;
        if (node.entryCount < BTreeImpl.MAX) {
            return null;
        }else {
            return this.split(node, h);
        }
    }

    private Node split(Node node, int h) {
        Node newNode = new Node(BTreeImpl.MAX / 2);
        newNode.entryCount = BTreeImpl.MAX / 2;
        for (int i = 0; i < BTreeImpl.MAX / 2; i++) {
            newNode.entries[i] = node.entries[BTreeImpl.MAX / 2 + i];
        }
        if (h == 0) {
            newNode.setNext(node.getNext());
            newNode.setPrev(node);
            node.setNext(newNode);
        }
        return newNode;
    }

    private boolean less(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) < 0;
    }

    private boolean isEqual(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) == 0;
    }

    @Override
    public void moveToDisk(Comparable k) throws Exception {
        if (pm == null) {
            throw new IllegalStateException("PersistenceManager is not set");
        }
        Value v = (Value) this.get(k);
        this.pm.serialize(k, v);
        this.put(k, k);
    }

    @Override
    public void setPersistenceManager(PersistenceManager pm) {
        this.pm = pm;
    }
}
