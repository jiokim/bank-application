package com.bank.loan.client.realname;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class InMemoryRealNameVerificationClient implements RealNameVerificationClient {

    @Override
    public RealNameVerificationResult verify(String custNm, String rnmNbr) {
        if (!StringUtils.hasText(custNm) || !StringUtils.hasText(rnmNbr)) {
            return RealNameVerificationResult.failure("실명인증 정보가 올바르지 않습니다.");
        }
        if ("FAIL".equalsIgnoreCase(rnmNbr)) {
            return RealNameVerificationResult.failure("실명인증 실패");
        }
        return RealNameVerificationResult.success();
    }
}
