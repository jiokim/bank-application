package com.bank.cust.core.repository;

import com.bank.cust.core.domain.model.Cust;
import com.bank.cust.core.domain.model.CustImpl;
import com.bank.cust.core.domain.repository.CustRepository;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryCustRepository implements CustRepository {

    private final AtomicLong sequence = new AtomicLong(1000L);
    private final Map<Long, Cust> store = new ConcurrentHashMap<>(Map.of(
            1L, new CustImpl(1L, 35, "홍길동", "9001011234567"),
            2L, new CustImpl(2L, 17),
            3L, new CustImpl(3L, 72)
    ));

    @Override
    public Cust save(Long custId, int age) {
        Cust cust = new CustImpl(custId, age);
        store.put(cust.getCustId(), cust);
        return cust;
    }

    @Override
    public Cust saveBrief(Long custId, String custNm, String rnmNbr) {
        Cust cust = new CustImpl(custId, 0, custNm, rnmNbr);
        store.put(cust.getCustId(), cust);
        return cust;
    }

    @Override
    public Optional<Cust> findById(Long custId) {
        return Optional.ofNullable(store.get(custId));
    }

    @Override
    public Optional<Cust> findByRnmNbr(String rnmNbr) {
        return store.values().stream()
                .filter(cust -> Objects.equals(rnmNbr, cust.getRnmNbr()))
                .findFirst();
    }

    @Override
    public Cust savePhoneNo(Long custId, String phoneNo) {
        Cust existing = store.get(custId);
        Cust updated = new CustImpl(existing.getCustId(), existing.getAge(), existing.getCustNm(), existing.getRnmNbr(), phoneNo);
        store.put(custId, updated);
        return updated;
    }

    @Override
    public Long nextId() {
        return sequence.incrementAndGet();
    }
}
