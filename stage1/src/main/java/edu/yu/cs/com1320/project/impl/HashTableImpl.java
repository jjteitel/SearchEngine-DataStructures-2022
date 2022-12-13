package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;

public class HashTableImpl<Key, Value> implements HashTable<Key, Value> {
    class Entry<Key, Value> {
        Key key;
        Value value;
        Entry next;
        Entry(Key k, Value v) {
            if (k == null) {
                throw new IllegalArgumentException();
            }
            key = k;
            value = v;
            next = null;
        }
    }

    private Entry<?,?>[] table;
    public HashTableImpl() {
        this.table = new Entry[5];
    }

    private int hashFunction(Key key) {
        return (key.hashCode() & 0x7fffffff) % this.table.length;
    }

    @Override
    public Value get(Key k) {
        int index = this.hashFunction(k);
        Entry current = this.table[index];
        if (current == null) {
            return null;
        }
        if (current.key == k) {
            return (Value)current.value;
        }
        while (current.next != null && current.next.key != k) {
            current = current.next;
        }
        if (current.next != null) {
            return (Value)current.next.value;
        }
        return null;
    }

    @Override
    public Value put(Key k, Value v) {
        int index = this.hashFunction(k);
        Entry current = this.table[index];
        if (v == null) {
            return delete(current, k, v, index);
        }
        if (current == null) {
            this.table[index] = new Entry<Key, Value>(k, v);
            return null;
        }
        if (current.key == k) {
            Value old = (Value)current.value;
            current.value = v;
            return old;
        }
        while (current.next != null && current.next.key != k) {
            current = current.next;
        }
        if (current.next != null) {
            Value old = (Value)current.next.value;
            current.next.value = v;
            return old;
        }
        current.next = new Entry<Key, Value>(k, v);
        return null;
    }

    private Value delete(Entry current, Key k, Value v, int index) {
        if (current.key == k) {
            Value old = (Value)current.value;
            this.table[index] = current.next;
            return old;
        }
        while (current.next != null && current.next.key != k) {
            current = current.next;
        }
        if (current.next != null) {
            Value old = (Value)current.next.value;
            current.next = current.next.next;
            return old;
        }
        return null;
    }
}
