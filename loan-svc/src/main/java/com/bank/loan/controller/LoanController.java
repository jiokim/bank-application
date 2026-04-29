package com.bank.loan.controller;

import com.bank.loan.controller.dto.LoanApplyRequest;
import com.bank.loan.controller.dto.LoanApplyResponse;
import com.bank.loan.controller.dto.LoanInquiryRequest;
import com.bank.loan.controller.dto.LoanInquiryResponse;
import com.bank.loan.controller.dto.LoanInquiryResultItem;
import com.bank.loan.service.application.LoanApplicationService;
import com.bank.loan.service.dto.LoanApplyCommand;
import com.bank.loan.service.dto.LoanApplyInfo;
import com.bank.loan.service.dto.LoanInquiryCommand;
import com.bank.loan.service.dto.LoanInquiryInfo;
import com.bank.loan.service.execution.LoanExecutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/loans")
public class LoanController {

    private final LoanApplicationService loanApplicationService;
    private final LoanExecutionService loanExecutionService;

    @PostMapping("/inquiry")
    public LoanInquiryResponse inquiry(@Valid @RequestBody LoanInquiryRequest request) {
        LoanInquiryInfo info = loanApplicationService.inquiry(
                new LoanInquiryCommand(request.getCustId(), request.getPdIds()));
        return toResponse(info);
    }

    @PostMapping
    public LoanApplyResponse apply(@Valid @RequestBody LoanApplyRequest request) {
        LoanApplyInfo info = loanExecutionService.apply(
                new LoanApplyCommand(request.getInquiryId(), request.getPdId(), request.getLnAmt()));
        return toResponse(info);
    }

    private LoanInquiryResponse toResponse(LoanInquiryInfo info) {
        List<LoanInquiryResultItem> items = info.getResults().stream()
                .map(r -> new LoanInquiryResultItem(r.getPdId(), r.getMaxLoanAmt(), r.getIntrRt()))
                .toList();
        return new LoanInquiryResponse(info.getInquiryId(), info.getCustId(), info.getInquiryDt(), items);
    }

    private LoanApplyResponse toResponse(LoanApplyInfo info) {
        return new LoanApplyResponse(
                info.getArrId(), info.getCustId(), info.getPdId(),
                info.getLnAmt(), info.getIntrRt(),
                info.getArrSttsCd(), info.getArrStrtDt(), info.getArrEndDt());
    }
}
