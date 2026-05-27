package com.bank.loan.core.domain.model;

import java.time.LocalDate;
import java.util.Objects;

public class LnAsmCreateSpec {

    private final String asmKndCd;
    private final String asmStsCd;
    private final Long custId;
    private final Long pdId;
    private final LocalDate asmDt;

    public LnAsmCreateSpec(String asmKndCd, String asmStsCd, Long custId, Long pdId, LocalDate asmDt) {
        this.asmKndCd = Objects.requireNonNull(asmKndCd);
        this.asmStsCd = Objects.requireNonNull(asmStsCd);
        this.custId = Objects.requireNonNull(custId);
        this.pdId = pdId;
        this.asmDt = Objects.requireNonNull(asmDt);
        validateProductRequired();
    }

    public static LnAsmCreateSpec group(Long custId, LocalDate asmDt) {
        return new LnAsmCreateSpec(LnAsm.KND_GROUP, LnAsm.STS_IN_REVIEW, custId, null, asmDt);
    }

    public static LnAsmCreateSpec loan(Long custId, Long pdId, LocalDate asmDt) {
        return new LnAsmCreateSpec(LnAsm.KND_LOAN, LnAsm.STS_IN_REVIEW, custId, pdId, asmDt);
    }

    public static LnAsmCreateSpec reAsm(Long custId, Long pdId, LocalDate asmDt) {
        return new LnAsmCreateSpec(LnAsm.KND_RE_ASM, LnAsm.STS_WAITING, custId, pdId, asmDt);
    }

    public String getAsmKndCd() { return asmKndCd; }
    public String getAsmStsCd() { return asmStsCd; }
    public Long getCustId() { return custId; }
    public Long getPdId() { return pdId; }
    public LocalDate getAsmDt() { return asmDt; }

    private void validateProductRequired() {
        if ((LnAsm.KND_LOAN.equals(asmKndCd) || LnAsm.KND_RE_ASM.equals(asmKndCd)) && pdId == null) {
            throw new IllegalArgumentException("상품코드는 필수입니다.");
        }
    }
}
