package com.bank.arrangementapi.model;

import com.bank.arrangementapi.enums.ArrSttsEnum;
import com.bank.arrangementapi.enums.ArrTpEnum;

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
