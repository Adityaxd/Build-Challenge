package com.example.challenge.assignment1;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ProducerConsumer implementation.
 * Tests include:
 * 1. Constructor validation for non-positive capacity
 * 2. Ensuring all items from source are moved to destination
 * 3. Blocking behavior of put() when queue is full
 * 4. Blocking behavior of take() when queue is empty
 */

class ProducerConsumerTest {

    // Test for constructor validation
    @Test
    void constructorRejectsNonPositiveCapacity() {
        assertThrows(IllegalArgumentException.class,
                () -> new BoundedBlockingQueue<Integer>(0));

        assertThrows(IllegalArgumentException.class,
                () -> new BoundedBlockingQueue<Integer>(-5));
    }

    @Test
    void allItemsMoveFromSourceToDestination() throws InterruptedException {
        List<Integer> source = Arrays.asList(10, 20, 30, 40, 50);
        List<Integer> destination = new ArrayList<>();

        BoundedBlockingQueue<Integer> queue = new BoundedBlockingQueue<>(2);

        Thread producer = new Thread(new Producer(source, queue));
        Thread consumer = new Thread(new Consumer(queue, destination));

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();

        // Consume all items from the queue and consume in order
        assertEquals(source, destination);
    }

    // Test for put() blocking behavior
    @Test
    void putBlocksWhenQueueIsFullUntilSpaceIsFreed() throws InterruptedException {
        BoundedBlockingQueue<Integer> queue = new BoundedBlockingQueue<>(1);

        // Fill the queue so it becomes full
        queue.put(1);

        Thread producer = new Thread(() -> {
            try {
                // This call should block until someone takes the first element
                queue.put(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();

        // Give the producer some time to hit the blocking put()
        Thread.sleep(100);

        // Now producer should still be blocked
        assertTrue(producer.isAlive(), "Producer should be blocked when queue is full");

        // Now unblock the producer by taking an element
        Integer first = queue.take();
        assertEquals(1, first);

        // After unblocking, the producer should finish 
        producer.join(500);

        assertFalse(producer.isAlive(), "Producer should finish after space is freed");

        // Now we can take the second element from the queue
        Integer second = queue.take();
        assertEquals(2, second);
    }

    // Test for take() blocking behavior
    @Test
    void takeBlocksWhenQueueIsEmptyUntilElementIsAvailable() throws InterruptedException {
        BoundedBlockingQueue<Integer> queue = new BoundedBlockingQueue<>(1);

        final List<Integer> resultHolder = new ArrayList<>(1);

        Thread consumer = new Thread(() -> {
            try {
                // This call should block until an element is put into the queue
                Integer value = queue.take();
                resultHolder.add(value);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        consumer.start();

        // Give the consumer some time to hit the blocking take()
        Thread.sleep(100);

        assertTrue(consumer.isAlive(), "Consumer should be blocked when queue is empty");

        // Put a value to unblock the consumer
        queue.put(42);

        // Wait for consumer to consume the value
        consumer.join(500);

        assertFalse(consumer.isAlive(), "Consumer should finish after element is available");
        assertEquals(1, resultHolder.size());
        assertEquals(42, resultHolder.get(0));
    }
}
