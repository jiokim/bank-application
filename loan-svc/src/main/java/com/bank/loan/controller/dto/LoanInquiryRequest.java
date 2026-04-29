package com.bank.loan.controller.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class LoanInquiryRequest {

    @NotNull
    private Long custId;

    @NotEmpty
    private List<Long> pdIds;
}
