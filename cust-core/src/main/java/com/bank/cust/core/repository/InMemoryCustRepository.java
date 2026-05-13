package com.bank.cust.core.repository;

import com.bank.cust.core.domain.model.Cust;
import com.bank.cust.core.domain.model.CustImpl;
import com.bank.cust.core.domain.repository.CustRepository;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryCustRepository implements CustRepository {

    private final Map<Long, Cust> store = new ConcurrentHashMap<>(Map.of(
            1L, new CustImpl(1L, 35),
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
    public Optional<Cust> findById(Long custId) {
        return Optional.ofNullable(store.get(custId));
    }
}
