package com.bank.loan.core.repository;

import com.bank.loan.core.domain.model.LnArrCreateSpec;
import com.bank.loan.core.domain.model.LnArrImpl;
import com.bank.loan.core.domain.repository.LoanRepository;
import com.bank.loan.core.domain.enums.ArrSttsEnum;
import com.bank.loan.core.domain.model.LnArr;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryLoanRepository implements LoanRepository {

    private final AtomicLong sequence = new AtomicLong(0);
    private final Map<Long, LnArr> loans = new LinkedHashMap<>();

    @Override
    public synchronized LnArr save(LnArrCreateSpec spec) {
        Long arrId = sequence.incrementAndGet();
        LnArr lnArr = new LnArrImpl(
                arrId,
                spec.getCustId(),
                spec.getPdId(),
                spec.getLnAmt(),
                spec.getIntrRt(),
                ArrSttsEnum.ACTIVE,
                spec.getArrStrtDt(),
                spec.getArrEndDt()
        );
        loans.put(arrId, lnArr);
        return lnArr;
    }

    @Override
    public synchronized Optional<LnArr> findById(Long arrId) {
        return Optional.ofNullable(loans.get(arrId));
    }

    @Override
    public synchronized List<LnArr> findAll() {
        return new ArrayList<>(loans.values());
    }
}
