package com.bank.arrangement.core.domain.service;

import com.bank.arrangement.core.domain.model.Arr;

public interface ArrMngr<T extends Arr> {

    boolean hasActiveArr(Long custId, Long pdId);
}
