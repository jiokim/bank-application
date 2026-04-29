package com.bank.loan.core.domain.model;

import com.bank.loanapi.enums.ArrSttsEnum;
import com.bank.loanapi.enums.ArrTpEnum;
import com.bank.loanapi.model.Arr;

import java.time.LocalDate;
import java.util.Objects;

public abstract class ArrImpl implements Arr {

    private final Long arrId;
    private final ArrTpEnum arrTpCd;
    private final Long custId;
    private final ArrSttsEnum arrSttsCd;
    private final LocalDate arrStrtDt;
    private final LocalDate arrEndDt;
    private final String pdId;

    protected ArrImpl(Long arrId, ArrTpEnum arrTpCd, Long custId,
                      ArrSttsEnum arrSttsCd, LocalDate arrStrtDt, LocalDate arrEndDt, String pdId) {
        this.arrId = Objects.requireNonNull(arrId);
        this.arrTpCd = Objects.requireNonNull(arrTpCd);
        this.custId = Objects.requireNonNull(custId);
        this.arrSttsCd = Objects.requireNonNull(arrSttsCd);
        this.arrStrtDt = Objects.requireNonNull(arrStrtDt);
        this.arrEndDt = Objects.requireNonNull(arrEndDt);
        this.pdId = Objects.requireNonNull(pdId);
    }

    @Override public Long getArrId() { return arrId; }
    @Override public ArrTpEnum getArrTpCd() { return arrTpCd; }
    @Override public Long getCustId() { return custId; }
    @Override public ArrSttsEnum getArrSttsCd() { return arrSttsCd; }
    @Override public LocalDate getArrStrtDt() { return arrStrtDt; }
    @Override public LocalDate getArrEndDt() { return arrEndDt; }
    @Override public String getPdId() { return pdId; }
}