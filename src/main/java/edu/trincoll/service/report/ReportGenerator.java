package edu.trincoll.service.report;

public interface ReportGenerator {

    boolean supports(String reportType);

    String generate();
}
