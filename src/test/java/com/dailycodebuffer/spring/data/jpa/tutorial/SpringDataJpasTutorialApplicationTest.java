package com.dailycodebuffer.spring.data.jpa.tutorial;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

@SpringBootTest
public class SpringDataJpasTutorialApplicationTest {
//    @Test
//    public void testDoFilter() throws Exception {
//        CustomFilter filter = new CustomFilter();
//        ServletRequest request = Mockito.mock(ServletRequest.class);
//        ServletResponse response = Mockito.mock(ServletResponse.class);
//        FilterChain chain = Mockito.mock(FilterChain.class);
//
//        filter.doFilter(request, response, chain);
//
//        Mockito.verify(chain).doFilter(request, response);
//    }


//    @Test
//    public void testWordCountStream() {
//        // Set up topology
//        StreamsBuilder streamsBuilder = new StreamsBuilder();
//        new KafkaStreamsConfig().wordCountStream(streamsBuilder);
//
//        Properties props = new Properties();
//        props.put("application.id", "test");
//        props.put("bootstrap.servers", "dummy:9092");
//        props.put("default.key.serde", Serdes.String().getClass());
//        props.put("default.value.serde", Serdes.String().getClass());
//
//        try (TopologyTestDriver testDriver = new TopologyTestDriver(streamsBuilder.build(), props)) {
//            TestInputTopic<String, String> inputTopic = testDriver.createInputTopic("input-topic", Serdes.String().serializer(), Serdes.String().serializer());
//            TestOutputTopic<String, Long> outputTopic = testDriver.createOutputTopic("output-topic", Serdes.String().deserializer(), Serdes.Long().deserializer());
//
//            inputTopic.pipeInput("key1", "hello kafka hello");
//
//            assertThat(outputTopic.readKeyValuesToMap()).containsEntry("hello", 2L).containsEntry("kafka", 1L);
//        }
//    }
}
