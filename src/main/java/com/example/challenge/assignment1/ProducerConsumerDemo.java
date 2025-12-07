package com.example.challenge.assignment1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 ProducerConsumerDemo class that demonstrates the producer-consumer pattern
 using a bounded blocking queue.
 This is the entry point of the application.
*/

public class ProducerConsumerDemo {

    public static void main(String[] args) throws InterruptedException {
        // Source data
        List<Integer> source = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);

        // Destination where the consumer will store items
        List<Integer> destination = new ArrayList<>();

        // Shared bounded queue with small capacity to force blocking
        BoundedBlockingQueue<Integer> queue = new BoundedBlockingQueue<>(3);

        Thread producerThread = new Thread(new Producer(source, queue), "Producer-Thread");
        Thread consumerThread = new Thread(new Consumer(queue, destination), "Consumer-Thread");

        producerThread.start();
        consumerThread.start();

        producerThread.join();
        consumerThread.join();

        System.out.println("Source data : " + source);
        System.out.println("Destination data : " + destination);
    }
}
