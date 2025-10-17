package edu.trincoll.report;

import edu.trincoll.repository.MemberRepository;
import org.springframework.stereotype.Component;

@Component
public class MemberCountReportGenerator implements ReportGenerator {

    private final MemberRepository memberRepository;

    public MemberCountReportGenerator(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public String getReportName() {
        return "members";
    }

    @Override
    public String generateReport() {
        long totalMembers = memberRepository.count();
        return "Total members: " + totalMembers;
    }
}

