package org.example;


import org.example.consumer.NotificationConsumerThread;

public class Main {

    public static void main(String[] args) {
        NotificationConsumerThread consumer = new NotificationConsumerThread();
        consumer.run();
    }
}
