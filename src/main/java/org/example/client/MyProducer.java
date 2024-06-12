package org.example.client;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.config.Config;
import org.example.data.Call;
import org.example.serialize.JsonSerializer;

import java.util.*;


public class MyProducer {

    public static void main(String[] args) {


        final var props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BROKER);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        List<String> phoneNumbers = List.of(
                "0982462855", "0943024933", "0974372733", "0932482393"
        );
        int numberOfCalls = 10000;
        Random random = new Random();
        List<Call> calls = new ArrayList<>();

        for (int i = 0; i < numberOfCalls; i++) {
            String fromPhoneNumber = phoneNumbers.get(random.nextInt(phoneNumbers.size()));
            String toPhoneNumber;
            do {
                toPhoneNumber = phoneNumbers.get(random.nextInt(phoneNumbers.size()));
            } while (toPhoneNumber.equals(fromPhoneNumber));

            calls.add(new Call(i, fromPhoneNumber, toPhoneNumber, random.nextInt(3600, 100000),System.currentTimeMillis()));
        }
        try (var producer = new KafkaProducer<>(props)) {

            calls.parallelStream().forEach(e-> {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                producer.send(new ProducerRecord<>(Config.CONSUMER_TOPIC, e.getId().toString(), e));});
        }
    }
}

