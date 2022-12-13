package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Stack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StackImplTest {

    private Stack stack;

    @BeforeEach
    public void initStack() {
        this.stack = new StackImpl();
        this.stack.push(10);
        this.stack.push(11);
        this.stack.push(12);
    }

    @Test
    void push() {
        this.stack.push(13);
        assertEquals(13, this.stack.peek());
        assertEquals(4, this.stack.size());
        assertEquals(13, this.stack.pop());
    }

    @Test
    void pop() {
        assertEquals(12, this.stack.pop());
        assertEquals(11, this.stack.pop());
        assertEquals(10, this.stack.pop());
        assertNull(this.stack.pop());
    }

    @Test
    void peek() {
        assertEquals(12, this.stack.peek());
        this.stack.pop();
        assertEquals(11, this.stack.peek());
        this.stack.pop();
        assertEquals(10, this.stack.peek());
        this.stack.pop();
        assertEquals(null, this.stack.peek());
    }

    @Test
    void size() {
        assertEquals(3, this.stack.size());
        this.stack.pop();
        assertEquals(2, this.stack.size());
        this.stack.pop();
        assertEquals(1, this.stack.size());
        this.stack.pop();
        assertEquals(0, this.stack.size());
    }
}