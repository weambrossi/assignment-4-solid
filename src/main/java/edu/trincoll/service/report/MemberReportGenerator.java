package edu.trincoll.service.report;

import edu.trincoll.service.member.MemberService;
import org.springframework.stereotype.Component;

@Component
public class MemberReportGenerator implements ReportGenerator {

    private final MemberService memberService;

    public MemberReportGenerator(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public boolean supports(String reportType) {
        return "members".equalsIgnoreCase(reportType);
    }

    @Override
    public String generate() {
        long totalMembers = memberService.countMembers();
        return "Total members: " + totalMembers;
    }
}
