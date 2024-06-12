package org.example.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.config.Config;
import org.example.data.Response;
import org.example.serialize.JsonSerializer;

import java.util.Properties;

public class Producer {
    private final Properties props = new Properties();
    private static Producer INSTANCE;
    private KafkaProducer<String,Response> producer;
    private Producer() {
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BROKER);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        producer = new KafkaProducer<>(props);
    }
    public static Producer getInstance(){
        if(INSTANCE == null){
            System.out.println("producer-check-point");
            INSTANCE = new Producer();
        }
        return INSTANCE;
    }
    public void response(Response response, String id){
            producer.send(new ProducerRecord<>(Config.PRODUCER_TOPIC, id, response));

    }
}
