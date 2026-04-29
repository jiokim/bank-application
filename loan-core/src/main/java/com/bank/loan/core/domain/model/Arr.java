package com.bank.loan.core.domain.model;

import com.bank.loan.core.domain.enums.ArrSttsEnum;
import com.bank.loan.core.domain.enums.ArrTpEnum;

import java.time.LocalDate;

public interface Arr {

    Long getArrId();

    ArrTpEnum getArrTpCd();

    Long getCustId();

    ArrSttsEnum getArrSttsCd();

    LocalDate getArrStrtDt();

    LocalDate getArrEndDt();

    Long getPdId();
}
