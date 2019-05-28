package dk.dbc.service.performance.replayer;
/*
 * Copyright (C) 2019 DBC A/S (http://dbc.dk/)
 *
 * This is part of performance-test
 *
 * performance-test is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * performance-test is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * File created: 26/03/2019
 */

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Implements a stack with at fixed number of elements
 *
 * @param <T> Type of values stored in the stack
 */
public class FixedSizeStack<T> {
    private int maxSize;
    protected Deque<T> stack;

    /**
     * A stack with a fixed number of elements of type T
     * @param size The number of elements in the stack
     */
    public FixedSizeStack(int size) {
        this.maxSize = size;
        this.stack = new ArrayDeque<T>(size);
    }

    /**
     * Add one element to the stack, potentially removing the
     * bottom element.
     *
     * @param object Element to be added
     * @return The added object
     */
    public T push(T object) {
        //If the stack is too big, remove elements until it's the right size.
        while (stack.size() >= maxSize) {
            stack.removeLast();
        }
        stack.push(object);
        return object;
    }

    /** Remove and return the top of the stack
     *
     * @return top element
     */
    protected T pop() {
        return stack.pop();
    }

    /**
     * Get the number of elements currently in the stack
     * @return number of elements in the stack
     */
    protected int size() {
        return stack.size();
    }

    /**
     * Return the top element, without removing it.
     *
     * @return Top element
     */
    protected T peekTop() {
        return stack.peekFirst();
    }
}
