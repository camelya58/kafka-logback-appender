package com.github.camelya58.kafka_logback_appender.kafka_appender.keying;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * This strategy uses the logger level as partitioning key. This ensures that all messages logged by the
 * same logger level will remain in the correct order for any consumer.
 */
public class LoggerLevelKeyingStrategy implements KeyingStrategy<ILoggingEvent> {

    @Override
    public String createKey(ILoggingEvent e) {
        final String loggerLevel;
        if (e.getLevel() == null) {
            loggerLevel = "";
        } else {
            loggerLevel = e.getLevel().levelStr;
        }
        return loggerLevel;
    }

}
