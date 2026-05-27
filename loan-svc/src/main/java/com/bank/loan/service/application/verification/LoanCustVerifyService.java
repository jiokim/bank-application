package com.bank.loan.service.application.verification;

import com.bank.cust.core.domain.model.Cust;
import com.bank.cust.core.domain.service.CustMngr;
import com.bank.loan.client.phone.PhoneVerificationClient;
import com.bank.loan.client.phone.PhoneVerificationResult;
import com.bank.loan.client.phone.PhoneVerifyResult;
import com.bank.loan.client.realname.RealNameVerificationClient;
import com.bank.loan.client.realname.RealNameVerificationResult;
import com.bank.loan.service.application.verification.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LoanCustVerifyService {

    private final RealNameVerificationClient realNameVerificationClient;
    private final PhoneVerificationClient phoneVerificationClient;
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

    public LoanPhoneSendInfo sendPhone(LoanPhoneSendCommand command) {
        validate(command);

        Cust cust = custMngr.findCustById(command.custId());
        if (cust == null) return LoanPhoneSendInfo.failure("미가입 고객입니다.");

        PhoneVerificationResult result = phoneVerificationClient.send(command.phoneNo(), cust.getCustNm(), cust.getRnmNbr());

        if (!result.sent()) {
            return LoanPhoneSendInfo.failure(result.message());
        }

        return LoanPhoneSendInfo.success(result.verifyToken(), command.phoneNo());
    }

    public LoanPhoneVerifyInfo verifyPhone(LoanPhoneVerifyCommand command) {
        validate(command);

        Cust cust = custMngr.findCustById(command.custId());
        if (cust == null) return LoanPhoneVerifyInfo.failure("미가입 고객입니다.");

        PhoneVerifyResult result = phoneVerificationClient.verify(
                command.verifyToken(), command.verifyCode(), command.phoneNo(), cust.getCustNm(), cust.getRnmNbr());

        if (!result.verified()) {
            return LoanPhoneVerifyInfo.failure(result.message());
        }

        custMngr.savePhoneNo(command.custId(), command.phoneNo());
        custMngr.saveTermsAgreement(command.custId(), command.agreedTermIds());

        return LoanPhoneVerifyInfo.success();
    }

    private void validate(LoanRealNameVerifyCommand command) {
        if (!StringUtils.hasText(command.custNm())) throw new IllegalArgumentException("고객명은 필수입니다.");
        if (!StringUtils.hasText(command.rnmNbr())) throw new IllegalArgumentException("실명번호는 필수입니다.");
    }

    private void validate(LoanPhoneSendCommand command) {
        if (command.custId() == null) throw new IllegalArgumentException("고객 ID는 필수입니다.");
        if (!StringUtils.hasText(command.phoneNo())) throw new IllegalArgumentException("휴대폰 번호는 필수입니다.");
    }

    private void validate(LoanPhoneVerifyCommand command) {
        if (command.custId() == null) throw new IllegalArgumentException("고객 ID는 필수입니다.");
        if (!StringUtils.hasText(command.phoneNo())) throw new IllegalArgumentException("휴대폰 번호는 필수입니다.");
        if (!StringUtils.hasText(command.verifyToken())) throw new IllegalArgumentException("인증 토큰은 필수입니다.");
        if (!StringUtils.hasText(command.verifyCode())) throw new IllegalArgumentException("인증번호는 필수입니다.");
    }

}
