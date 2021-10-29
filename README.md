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

To see it use the following command:
```
./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic main_topic --from-beginning --consumer.config ./config/consumer.properties
```

```
2021-10-29 13:06:04 [main] INFO  LoggerRoot -
------------------------------------------
	Application 'LoggerRoot' is running!
	Access URL: http://127.0.0.1:8090/swagger-ui.html


2021-10-29 13:06:33 [http-nio-8090-exec-1] INFO  o.a.c.c.C.[Tomcat].[localhost].[/] - Initializing Spring DispatcherServlet 'dispatcherServlet'
```
## Step 7
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
                     <!--    with timezone   -->
       <timestampFormat>yyyy-MM-dd HH:mm:ss.SSSXXX</timestampFormat>
                     <!--    if you need in UTC add    -->
       <timestampFormatTimezoneId>Etc/UTC</timestampFormatTimezoneId>
       <jsonFormatter class="ch.qos.logback.contrib.jackson.JacksonJsonFormatter">
           <prettyPrint>true</prettyPrint>
       </jsonFormatter>
   </layout>
</encoder>
```
The message will be like the following:
```
{
  "timestamp" : "2021-10-29 15:19:13.530+03:00",
  "level" : "INFO",
  "thread" : "main",
  "logger" : "LoggerRoot",
  "message" : "\n------------------------------------------\n\tApplication 'LoggerRoot' is running!\n\tAccess URL: http://127.0.0.1:8090/swagger-ui.html\n",
  "context" : "default"
}
```

## Step 8
If you need custom fields:
- add the dependencies:
```xml
   <dependency>
            <groupId>net.logstash.logback</groupId>
            <artifactId>logstash-logback-encoder</artifactId>
            <version>6.6</version>
   </dependency>
```
- change encoder to a logback.xml:
```xml
 <encoder class="net.logstash.logback.encoder.LogstashEncoder">
                    <jsonGeneratorDecorator class="net.logstash.logback.decorate.PrettyPrintingJsonGeneratorDecorator"/>
                    <includeContext>false</includeContext>
                    <fieldNames>
                        <timestamp>@timestamp</timestamp>
                        <version>[ignore]</version>
                        <level>level</level>
                        <levelValue>[ignore]</levelValue>
                        <caller>[ignore]</caller>
                        <thread>thread</thread>
                        <logger>logger</logger>
                        <message>message</message>
                        <caller>trace</caller>
                        <stackTrace>exception</stackTrace>
                        <!-- if context is included -->
                        <mdc>context</mdc>
                    </fieldNames>
                    <customFields>{"topic":"${main-topic}"}</customFields>
 </encoder>
```
The message will be like the following:
```
{
  "@timestamp" : "2021-10-29T15:37:10.324+03:00",
  "message" : "\n------------------------------------------\n\tApplication 'LoggerRoot' is running!\n\tAccess URL: http://127.0.0.1:8090/swagger-ui.html\n",
  "logger" : "LoggerRoot",
  "thread" : "main",
  "level" : "INFO",
  "topic" : "main_topic"
}
```
## Step 9
To hide some sensitive information like password add to encoder:
```xml
   <encoder>
        <pattern>
            %d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %replace(%msg){'password=.+', 'password=******'}%n
        </pattern>
   </encoder>
```
For kafka appender add inside encoder:
```xml
<provider class="net.logstash.logback.composite.loggingevent.LoggingEventPatternJsonProvider">
    <pattern>
        {
          "message": "%replace(%msg){'password=.+', 'password=******'}"
        }
    </pattern>
</provider>
```
## Step 10
You can switch on/off logging to kafka.
- add property to application.yml:
```yml
logsToKafka: true
```
- add to logback-spring.xml:
```xml
<springProperty scope="context" name="logsToKafka" source="logsToKafka"/>
<if condition='property("logsToKafka").contains("true")'>
        <then>
            <appender name="main-kafka-appender"
                      class="com.github.camelya58.kafka_logback_appender.kafka_appender.KafkaAppender">
             <!-- your code-->
            </appender>
            <root level="INFO">
                <appender-ref ref="main-kafka-appender"/>
            </root>
        </then>
</if>
```
## Step 11
To add logging to a file:
- add to application.yml:
```yml
logsToFile:
  logsFolder: api_logs
  fileNameDateFormat: yyyy_MM_dd_HH-mm
```
- add to logback-spring.xml:
```xml
    <appender name="FILE-AUDIT"
              class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/main.log</file>
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <pattern>%d{HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${logsFolder}/%d{yyyy/MM, aux}/main-%d{yyyy-MM-dd-HH}-%i.log</fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy
                    class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>100MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
        </rollingPolicy>
    </appender>
```

Every file will be named as a date and be hold in a folder "api_logs" with a folder 
as a year and folders with months.
Every hour in a separate file.

But the current hour will be hold in a folder "logs".

The code with spring see on the branch - "spring_version".