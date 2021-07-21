package com.github.camelya58.kafka_logback_appender.kafka_appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import com.github.camelya58.kafka_logback_appender.kafka_appender.delivery.FailedDeliveryCallback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class KafkaAppender<E> extends KafkaAppenderConfig<E> {

    /**
     * Kafka clients uses this prefix for its slf4j logging.
     * This appender defers appends of any Kafka logs since it could cause harmful infinite recursion/self feeding effects.
     */
    private static final String KAFKA_LOGGER_PREFIX = KafkaProducer.class.getPackage().getName().replaceFirst("\\.producer$", "");

    private LazyProducer lazyProducer = null;
    private final AppenderAttachableImpl<E> aai = new AppenderAttachableImpl<>();
    private final ConcurrentLinkedQueue<E> queue = new ConcurrentLinkedQueue<>();
    private final FailedDeliveryCallback<E> failedDeliveryCallback = (evt, throwable) -> aai.appendLoopOnAppenders(evt);

    public KafkaAppender() {
        addProducerConfigValue(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        addProducerConfigValue(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
    }

    @Override
    public void doAppend(E e) {
        ensureDeferredAppends();
        if (e instanceof ILoggingEvent && ((ILoggingEvent)e).getLoggerName().startsWith(KAFKA_LOGGER_PREFIX)) {
            deferAppend(e);
        } else {
            super.doAppend(e);
        }
    }

    @Override
    public void start() {
        // only error free appenders should be activated
        if (!checkPrerequisites()) return;

        if (partition != null && partition < 0) {
            partition = null;
        }

        lazyProducer = new LazyProducer();

        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        if (lazyProducer != null && lazyProducer.isInitialized()) {
            try {
                lazyProducer.get().close();
            } catch (KafkaException e) {
                this.addWarn("Failed to shut down kafka producer: " + e.getMessage(), e);
            }
            lazyProducer = null;
        }
    }

    @Override
    public void addAppender(Appender<E> newAppender) {
        aai.addAppender(newAppender);
    }

    @Override
    public Iterator<Appender<E>> iteratorForAppenders() {
        return aai.iteratorForAppenders();
    }

    @Override
    public Appender<E> getAppender(String name) {
        return aai.getAppender(name);
    }

    @Override
    public boolean isAttached(Appender<E> appender) {
        return aai.isAttached(appender);
    }

    @Override
    public void detachAndStopAllAppenders() {
        aai.detachAndStopAllAppenders();
    }

    @Override
    public boolean detachAppender(Appender<E> appender) {
        return aai.detachAppender(appender);
    }

    @Override
    public boolean detachAppender(String name) {
        return aai.detachAppender(name);
    }

    @Override
    protected void append(E e) {
        final byte[] payload = encoder.encode(e);
        final String key = keyingStrategy.createKey(e);

        final ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, partition, key, payload);

        final Producer<String, byte[]> producer = lazyProducer.get();
        if (producer != null) {
            deliveryStrategy.send(lazyProducer.get(), record, e, failedDeliveryCallback);
        } else {
            failedDeliveryCallback.onFailedDelivery(e, null);
        }
    }

    protected Producer<String, byte[]> createProducer() {
        return new KafkaProducer<>(new HashMap<>(producerConfig));
    }

    private void deferAppend(E event) {
        queue.add(event);
    }

    // drains queue events to super
    private void ensureDeferredAppends() {
        E event;

        while ((event = queue.poll()) != null) {
            super.doAppend(event);
        }
    }

    /**
     * Lazy initializer for producer, patterned after commons-lang.
     *
     * @see <a href="https://commons.apache.org/proper/commons-lang/javadocs/api-3.4/org/apache/commons/lang3/concurrent/LazyInitializer.html">LazyInitializer</a>
     */
    private class LazyProducer {

        private volatile Producer<String, byte[]> producer;

        public Producer<String, byte[]> get() {
            Producer<String, byte[]> result = this.producer;
            if (result == null) {
                synchronized(this) {
                    result = this.producer;
                    if(result == null) {
                        this.producer = result = this.initialize();
                    }
                }
            }

            return result;
        }

        protected Producer<String, byte[]> initialize() {
            Producer<String, byte[]> producer = null;
            try {
                producer = createProducer();
            } catch (Exception e) {
                addError("error creating producer", e);
            }
            return producer;
        }

        public boolean isInitialized() { return producer != null; }
    }

}
