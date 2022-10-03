package com.github.bullettooth.logback.kafka;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class LogbackIntegrationIT extends WithKafkaContainer {

    private org.slf4j.Logger logger;

    @Before
    public void beforeLogSystemInit() {
        System.setProperty("KAFKA_URL", kafka.getBootstrapServers());

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            // Call context.reset() to clear any previous configuration, e.g. default
            // configuration. For multi-step configuration, omit calling context.reset().
            context.reset();
            URL config = Thread.currentThread().getContextClassLoader().getResource("logback-kafka.xml");
            configurator.doConfigure(Objects.requireNonNull(config));
        } catch (JoranException je) {
            // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);

        logger = LoggerFactory.getLogger("LogbackIntegrationIT");
    }

    @Test
    public void testLogging() {
        for (int i = 0; i < 1000; ++i) {
            logger.info("message" + i);
        }

        int no;
        try (KafkaConsumer<byte[], byte[]> client = createClient()) {
            client.assign(Collections.singletonList(new TopicPartition("logs", 0)));
            client.seekToBeginning(Collections.singletonList(new TopicPartition("logs", 0)));

            no = 0;

            ConsumerRecords<byte[], byte[]> poll = client.poll(1000);
            while (!poll.isEmpty()) {
                for (ConsumerRecord<byte[], byte[]> consumerRecord : poll) {
                    final String messageFromKafka = new String(consumerRecord.value(), StandardCharsets.UTF_8);
                    assertThat(messageFromKafka, Matchers.equalTo("message" + no));
                    ++no;
                }
                poll = client.poll(1000);
            }
        }

        assertEquals(1000, no);
    }
}
