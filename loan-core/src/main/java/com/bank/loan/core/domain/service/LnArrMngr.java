package com.bank.loan.core.domain.service;

import com.bank.arrangement.core.domain.service.ArrMngr;
import com.bank.loan.core.domain.model.LnArr;

public interface LnArrMngr extends ArrMngr<LnArr> {

    boolean hasInProgressArr(Long custId);

    boolean hasActiveArr(Long custId, Long pdId);
}
