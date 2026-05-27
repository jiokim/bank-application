package com.bank.loan.core.repository;

import com.bank.loan.core.domain.model.LnAsm;
import com.bank.loan.core.domain.model.LnAsmCreateSpec;
import com.bank.loan.core.domain.model.LnAsmImpl;
import com.bank.loan.core.domain.repository.LnAsmRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryLnAsmRepository implements LnAsmRepository {

    private final AtomicLong sequence = new AtomicLong(0);
    private final Map<Long, LnAsm> assessments = new LinkedHashMap<>();

    @Override
    public Long nextId() {
        return sequence.incrementAndGet();
    }

    @Override
    public synchronized LnAsm save(Long asmId, LnAsmCreateSpec spec) {
        LnAsm lnAsm = new LnAsmImpl(
                asmId,
                spec.getAsmKndCd(),
                spec.getAsmStsCd(),
                spec.getCustId(),
                spec.getPdId(),
                spec.getAsmDt(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null
        );
        assessments.put(asmId, lnAsm);
        return lnAsm;
    }

    @Override
    public synchronized LnAsm updateStatus(Long asmId, String asmStsCd) {
        LnAsm existing = getExisting(asmId);
        LnAsm updated = copy(existing, asmStsCd, existing.getMaxLoanAmt(), existing.getIntrRt(), existing.getRjctRsn());
        assessments.put(asmId, updated);
        return updated;
    }

    @Override
    public synchronized LnAsm approve(Long asmId, BigDecimal maxLoanAmt, BigDecimal intrRt) {
        LnAsm existing = getExisting(asmId);
        LnAsm approved = copy(existing, LnAsm.STS_APPROVED, maxLoanAmt, intrRt, null);
        assessments.put(asmId, approved);
        return approved;
    }

    @Override
    public synchronized LnAsm reject(Long asmId, String rjctRsn) {
        LnAsm existing = getExisting(asmId);
        LnAsm rejected = copy(existing, LnAsm.STS_REJECTED, BigDecimal.ZERO, BigDecimal.ZERO, rjctRsn);
        assessments.put(asmId, rejected);
        return rejected;
    }

    @Override
    public synchronized Optional<LnAsm> findById(Long asmId) {
        return Optional.ofNullable(assessments.get(asmId));
    }

    @Override
    public synchronized List<LnAsm> findAll() {
        return new ArrayList<>(assessments.values());
    }

    private LnAsm getExisting(Long asmId) {
        LnAsm existing = assessments.get(asmId);
        if (existing == null) throw new IllegalArgumentException("존재하지 않는 심사건: " + asmId);
        return existing;
    }

    private LnAsm copy(LnAsm existing, String asmStsCd, BigDecimal maxLoanAmt, BigDecimal intrRt, String rjctRsn) {
        return new LnAsmImpl(
                existing.getAsmId(),
                existing.getAsmKndCd(),
                asmStsCd,
                existing.getCustId(),
                existing.getPdId(),
                existing.getAsmDt(),
                maxLoanAmt,
                intrRt,
                rjctRsn
        );
    }
}
