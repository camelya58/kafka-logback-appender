package com.github.camelya58.kafka_logback_appender.kafka_appender.keying;

public interface KeyingStrategy<E> {

    /**
     * creates a string key for the given {@link ch.qos.logback.classic.spi.ILoggingEvent}
     * @param e the logging event
     * @return a key
     */
    String createKey(E e);

}
