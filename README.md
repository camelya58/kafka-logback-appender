# kafka-logback-appender

Simple project with Apache Kafka and logback logging.

## Step 1
Create maven project and add the dependencies.
```xml
 <dependencies>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>1.2.3</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>1.7.25</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
            <version>2.3.7.RELEASE</version>
        </dependency>
    </dependencies>
```

## Step 2
Create classes for keying strategy and delivery strategy.

## Step 3
Create classes KafkaAppenderConfig and KafkaAppender.

## Step 4
Create logback.xml.
We can divide logging by level. For example, level ERROR send to another topic. 
```xml
<configuration>
    <property name="address" value="localhost:9092"/>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <target>System.out</target>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="STDERR" class="ch.qos.logback.core.ConsoleAppender">
        <target>System.err</target>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="main-kafka-appender" class="com.github.camelya58.kafka_logback_appender.kafka_appender.KafkaAppender">
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>

        <topic>main_topic</topic>
        <keyingStrategy class="com.github.camelya58.kafka_logback_appender.kafka_appender.keying.LoggerLevelKeyingStrategy" />
        <deliveryStrategy class="com.github.camelya58.kafka_logback_appender.kafka_appender.delivery.AsynchronousDeliveryStrategy" />

        <producerConfig>bootstrap.servers=${address}</producerConfig>
        <producerConfig>acks=0</producerConfig>
        <producerConfig>linger.ms=100</producerConfig>
        <producerConfig>max.block.ms=100</producerConfig>
        <producerConfig>client.id=${HOSTNAME}-${CONTEXT_NAME}-logback-main</producerConfig>

    </appender>

    <appender name="error-kafka-appender" class="com.github.camelya58.kafka_logback_appender.kafka_appender.KafkaAppender">

        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>ERROR</level>
        </filter>

        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>

        <topic>error_topic</topic>
        <keyingStrategy class="com.github.camelya58.kafka_logback_appender.kafka_appender.keying.LoggerLevelKeyingStrategy" />
        <deliveryStrategy class="com.github.camelya58.kafka_logback_appender.kafka_appender.delivery.AsynchronousDeliveryStrategy"/>

        <producerConfig>bootstrap.servers=${address}</producerConfig>

        <producerConfig>client.id=${HOSTNAME}-${CONTEXT_NAME}-logback-error</producerConfig>
        <producerConfig>compression.type=gzip</producerConfig>

        <appender-ref ref="STDERR"/>
    </appender>

    <root level="info">
        <appender-ref ref="main-kafka-appender" />
        <appender-ref ref="error-kafka-appender" />
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
```

For spring applications we can use properties from application.properties or application.yml.
```properties
spring.kafka.bootstrap_server=localhost:9092
```
Use logback-spring.xml and add to it:
```xml
<springProperty scope="context" name="address" source="spring.kafka.bootstrap_server"/>
```

## Step 5
Create simple class.
```java

public class LoggerRoot {
    private static final Logger logger = LoggerFactory.getLogger(LoggerRoot.class.getSimpleName());

    public static void main(String... args) throws InterruptedException {
        int counter = 0;
        while (counter < 10) {
            logger.info("Counter:" + counter);
            counter++;

        }
        logger.error("Counter:" + counter);
        Thread.sleep(1000);
        logger.info("Completed");
        logger.error("Completed");
    }
}
```
## Step 6
Run Application. The log will look like in a console.

If you want to see log in a json format you need to do the following:
- add the dependencies:
```xml
<dependency>
   <groupId>com.fasterxml.jackson.core</groupId>
   <artifactId>jackson-databind</artifactId>
   <version>2.11.4</version>
</dependency>
<!-- ch.qos.logback.contrib.jackson.JacksonJsonFormatter -->
<dependency>
   <groupId>ch.qos.logback.contrib</groupId>
   <artifactId>logback-jackson</artifactId>
   <version>0.1.5</version>
</dependency>

<!-- ch.qos.logback.contrib.json.classic.JsonLayout -->
<dependency>
   <groupId>ch.qos.logback.contrib</groupId>
   <artifactId>logback-json-classic</artifactId>
   <version>0.1.5</version>
</dependency>
```
- change encoder to a logback.xml:
```xml
<encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
   <layout class="ch.qos.logback.contrib.json.classic.JsonLayout">
       <timestampFormat>yyyy-MM-dd HH:mm:ss.SSS</timestampFormat>
       <jsonFormatter class="ch.qos.logback.contrib.jackson.JacksonJsonFormatter">
           <prettyPrint>true</prettyPrint>
       </jsonFormatter>
   </layout>
</encoder>
```
