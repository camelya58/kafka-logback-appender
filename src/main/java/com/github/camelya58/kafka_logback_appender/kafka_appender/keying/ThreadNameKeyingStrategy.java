package com.github.camelya58.kafka_logback_appender.kafka_appender.keying;

import ch.qos.logback.classic.spi.ILoggingEvent;

import java.nio.ByteBuffer;

/**
 * This strategy uses the calling threads name as partitioning key. This ensures that all messages logged by the
 * same thread will remain in the correct order for any consumer.
 * But this strategy can lead to uneven log distribution for a small number of thread(-names) (compared to the number of partitions).
 */
public class ThreadNameKeyingStrategy implements KeyingStrategy<ILoggingEvent> {

    @Override
    public String createKey(ILoggingEvent e) {
        return e.getThreadName();
    }
}
