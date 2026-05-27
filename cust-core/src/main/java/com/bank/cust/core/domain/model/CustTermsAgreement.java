package com.bank.cust.core.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public record CustTermsAgreement(
        Long agreementId,
        Long custId,
        List<Long> agreedTermIds,
        LocalDateTime agreedAt
) {}