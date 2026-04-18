package com.alertsystem.analytics.service;

import com.alertsystem.analytics.model.AlertRuleConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Вычисляет, нарушено ли условие правила для заданного значения метрики.
 */
@Component
@Slf4j
public class RuleEvaluator {

    /**
     * @return true если условие нарушено (алерт должен сработать)
     */
    public boolean evaluate(AlertRuleConfig rule, double metricValue) {
        return switch (rule.getCondition()) {
            case ">"  -> metricValue > rule.getThreshold();
            case ">=" -> metricValue >= rule.getThreshold();
            case "<"  -> metricValue < rule.getThreshold();
            case "<=" -> metricValue <= rule.getThreshold();
            default -> {
                log.warn("Unknown condition '{}' in rule {}", rule.getCondition(), rule.getId());
                yield false;
            }
        };
    }

    /**
     * Простой анализ тренда: рост метрики более чем на trendThresholdPercent% за окно.
     * Используется для pre-alerting.
     *
     * @param firstValue  значение в начале окна
     * @param lastValue   текущее значение
     * @param trendThresholdPercent  порог роста в %
     */
    public boolean isTrendExceeded(double firstValue, double lastValue, double trendThresholdPercent) {
        if (firstValue == 0) {
            return false;
        }
        double growthPercent = ((lastValue - firstValue) / Math.abs(firstValue)) * 100;
        return growthPercent > trendThresholdPercent;
    }
}
