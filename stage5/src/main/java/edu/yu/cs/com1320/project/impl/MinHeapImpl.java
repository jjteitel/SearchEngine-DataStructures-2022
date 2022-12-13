package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E> {
    public MinHeapImpl() {
        this.elements = (E[]) new Comparable[10];
    }

    @Override
    public void reHeapify(E element) {
        int idx = this.getArrayIndex(element);
        if (idx >= 0) {
            this.upHeap(idx);
            this.downHeap(idx);
        }
    }

    @Override
    protected int getArrayIndex(E element) {
        int idx = -1;
        for (int i = 0; i < this.elements.length; i++) {
            if (this.elements[i] == element) {
                idx = i;
                break;
            }
        }
        return idx;
    }

    @Override
    protected void doubleArraySize() {
        E[] old = this.elements;
        this.elements = (E[]) new Comparable[2 * old.length];
        System.arraycopy(old, 0, this.elements, 0, old.length);
    }
}
