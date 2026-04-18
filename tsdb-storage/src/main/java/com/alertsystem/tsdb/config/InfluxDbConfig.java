package com.alertsystem.tsdb.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.WriteOptions;
import com.influxdb.client.WriteApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxDbConfig {

    @Value("${influxdb.url}")
    private String url;

    @Value("${influxdb.token}")
    private String token;

    @Value("${influxdb.org}")
    private String org;

    @Value("${influxdb.bucket}")
    private String bucket;

    @Value("${influxdb.batch-size:500}")
    private int batchSize;

    @Value("${influxdb.flush-interval-ms:1000}")
    private int flushIntervalMs;

    @Bean
    public InfluxDBClient influxDBClient() {
        return InfluxDBClientFactory.create(
                InfluxDBClientOptions.builder()
                        .url(url)
                        .authenticateToken(token.toCharArray())
                        .org(org)
                        .bucket(bucket)
                        .build()
        );
    }

    @Bean
    public WriteApi writeApi(InfluxDBClient client) {
        return client.makeWriteApi(
                WriteOptions.builder()
                        .batchSize(batchSize)
                        .flushInterval(flushIntervalMs)
                        .build()
        );
    }

    @Bean
    public String influxBucket() {
        return bucket;
    }

    @Bean
    public String influxOrg() {
        return org;
    }
}
