package com.alertsystem.reports.service;

import com.alertsystem.reports.model.ReportData;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class PdfReportService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public byte[] generate(ReportData data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc)) {

            // Заголовок
            document.add(new Paragraph("Infrastructure Monitoring Report")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(String.format("Period: %s - %s",
                    data.getPeriodFrom().atZone(java.time.ZoneOffset.UTC).format(FMT),
                    data.getPeriodTo().atZone(java.time.ZoneOffset.UTC).format(FMT)))
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("\n"));

            // Сводка
            document.add(new Paragraph("Summary").setFontSize(16).setBold());
            document.add(new Paragraph("Total alerts fired: " + data.getTotalAlerts()));
            document.add(new Paragraph("Critical alerts:    " + data.getCriticalAlerts()));
            document.add(new Paragraph("Warning alerts:     " + data.getWarningAlerts()));

            document.add(new Paragraph("\n"));

            // Таблица алертов
            if (!data.getAlerts().isEmpty()) {
                document.add(new Paragraph("Alert History").setFontSize(16).setBold());

                Table table = new Table(UnitValue.createPercentArray(new float[]{30, 20, 15, 20, 15}))
                        .setWidth(UnitValue.createPercentValue(100));

                addHeaderCell(table, "Rule");
                addHeaderCell(table, "Source");
                addHeaderCell(table, "Severity");
                addHeaderCell(table, "Fired At");
                addHeaderCell(table, "Resolved At");

                for (ReportData.AlertSummary alert : data.getAlerts()) {
                    table.addCell(alert.getRuleName());
                    table.addCell(alert.getSourceName() != null ? alert.getSourceName() : "-");
                    table.addCell(alert.getSeverity());
                    table.addCell(alert.getFiredAt() != null
                            ? alert.getFiredAt().atZone(java.time.ZoneOffset.UTC).format(FMT) : "-");
                    table.addCell(alert.getResolvedAt() != null
                            ? alert.getResolvedAt().atZone(java.time.ZoneOffset.UTC).format(FMT) : "ACTIVE");
                }

                document.add(table);
            }

            log.info("PDF report generated: {} alerts", data.getTotalAlerts());

        } catch (Exception e) {
            log.error("Failed to generate PDF report", e);
            throw new RuntimeException("PDF generation failed", e);
        }

        return baos.toByteArray();
    }

    private void addHeaderCell(Table table, String text) {
        table.addCell(new Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY));
    }
}
