package com.alertsystem.analytics.config;

import com.alertsystem.common.events.AlertEvent;
import com.alertsystem.common.events.ConfigUpdatedEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaTemplate<String, AlertEvent> alertKafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class,
                JsonSerializer.ADD_TYPE_INFO_HEADERS, false
        )));
    }

    @Bean
    public ConsumerFactory<String, ConfigUpdatedEvent> configConsumerFactory() {
        JsonDeserializer<ConfigUpdatedEvent> deser = new JsonDeserializer<>(ConfigUpdatedEvent.class, false);
        deser.addTrustedPackages("com.alertsystem.*");
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, "analytics-alerting-config",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"
        ), new StringDeserializer(), deser);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ConfigUpdatedEvent> configListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ConfigUpdatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(configConsumerFactory());
        return factory;
    }

    @Bean
    public NewTopic alertsTopic() {
        return TopicBuilder.name("alerts-topic").partitions(3).replicas(1).build();
    }
}
