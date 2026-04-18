package com.alertsystem.tsdb.config;

import com.alertsystem.common.events.MetricsRawEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, MetricsRawEvent> metricsConsumerFactory() {
        JsonDeserializer<MetricsRawEvent> deser = new JsonDeserializer<>(MetricsRawEvent.class, false);
        deser.addTrustedPackages("com.alertsystem.*");
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, "tsdb-storage",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest",
                ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "500"
        ), new StringDeserializer(), deser);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MetricsRawEvent> metricsListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, MetricsRawEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(metricsConsumerFactory());
        factory.setConcurrency(3);
        factory.setBatchListener(false);
        return factory;
    }
}
