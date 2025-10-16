package edu.trincoll.service.report;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {

    private final List<ReportGenerator> generators;

    public ReportService(List<ReportGenerator> generators) {
        this.generators = generators;
    }

    public String generate(String reportType) {
        return generators.stream()
                .filter(generator -> generator.supports(reportType))
                .findFirst()
                .map(ReportGenerator::generate)
                .orElseThrow(() -> new IllegalArgumentException("Invalid report type"));
    }
}
