package org.example.consumer;

import lombok.Getter;
import org.example.config.Config;



import java.util.List;

@Getter
public final class NotificationConsumerGroup {
    private List<NotificationConsumerThread> consumers;
    public NotificationConsumerGroup() {


        /*consumers = new ArrayList<>();
        for (int i = 0; i < Config.NUMBER_OF_CONSUMER; i++) {
            NotificationConsumerThread ncThread =
                    new NotificationConsumerThread();
            consumers.add(ncThread);
        }*/
    }
   /* public void execute() {
        for (NotificationConsumerThread ncThread : consumers) {
            Thread t = new Thread(ncThread);
            t.start();
        }
    }*/

}
