package com.bank.deposit.core.repository;

import com.bank.arrangement.core.domain.enums.ArrSttsEnum;
import com.bank.deposit.core.domain.model.DpArr;
import com.bank.deposit.core.domain.model.DpArrCreateSpec;
import com.bank.deposit.core.domain.model.DpArrImpl;
import com.bank.deposit.core.domain.repository.DepositRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryDepositRepository implements DepositRepository {

    private final AtomicLong sequence = new AtomicLong(0);
    private final Map<Long, DpArr> deposits = new LinkedHashMap<>();

    @Override
    public synchronized DpArr save(DpArrCreateSpec spec) {
        Long arrId = sequence.incrementAndGet();
        DpArr dpArr = new DpArrImpl(
                arrId,
                spec.getCustId(),
                spec.getPdId(),
                spec.getDpAmt(),
                spec.getIntrRt(),
                ArrSttsEnum.ACTIVE,
                spec.getArrStrtDt(),
                spec.getArrEndDt(),
                spec.getMaturityDt()
        );
        deposits.put(arrId, dpArr);
        return dpArr;
    }

    @Override
    public synchronized Optional<DpArr> findById(Long arrId) {
        return Optional.ofNullable(deposits.get(arrId));
    }

    @Override
    public synchronized List<DpArr> findAll() {
        return new ArrayList<>(deposits.values());
    }
}
