package com.example.challenge.assignment1;

import java.util.List;

/*
    Producer class that reads integers from a source list and puts them into a bounded blocking queue.
    It blocks when the queue is full and waits until space is available.
*/

public class Producer implements Runnable {

    private final List<Integer> source;
    private final BoundedBlockingQueue<Integer> queue;

    public Producer(List<Integer> source, BoundedBlockingQueue<Integer> queue) {
        this.source = source;
        this.queue = queue;
    }

    @Override
    public void run() {
        try {

            for (Integer value : source) {
                queue.put(value); // Blocks if the queue is full
            }
            queue.put(null); // Indicate end of production

        } catch (InterruptedException e) {
            // Restore interrupted status and exit
            Thread.currentThread().interrupt();
            System.err.println("Producer interrupted : " + e.getMessage());
        }
    }

}
