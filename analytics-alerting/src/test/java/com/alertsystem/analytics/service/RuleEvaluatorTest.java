package com.alertsystem.analytics.service;

import com.alertsystem.analytics.model.AlertRuleConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RuleEvaluatorTest {

    private RuleEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new RuleEvaluator();
    }

    private AlertRuleConfig rule(String condition, double threshold) {
        return AlertRuleConfig.builder()
                .id(UUID.randomUUID())
                .name("test-rule")
                .condition(condition)
                .threshold(threshold)
                .build();
    }

    @Test
    void greaterThan_exceeded() {
        assertTrue(evaluator.evaluate(rule(">", 80.0), 90.0));
    }

    @Test
    void greaterThan_notExceeded() {
        assertFalse(evaluator.evaluate(rule(">", 80.0), 79.9));
    }

    @Test
    void greaterOrEqual_boundary() {
        assertTrue(evaluator.evaluate(rule(">=", 80.0), 80.0));
    }

    @Test
    void lessThan_below() {
        assertTrue(evaluator.evaluate(rule("<", 10.0), 5.0));
    }

    @Test
    void unknownCondition_returnsFalse() {
        assertFalse(evaluator.evaluate(rule("!=", 80.0), 90.0));
    }

    @Test
    void trendExceeded_above10percent() {
        assertTrue(evaluator.isTrendExceeded(100.0, 115.0, 10.0));
    }

    @Test
    void trendExceeded_zeroBase_returnsFalse() {
        assertFalse(evaluator.isTrendExceeded(0.0, 50.0, 10.0));
    }
}
