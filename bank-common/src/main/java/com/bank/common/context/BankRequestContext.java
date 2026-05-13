package com.bank.common.context;

import java.time.LocalDate;

public record BankRequestContext(
        String staffId,
        String channelCode,
        LocalDate txDate
) {}
