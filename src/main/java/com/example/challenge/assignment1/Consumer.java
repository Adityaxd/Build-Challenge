package com.example.challenge.assignment1;

import java.util.List;

/*
 Consumer class that takes integers from a BoundedBlockingQueue
 and adds them to a destination list.
*/
public class Consumer implements Runnable {

    private final BoundedBlockingQueue<Integer> queue;
    private final List<Integer> destination;

    public Consumer(BoundedBlockingQueue<Integer> queue, List<Integer> destination) {
        this.queue = queue;
        this.destination = destination;
    }

    @Override
    public void run() {
        try {
            while (true) {
                // Take an integer from the queue, blocks if queue is empty
                Integer value = queue.take();

                // null is the sentinel that indicates end of production
                if (value == null) {
                    break;
                }

                destination.add(value);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Consumer was interrupted : " + e.getMessage());
        }
    }
}
