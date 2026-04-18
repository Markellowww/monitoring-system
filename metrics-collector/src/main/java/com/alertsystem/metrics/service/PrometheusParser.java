package com.alertsystem.metrics.service;

import com.alertsystem.common.events.MetricsRawEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Парсер текстового формата Prometheus (Exposition format).
 * Пример строки: http_requests_total{method="POST",code="200"} 1027 1395066363000
 */
@Component
@Slf4j
public class PrometheusParser {

    public List<MetricsRawEvent.MetricPoint> parse(String text) {
        List<MetricsRawEvent.MetricPoint> points = new ArrayList<>();

        for (String rawLine : text.split("\n")) {
            String line = rawLine.strip();

            // Пропускаем комментарии и пустые строки
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            try {
                points.add(parseLine(line));
            } catch (Exception e) {
                log.debug("Skipping unparseable line: {}", line);
            }
        }

        return points;
    }

    private MetricsRawEvent.MetricPoint parseLine(String line) {
        Map<String, String> labels = new HashMap<>();
        String name;
        double value;

        int labelStart = line.indexOf('{');
        int labelEnd   = line.indexOf('}');

        String namePart;
        String remainder;

        if (labelStart > 0 && labelEnd > labelStart) {
            namePart  = line.substring(0, labelStart).strip();
            String labelStr = line.substring(labelStart + 1, labelEnd);
            remainder = line.substring(labelEnd + 1).strip();
            labels = parseLabels(labelStr);
        } else {
            int spaceIdx = line.indexOf(' ');
            namePart  = line.substring(0, spaceIdx).strip();
            remainder = line.substring(spaceIdx).strip();
        }

        name = namePart;

        // remainder может быть "value timestamp" или просто "value"
        String[] parts = remainder.strip().split("\\s+");
        value = Double.parseDouble(parts[0]);

        return MetricsRawEvent.MetricPoint.builder()
                .name(name)
                .value(value)
                .labels(labels)
                .build();
    }

    private Map<String, String> parseLabels(String labelStr) {
        Map<String, String> labels = new HashMap<>();
        if (labelStr.isBlank()) {
            return labels;
        }

        // key="value",key2="value2"
        for (String kv : labelStr.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")) {
            String[] pair = kv.split("=", 2);
            if (pair.length == 2) {
                String key = pair[0].strip();
                String val = pair[1].strip().replaceAll("^\"|\"$", "");
                labels.put(key, val);
            }
        }

        return labels;
    }
}
