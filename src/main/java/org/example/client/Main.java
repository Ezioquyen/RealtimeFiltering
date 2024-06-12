package org.example.client;



public class Main {
    public static void main(String[] args) {
        MyConsumer consumer = new MyConsumer();
        consumer.run();
        try {
            Thread.sleep(100000);
        } catch (InterruptedException _) {

        }
    }
}
