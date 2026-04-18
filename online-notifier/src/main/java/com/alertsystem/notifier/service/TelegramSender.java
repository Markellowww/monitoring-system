package com.alertsystem.notifier.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramSender {

    private final WebClient.Builder webClientBuilder;

    @Value("${notifier.telegram.base-url:https://api.telegram.org}")
    private String telegramBaseUrl;

    /**
     * @param botToken  токен Telegram-бота
     * @param chatId    ID чата или канала
     * @param text      текст сообщения (до 4096 символов, поддерживает HTML)
     */
    public void send(String botToken, String chatId, String text) {
        String url = telegramBaseUrl + "/bot" + botToken + "/sendMessage";

        Map<String, String> body = Map.of(
                "chat_id",    chatId,
                "text",       text,
                "parse_mode", "HTML"
        );

        webClientBuilder.build()
                .post()
                .uri(url)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnSuccess(resp -> log.debug("Telegram message sent to chatId={}", chatId))
                .doOnError(e -> log.error("Failed to send Telegram message: {}", e.getMessage()))
                .block();
    }

    public String formatAlert(String ruleName, String sourceName,
                               String status, double value, double threshold, String severity) {
        String emoji = switch (severity) {
            case "CRITICAL" -> "🔴";
            case "WARNING"  -> "🟡";
            default         -> "🔵";
        };

        if ("RESOLVED".equals(status)) {
            return String.format(
                    "✅ <b>RESOLVED</b>\nRule: <b>%s</b>\nSource: %s\nValue: %.2f",
                    ruleName, sourceName, value);
        }

        return String.format(
                "%s <b>ALERT: %s</b>\nRule: <b>%s</b>\nSource: %s\nValue: <b>%.2f</b> (threshold: %.2f)",
                emoji, severity, ruleName, sourceName, value, threshold);
    }
}
