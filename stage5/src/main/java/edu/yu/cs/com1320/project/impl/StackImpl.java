package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Stack;

public class StackImpl<T> implements Stack<T> {
    class Node<T> {
        T element;
        Node next;
        Node(T e) {
            if (e == null) {
                throw new IllegalArgumentException();
            }
            element = e;
            next = null;
        }
    }

    private Node root;
    private int size = 0;

    public StackImpl() {}

    @Override
    public void push(T element) {
        Node n = new Node(element);
        if (this.root != null) {
            n.next = this.root;
        }
        this.root = n;
        this.size++;
    }

    @Override
    public T pop() {
        if (this.root == null) {
            return null;
        }
        T value = (T)this.root.element;
        this.root = this.root.next;
        this.size--;
        return value;
    }

    @Override
    public T peek() {
        if (this.root == null) {
            return null;
        }
        return (T)this.root.element;
    }

    @Override
    public int size() {
        return this.size;
    }
}
