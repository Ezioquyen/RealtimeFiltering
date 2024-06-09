package org.example.consumer;

import lombok.Getter;
import org.example.redis.CallFilter;

import java.util.ArrayList;
import java.util.List;

public final class NotificationConsumerGroup {
    @Getter
    private final int numberOfConsumers;
    private final String groupId;
    private final String topic;
    private final String brokers;
    private List<NotificationConsumerThread> consumers;
    public NotificationConsumerGroup(String brokers, String groupId, String topic, int numberOfConsumers) {

        this.brokers = brokers;
        this.topic = topic;
        this.groupId = groupId;
        this.numberOfConsumers = numberOfConsumers;
        consumers = new ArrayList<>();
        for (int i = 0; i < this.numberOfConsumers; i++) {
            NotificationConsumerThread ncThread =
                    new NotificationConsumerThread(this.brokers, this.groupId, this.topic);
            consumers.add(ncThread);
        }
    }

    public void execute() {
        for (NotificationConsumerThread ncThread : consumers) {
            Thread t = new Thread(ncThread);
            t.start();
        }
    }

     String getGroupId() {
        return groupId;
    }



}
