package org.example;

import org.example.consumer.NotificationConsumerGroup;
import org.example.redis.CallFilter;

public class Main {
    public static void main(String[] args) {

        String brokers = "localhost:8099, localhost:8097, localhost:8098";
        String groupId = "call-receiver";
        String topic = "phone-call";
        int numberOfConsumer = 3;
        CallFilter callFilter = CallFilter.getInstance();
        if (args != null && args.length > 4) {
            brokers = args[0];
            groupId = args[1];
            topic = args[2];
            numberOfConsumer = Integer.parseInt(args[3]);
        }
        // Start group of Notification Consumers
        NotificationConsumerGroup consumerGroup =
                new NotificationConsumerGroup(brokers, groupId, topic, numberOfConsumer);

        consumerGroup.execute();



        try {
            Thread.sleep(100000);
        } catch (InterruptedException _) {

        }
    }
}
