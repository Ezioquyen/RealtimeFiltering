package org.example.client;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.config.Config;
import org.example.data.Call;
import org.example.serialize.JsonSerializer;

import java.util.*;


public class CallProducer {
    public  static volatile int count;
    public static void main(String[] args) {
        final var props = new Properties();
        Random random = new Random();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.BROKER);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        count = 0;


        Runtime.getRuntime().addShutdownHook(new Thread(() ->{

            System.out.println(count);}));

        //Call simulator
        try (var producer = new KafkaProducer<>(props)) {
            while (true) {
            List<Call> calls = genCall();
                calls.parallelStream().forEach(e -> {
                    e.setSendTime(System.currentTimeMillis());
                    producer.send(new ProducerRecord<>(Config.CONSUMER_TOPIC, e.getId(), e));
                });
                count += calls.toArray().length;
                Thread.sleep(random.nextInt(10));
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }



    }

    private static List<Call> genCall() {
        String[] prefixes = {"090", "091", "092", "093", "094", "095", "096", "097", "098", "099",
                "080", "081", "082", "083", "084", "085", "086", "087", "088", "089",
                "070", "071", "072", "073", "074", "075", "076", "077", "078", "079",
                "050", "051", "052", "053", "054", "055", "056", "057", "058", "059",
                "030", "031", "032", "033", "034", "035", "036", "037", "038", "039"};
        Random random = new Random();
        List<Call> calls = new ArrayList<>();
        for (int i = 0; i < random.nextInt(15); i++) {
            String id = UUID.randomUUID().toString();

            String caller;
            String receiver;

            do {
                String callerPrefix = prefixes[random.nextInt(prefixes.length)];
                String receiverPrefix = prefixes[random.nextInt(prefixes.length)];
                caller = callerPrefix + String.format("%07d", random.nextInt(10000000));
                receiver = receiverPrefix + String.format("%07d", random.nextInt(10000000));
            } while (caller.equals(receiver));

            int duration = random.nextInt(30);
            calls.add(new Call(id, caller, receiver, duration, 0L));
        }
        return calls;
    }

}

