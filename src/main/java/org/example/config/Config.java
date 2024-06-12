package org.example.config;

import lombok.Getter;

@Getter
public class Config {
    public static final String BROKER = "localhost:8097";
    public static final String GROUP_ID = "call-receiver";
    public static final String CONSUMER_TOPIC = "phone-call";
    /*public static final int NUMBER_OF_CONSUMER = 1;*/
    public static final int NUMBER_OF_THREAD_PER_CONSUMER = 10;
    public static final int MESSAGES_POLL = 500;
    public static final int MAX_DAILY_CALL = 10;
    public static final int MIN_TIME_INTERVAL = 30000;
    public static final String PRODUCER_TOPIC = "call-response";
}
