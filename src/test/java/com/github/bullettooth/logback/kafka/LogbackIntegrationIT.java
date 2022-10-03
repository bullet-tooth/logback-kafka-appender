package com.github.bullettooth.logback.kafka;

import com.github.bullettooth.logback.kafka.util.TestKafka;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class LogbackIntegrationIT {

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    private TestKafka kafka;
    private org.slf4j.Logger logger;

    @Before
    public void beforeLogSystemInit() throws IOException, InterruptedException {
        kafka = TestKafka.createTestKafka(Collections.singletonList(9092));
        logger = LoggerFactory.getLogger("LogbackIntegrationIT");
    }

    @After
    public void tearDown() {
        kafka.shutdown();
        kafka.awaitShutdown();
    }


    @Test
    public void testLogging() {
        for (int i = 0; i < 1000; ++i) {
            logger.info("message" + i);
        }

        int no;
        try (KafkaConsumer<byte[], byte[]> client = kafka.createClient()) {
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
