package edu.trincoll.service.report;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ReportService Tests")
class ReportServiceTest {

    private final ReportGenerator availableGenerator = new ReportGenerator() {
        @Override
        public boolean supports(String reportType) {
            return "available".equalsIgnoreCase(reportType);
        }

        @Override
        public String generate() {
            return "Available books: 5";
        }
    };

    private final ReportService reportService = new ReportService(List.of(availableGenerator));

    @Test
    @DisplayName("generate: returns output from matching generator")
    void generateReturnsMatchingReport() {
        String report = reportService.generate("available");

        assertThat(report).isEqualTo("Available books: 5");
    }

    @Test
    @DisplayName("generate: throws when report type unsupported")
    void generateThrowsForUnsupportedReport() {
        assertThatThrownBy(() -> reportService.generate("overdue"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid report type");
    }
}
