package com.github.camelya58.kafka_logback_appender.kafka_appender.delivery;


public interface FailedDeliveryCallback<E> {
    void onFailedDelivery(E evt, Throwable throwable);
}
