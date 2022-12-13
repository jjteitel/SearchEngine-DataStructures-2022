package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.stage5.impl.DocumentStoreImpl;

import java.util.*;

public class TrieImpl<Value> implements Trie<Value> {
    private final int alphaNumSize = 36;
    private Node<Value> root;

    BTree bTree;

    class Node<Value> {
        protected Set<Value> val = new HashSet<>();
        protected Node[] links = new Node[alphaNumSize];
    }

    public TrieImpl() {}

    @Override
    public void put(String key, Value val) {
        if (key == null || val == null) {
            throw new IllegalArgumentException();
        }
        this.root = put(this.root, key, val, 0);
    }

    private Node<Value> put(Node x, String key, Value val, int d) {
        if (x == null) {
            x = new Node();
        }
        if (d == key.length()) {
            x.val.add(val);
            return x;
        }
        char c = this.getIndex(key, d);
        x.links[c] = this.put(x.links[c], key, val, d + 1);
        return x;
    }

    @Override
    public List<Value> getAllSorted(String key, Comparator<Value> comparator) {
        if (key == null || comparator == null) {
            throw new IllegalArgumentException();
        }
        List<Value> x = this.getSorted(this.root, key, 0);
        x.sort(comparator);
        return x;
    }

    private List<Value> getSorted(Node x, String key, int d) {
        if (x == null) {
            return new ArrayList<>();
        }
        if (d == key.length()) {
            return new ArrayList<Value>(x.val);
        }
        char c = this.getIndex(key, d);
        if (x.links[c] == null) {
            return new ArrayList<>();
        }
        return this.getSorted(x.links[c], key, d + 1);
    }

    @Override
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator) {
        if (prefix == null || comparator == null) {
            throw new IllegalArgumentException();
        }
        List<Value> x = new ArrayList<>();
        x = this.getPrefix(this.root, prefix, 0, x);
        x.sort(comparator);
        return x;
    }

    private List<Value> getPrefix(Node x, String prefix, int d, List<Value> list) {
        if (x == null) {
            return list;
        }
        if (d >= prefix.length()) {
            if (x.val != null) {
                list.addAll(x.val);
            }
            if (x.links != null) {
                for (int i = 0; i < x.links.length; i++) {
                    this.getPrefix(x.links[i], prefix, d + 1, list);
                }
            }
            return list;
        }
        char c = this.getIndex(prefix, d);
        return this.getPrefix(x.links[c], prefix, d + 1, list);
    }

    @Override
    public Set<Value> deleteAllWithPrefix(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException();
        }
        return deletePrefix(this.root, prefix, 0);
    }

    private Set<Value> deletePrefix(Node x, String prefix, int d) {
        if (x == null) {
            return new HashSet<>();
        }
        Set<Value> temp = new HashSet<>();
        if (d == prefix.length()) {
            temp.addAll(this.getPrefix(x, prefix, d, new ArrayList<>()));
            for (int i = 0; i < x.links.length; i++) {
                x.links[i] = null;
            }
            x.val = null;
            return temp;
        }
        char c = this.getIndex(prefix, d);
        return this.deletePrefix(x.links[c], prefix, d + 1);
    }

    @Override
    public Set<Value> deleteAll(String key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        return this.deleteAll(this.root, key, 0);
    }

    private Set<Value> deleteAll(Node x, String key, int d) {
        if (x == null) {
            return new HashSet<>();
        }
        if (d == key.length()) {
            Set<Value> temp = new HashSet<>(x.val);
            x.val.removeAll(temp);
            return temp;
        }
        char c = this.getIndex(key, d);
        return this.deleteAll(x.links[c], key, d + 1);
    }

    @Override
    public Value delete(String key, Value val) {
        if (key == null || val == null) {
            throw new IllegalArgumentException();
        }
        return this.delete(this.root, key, val, 0);
    }

    private Value delete(Node x, String key, Value val, int d) {
        if (d == key.length() && x.val.contains(val)) {
            x.val.remove(val);
            return val;
        }
        if (d == key.length() && !x.val.contains(val)) {
            return null;
        }
        char c = this.getIndex(key, d);
        return (Value)this.delete(x.links[c], key, val, d + 1);
    }

    private char getIndex(String key, int d) {
        char c = key.charAt(d);
        if (c >= 48 && c <= 57) {
            c -= 48;
        }
        if (c >= 65 && c <= 90) {
            c -= 55;
        }
        if (c >= 97 && c <= 122) {
            c -= 87;
        }
        return c;
    }
}
