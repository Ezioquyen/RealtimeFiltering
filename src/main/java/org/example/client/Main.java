package org.example.client;



public class Main {
    public static volatile int count;
    public static void main(String[] args) {
        ResponseConsumer consumer = new ResponseConsumer();
        Runtime.getRuntime().addShutdownHook(new Thread(() ->{
            System.out.println(count);}));
        consumer.run();
    }
}
