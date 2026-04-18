package com.alertsystem.tsdb.service;

import com.alertsystem.common.events.MetricsRawEvent;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class InfluxDbService {

    private final WriteApi writeApi;
    private final InfluxDBClient influxDBClient;
    private final String influxBucket;
    private final String influxOrg;

    public InfluxDbService(WriteApi writeApi,
                           InfluxDBClient influxDBClient,
                           @Qualifier("influxBucket") String influxBucket,
                           @Qualifier("influxOrg") String influxOrg) {
        this.writeApi = writeApi;
        this.influxDBClient = influxDBClient;
        this.influxBucket = influxBucket;
        this.influxOrg = influxOrg;
    }

    public void writeMetrics(MetricsRawEvent event) {
        List<Point> points = new ArrayList<>();

        for (MetricsRawEvent.MetricPoint metric : event.getMetrics()) {
            Point point = Point.measurement(metric.getName())
                    .addTag("source_id", event.getSourceId().toString())
                    .addTag("source_name", event.getSourceName())
                    .addField("value", metric.getValue())
                    .time(event.getTimestamp().toEpochMilli(), WritePrecision.NS);

            if (metric.getLabels() != null) {
                metric.getLabels().forEach(point::addTag);
            }

            points.add(point);
        }

        writeApi.writePoints(influxBucket, influxOrg, points);
        log.debug("Wrote {} points for source: {}", points.size(), event.getSourceName());
    }

    /**
     * Запрос метрики за период.
     *
     * @param metricName имя измерения (measurement)
     * @param sourceId   UUID источника (фильтр по тегу)
     * @param from       начало периода
     * @param to         конец периода
     * @return список {time, value}
     */
    public List<Map<String, Object>> queryRange(String metricName, String sourceId,
                                                Instant from, Instant to) {
        String flux = String.format("""
                        from(bucket: "%s")
                          |> range(start: %s, stop: %s)
                          |> filter(fn: (r) => r._measurement == "%s")
                          |> filter(fn: (r) => r.source_id == "%s")
                          |> filter(fn: (r) => r._field == "value")
                        """,
                influxBucket,
                from.toString(),
                to.toString(),
                metricName,
                sourceId);

        return executeQuery(flux);
    }

    /**
     * Последнее значение метрики.
     */
    public Optional<Double> queryLatest(String metricName, String sourceId) {
        String flux = String.format("""
                        from(bucket: "%s")
                          |> range(start: -5m)
                          |> filter(fn: (r) => r._measurement == "%s")
                          |> filter(fn: (r) => r.source_id == "%s")
                          |> filter(fn: (r) => r._field == "value")
                          |> last()
                        """,
                influxBucket, metricName, sourceId);

        List<Map<String, Object>> result = executeQuery(flux);
        if (result.isEmpty()) {
            return Optional.empty();
        }
        Object val = result.get(0).get("value");
        return val instanceof Number n ? Optional.of(n.doubleValue()) : Optional.empty();
    }

    /**
     * Среднее значение метрики за последние N секунд - для движка алертинга.
     */
    public OptionalDouble queryAvg(String metricName, String sourceId, int lastSeconds) {
        String flux = String.format("""
                        from(bucket: "%s")
                          |> range(start: -%ds)
                          |> filter(fn: (r) => r._measurement == "%s")
                          |> filter(fn: (r) => r.source_id == "%s")
                          |> filter(fn: (r) => r._field == "value")
                          |> mean()
                        """,
                influxBucket, lastSeconds, metricName, sourceId);

        List<Map<String, Object>> result = executeQuery(flux);
        if (result.isEmpty()) {
            return OptionalDouble.empty();
        }
        Object val = result.get(0).get("value");
        return val instanceof Number n ? OptionalDouble.of(n.doubleValue()) : OptionalDouble.empty();
    }

    private List<Map<String, Object>> executeQuery(String flux) {
        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(flux, influxOrg);
        List<Map<String, Object>> result = new ArrayList<>();

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("time", record.getTime());
                row.put("value", record.getValue());
                row.putAll(record.getValues());
                result.add(row);
            }
        }

        return result;
    }
}
