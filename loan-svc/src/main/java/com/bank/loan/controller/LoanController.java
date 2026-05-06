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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "대출", description = "한도조회 및 대출 실행 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/loans")
public class LoanController {

    private final LoanApplicationService loanApplicationService;
    private final LoanExecutionService loanExecutionService;

    @Operation(
        summary = "멀티상품 한도조회",
        description = "최대 3개 상품의 가능 한도와 금리를 병렬로 조회합니다. 반환된 inquiryId는 대출 실행(apply) 시 사용됩니다."
    )
    @PostMapping("/inquiry")
    public LoanInquiryResponse inquiry(@Valid @RequestBody LoanInquiryRequest request) {
        LoanInquiryInfo info = loanApplicationService.inquiry(
                new LoanInquiryCommand(request.getCustId(), request.getPdIds()));
        return toResponse(info);
    }

    @Operation(
        summary = "대출 실행",
        description = "한도조회 결과(inquiryId)를 기반으로 선택한 상품의 대출 계약을 생성합니다. inquiryId는 반드시 inquiry API를 먼저 호출해 발급받아야 합니다."
    )
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
