package com.bank.deposit.core.domain.model;

import com.bank.productapi.model.Pd;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class DpArrCreateSpec {

    private final Long custId;
    private final Long pdId;
    private final BigDecimal dpAmt;
    private final BigDecimal intrRt;
    private final LocalDate arrStrtDt;
    private final LocalDate arrEndDt;
    private final LocalDate maturityDt;

    public DpArrCreateSpec(Long custId, Long pdId, BigDecimal dpAmt, BigDecimal intrRt,
                           LocalDate arrStrtDt, LocalDate arrEndDt, LocalDate maturityDt) {
        this.custId = Objects.requireNonNull(custId);
        this.pdId = Objects.requireNonNull(pdId);
        this.dpAmt = Objects.requireNonNull(dpAmt);
        this.intrRt = Objects.requireNonNull(intrRt);
        this.arrStrtDt = Objects.requireNonNull(arrStrtDt);
        this.arrEndDt = Objects.requireNonNull(arrEndDt);
        this.maturityDt = Objects.requireNonNull(maturityDt);
    }

    public Long getCustId() { return custId; }
    public Long getPdId() { return pdId; }
    public BigDecimal getDpAmt() { return dpAmt; }
    public BigDecimal getIntrRt() { return intrRt; }
    public LocalDate getArrStrtDt() { return arrStrtDt; }
    public LocalDate getArrEndDt() { return arrEndDt; }
    public LocalDate getMaturityDt() { return maturityDt; }

    public static DpArrCreateSpec fromProduct(Long custId, Pd product, BigDecimal dpAmt,
                                              LocalDate arrStrtDt, LocalDate arrEndDt, LocalDate maturityDt) {
        Objects.requireNonNull(product);
        return new DpArrCreateSpec(
                custId,
                product.getPdId(),
                dpAmt,
                product.getInterestRate(),
                arrStrtDt,
                arrEndDt,
                maturityDt
        );
    }
}
