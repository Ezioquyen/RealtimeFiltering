package org.example.client;

import lombok.Data;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.config.Config;
import org.example.data.Response;
import org.example.serialize.JsonDeserializer;


import java.util.List;
import java.util.Properties;

@Data

public class MyConsumer {
    private final KafkaConsumer<String, Response> consumer = new KafkaConsumer<>(createConsumerConfig());

    public MyConsumer() {
        consumer.subscribe(List.of(Config.PRODUCER_TOPIC));
    }


    public void run() {
        long currentTime;
        while (true) {
            ConsumerRecords<String, Response> records = consumer.poll(100);
            for (ConsumerRecord<String, Response> record : records) {
                if (record.key().equals("5001")) {
                    currentTime = System.currentTimeMillis();
                    System.out.println("id 2000: " + record.value().getMessage());
                    System.out.println("about: "+ (currentTime-record.value().getTime()) + "ms");
                }
            }
        }
    }

    private static Properties createConsumerConfig() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BROKER);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "client");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, Config.MESSAGES_POLL);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.VALUE_CLASS_NAME_CONFIG, Response.class);
        return props;
    }
}
