package org.example.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.config.Config;
import org.example.data.Call;
import org.example.redis.CallFilter;
import org.example.serialize.JsonDeserializer;

import java.time.Duration;

import java.util.List;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationConsumerThread {

    private final KafkaConsumer<String, Call> consumer;

    private CallFilter callFilter;
    public NotificationConsumerThread() {
        Properties prop = createConsumerConfig();
        this.consumer = new KafkaConsumer<>(prop);
        this.consumer.subscribe(List.of(Config.CONSUMER_TOPIC));
    }

    private static Properties createConsumerConfig() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BROKER);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, Config.GROUP_ID);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,Config.MESSAGES_POLL);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.VALUE_CLASS_NAME_CONFIG, Call.class);


        return props;
    }


    public void run() {
        callFilter = CallFilter.getInstance();
        ExecutorService executorService = Executors.newFixedThreadPool(Config.NUMBER_OF_THREAD_PER_CONSUMER);
        while (true) {
            ConsumerRecords<String, Call> records = consumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord<String, Call> record : records) {
                executorService.submit(()->{
                    callFilter.handleCall(record.value());
                });
            }

        }
    }
}
