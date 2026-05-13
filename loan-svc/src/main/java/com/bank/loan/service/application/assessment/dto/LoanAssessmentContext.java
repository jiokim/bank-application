package com.bank.loan.service.application.assessment.dto;

import com.bank.loan.client.kcb.KcbCreditInfo;
import com.bank.loan.client.nice.NiceCreditInfo;

public record LoanAssessmentContext(
        Long custId,
        NiceCreditInfo nice,
        KcbCreditInfo kcb
) {}
