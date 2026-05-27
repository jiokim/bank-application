package com.bank.loan.service.application.verification;

import com.bank.cust.core.domain.model.Cust;
import com.bank.cust.core.domain.service.CustMngr;
import com.bank.loan.client.realname.RealNameVerificationClient;
import com.bank.loan.client.realname.RealNameVerificationResult;
import com.bank.loan.service.application.verification.dto.LoanRealNameVerifyCommand;
import com.bank.loan.service.application.verification.dto.LoanRealNameVerifyInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LoanCustVerifyService {

    private final RealNameVerificationClient realNameVerificationClient;
    private final CustMngr custMngr;

    public LoanRealNameVerifyInfo verifyRealName(LoanRealNameVerifyCommand command) {
        validate(command);

        RealNameVerificationResult verificationResult =
                realNameVerificationClient.verify(command.custNm(), command.rnmNbr());
        
        if (!verificationResult.verified()) {
            return LoanRealNameVerifyInfo.failure(verificationResult.message());
        }

        Cust cust = custMngr.findCustByRnmNbr(command.rnmNbr())
                .orElseGet(() -> custMngr.create(command.custNm(), command.rnmNbr()));

        return LoanRealNameVerifyInfo.success(cust.getCustId());
    }

    private void validate(LoanRealNameVerifyCommand command) {
        if (!StringUtils.hasText(command.custNm())) throw new IllegalArgumentException("고객명은 필수입니다.");
        if (!StringUtils.hasText(command.rnmNbr())) throw new IllegalArgumentException("실명번호는 필수입니다.");
    }

}
