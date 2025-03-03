package io.github.bullettooth.logback.kafka.util;

import ch.qos.logback.classic.Logger;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.slf4j.LoggerFactory;

@AxisRange(min = 0, max = 5)
@BenchmarkMethodChart(filePrefix = "benchmark-lists")
@BenchmarkHistoryChart(labelWith = LabelType.CUSTOM_KEY, maxRuns = 20)
@Ignore("Do not run during build")
public class KafkaAppenderBenchmarkTest {

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();
    private ch.qos.logback.classic.Logger logger;

    @Before
    public void before() {
        LoggerFactory.getLogger("triggerLogInitialization");
        logger = (Logger) LoggerFactory.getLogger("IT");
    }

    @SuppressWarnings("java:S2699")
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 2, concurrency = 8)
    @Test
    public void benchmark() {
        for (int i = 0; i < 100000; ++i) {
            logger.info("A VERY IMPORTANT LOG MESSAGE {}", i);
        }
    }
}
