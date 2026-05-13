package com.bank.loan.service.application.eligibility;

import com.bank.cust.core.domain.model.Cust;
import com.bank.cust.core.domain.repository.CustRepository;
import com.bank.loan.core.domain.service.LnArrMngr;
import com.bank.loan.service.application.eligibility.dto.LoanEligibilityCommand;
import com.bank.loan.service.application.eligibility.dto.LoanEligibilityInfo;
import com.bank.productapi.model.LnPd;
import com.bank.productapi.model.PdMngr;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LnEligibilityService {

    private final CustRepository custRepository;
    private final LnArrMngr lnArrMngr;
    private final PdMngr<LnPd> lnPdMngr;

    public LoanEligibilityInfo checkEligibility(LoanEligibilityCommand command) {
        validate(command);

        Cust cust = custRepository.findById(command.custId()).orElse(null);
        if (cust == null) {
            return new LoanEligibilityInfo(true, null, "신청 가능한 신규 고객 입니다.");
        }

        if (lnArrMngr.hasInProgressArr(command.custId())) {
            return new LoanEligibilityInfo(false, null, "진행중인 신청건이 있습니다. 실행완료 또는 취소 후 신청이 가능합니다.");
        }
        if (lnArrMngr.hasActiveArr(command.custId(), command.pdId())) {
            return new LoanEligibilityInfo(false, null, "대출잔액이 존재합니다.");
        }

        LnPd pd = lnPdMngr.getPd(command.pdId());
        if (!cust.isAgeEligible(pd.getMinAge(), pd.getMaxAge())) {
            return new LoanEligibilityInfo(false, null, "상품에 가입 가능한 연령이 아닙니다.");
        }

        return new LoanEligibilityInfo(true, null, null);
    }

    private void validate(LoanEligibilityCommand command) {
        if (command.custId() == null) throw new IllegalArgumentException("고객식별자는 필수입니다.");
        if (command.pdId() == null) throw new IllegalArgumentException("상품코드는 필수입니다.");
        if (!StringUtils.hasText(command.lnRcvChnlCd())) throw new IllegalArgumentException("대출접수경로코드는 필수입니다.");
    }
}
