package com.example.challenge.assignment1;

import java.util.LinkedList;
import java.util.Queue;


/*
    Bounded blocking queue implemented via LinkedList
    Methods : 
    1. put() - blocks inserts when queue is full and waits
    2. take() - blocks extraction when queue is empty

    Helper Methods :
    1. size() - returns the size of the queue
    2. getCapacity() - Maximum Number of elements the queue can hold

*/

public class BoundedBlockingQueue<T> {
    
    private final Queue<T> queue = new LinkedList<>();
    private final int capacity;


    public BoundedBlockingQueue(int capacity) {
        if(capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive or greater than 0, but was : " + capacity);
        }
        this.capacity = capacity;
    }

    // Adds an element to the queue, blocks additions if queue is full
    public void put(T element) throws InterruptedException {
        synchronized (queue) {
            while(queue.size() == capacity) {
                queue.wait();
            }
            queue.add(element);
            queue.notifyAll(); // notifies any waiting threads 
        }
    }

    //Removes and returns the head/front of the queue, blocks removals if queue is empty
    public T take() throws InterruptedException {
        synchronized (queue) {
            while(queue.isEmpty()) {
                queue.wait();
            }
            T value = queue.remove();
            queue.notifyAll(); // notifies any waiting threads 
            return value;
        }
    }

    public int size() {
        synchronized(queue) {
            return queue.size();
        }
    }

    public int getCapacity() {
        return capacity;
    }
}
