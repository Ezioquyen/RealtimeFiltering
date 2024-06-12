package org.example;


import org.example.consumer.NotificationConsumerThread;
import org.example.redis.CallFilter;

public class Main {

    public static void main(String[] args) {
        CallFilter.getInstance();
        NotificationConsumerThread consumer = new NotificationConsumerThread();

        consumer.run();

        try {
            Thread.sleep(100000);
        } catch (InterruptedException _) {

        }
    }
}
