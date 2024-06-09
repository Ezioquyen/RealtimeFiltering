package org.example.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.serialize.JsonSerializer;

import java.util.Properties;

import java.util.stream.IntStream;

public class MyProducer {

    public static void main(String[] args) {


        final var props = new Properties();
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "producer");
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:8099, localhost:8097, localhost:8098");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        try (var producer = new KafkaProducer<String, String>(props)) {
            IntStream.range(0, 10000)
                    .parallel()
                    .forEach(_ -> {

                        producer.send(new ProducerRecord<>("phone-call", "0982462855"));
                    });
        }
    }

}

