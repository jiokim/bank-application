package com.bank.loanapi.model;

import com.bank.loanapi.enums.ArrSttsEnum;
import com.bank.loanapi.enums.ArrTpEnum;

import java.time.LocalDate;

public interface Arr {

    Long getArrId();

    ArrTpEnum getArrTpCd();

    Long getCustId();

    ArrSttsEnum getArrSttsCd();

    LocalDate getArrStrtDt();

    LocalDate getArrEndDt();

    String getPdId();
}