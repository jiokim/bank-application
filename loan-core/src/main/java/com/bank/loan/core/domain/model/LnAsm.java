package com.bank.loan.core.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface LnAsm {

    String KND_GROUP = "GROUP";
    String KND_LOAN = "LOAN";
    String KND_RE_ASM = "RE_ASM";

    String STS_WAITING = "WAITING";
    String STS_IN_REVIEW = "IN_REVIEW";
    String STS_APPROVED = "APPROVED";
    String STS_REJECTED = "REJECTED";
    String STS_COMPLETED = "COMPLETED";
    String STS_EXPIRED = "EXPIRED";

    Long getAsmId();

    String getAsmKndCd();

    String getAsmStsCd();

    Long getCustId();

    Long getPdId();

    LocalDate getAsmDt();

    BigDecimal getMaxLoanAmt();

    BigDecimal getIntrRt();

    String getRjctRsn();

    default boolean isGroupAsm() {
        return KND_GROUP.equals(getAsmKndCd());
    }

    default boolean isLoanAsm() {
        return KND_LOAN.equals(getAsmKndCd());
    }

    default boolean isApproved() {
        return STS_APPROVED.equals(getAsmStsCd());
    }
}
