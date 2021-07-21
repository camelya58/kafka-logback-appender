package com.github.camelya58.kafka_logback_appender.kafka_appender.delivery;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.errors.TimeoutException;

public class AsynchronousDeliveryStrategy implements DeliveryStrategy {

    @Override
    public <K, V, E> boolean send(Producer<K, V> producer, ProducerRecord<K, V> record, final E event,
                                  final FailedDeliveryCallback<E> failedDeliveryCallback) {
        try {
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    failedDeliveryCallback.onFailedDelivery(event, exception);
                }
            });
            return true;
        } catch (TimeoutException e) {
            failedDeliveryCallback.onFailedDelivery(event, e);
            return false;
        }
    }
}
