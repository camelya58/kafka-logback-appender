package com.github.camelya58.kafka_logback_appender.kafka_appender.keying;

public class NoKeyKeyingStrategy implements KeyingStrategy<Object> {

    @Override
    public String createKey(Object e) {
        return null;
    }
}
