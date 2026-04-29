package com.bank.loan.service.query;

import com.bank.loan.core.domain.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanQueryService {

    private final LoanRepository loanRepository;
}
