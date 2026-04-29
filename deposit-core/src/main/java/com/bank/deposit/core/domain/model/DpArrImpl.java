package com.bank.deposit.core.domain.model;

import com.bank.arrangement.core.domain.enums.ArrSttsEnum;
import com.bank.arrangement.core.domain.enums.ArrTpEnum;
import com.bank.arrangement.core.domain.model.ArrImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class DpArrImpl extends ArrImpl implements DpArr {

    private final BigDecimal dpAmt;
    private final BigDecimal intrRt;
    private final LocalDate maturityDt;

    public DpArrImpl(Long arrId, Long custId, Long pdId,
                     BigDecimal dpAmt, BigDecimal intrRt,
                     ArrSttsEnum arrSttsCd, LocalDate arrStrtDt,
                     LocalDate arrEndDt, LocalDate maturityDt) {
        super(arrId, ArrTpEnum.DP, custId, arrSttsCd, arrStrtDt, arrEndDt, pdId);
        this.dpAmt = Objects.requireNonNull(dpAmt);
        this.intrRt = Objects.requireNonNull(intrRt);
        this.maturityDt = Objects.requireNonNull(maturityDt);
    }

    @Override public BigDecimal getDpAmt() { return dpAmt; }
    @Override public BigDecimal getIntrRt() { return intrRt; }
    @Override public LocalDate getMaturityDt() { return maturityDt; }
}
