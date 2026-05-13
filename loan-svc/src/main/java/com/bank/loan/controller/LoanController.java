package com.bank.loan.controller;

import com.bank.loan.controller.dto.*;
import com.bank.loan.service.application.assessment.LnAssessmentService;
import com.bank.loan.service.application.eligibility.LnEligibilityService;
import com.bank.loan.service.application.assessment.dto.LoanAssessmentCommand;
import com.bank.loan.service.application.assessment.dto.LoanAssessmentInfo;
import com.bank.loan.service.application.eligibility.dto.LoanEligibilityCommand;
import com.bank.loan.service.application.eligibility.dto.LoanEligibilityInfo;
import com.bank.loan.service.execution.LnExecutionService;
import com.bank.loan.service.execution.dto.LoanApplyCommand;
import com.bank.loan.service.execution.dto.LoanApplyInfo;
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

    private final LnAssessmentService assessmentService;
    private final LnEligibilityService eligibilityService;
    private final LnExecutionService executionService;

    @Operation(summary = "멀티상품 한도조회",
            description = "최대 3개 상품의 가능 한도와 금리를 병렬로 조회합니다. 반환된 inquiryId는 대출 실행(apply) 시 사용됩니다.")
    @PostMapping("/inquiry")
    public LoanInquiryResponse inquiry(@Valid @RequestBody LoanInquiryRequest request) {
        LoanAssessmentInfo info = assessmentService.assess(
                new LoanAssessmentCommand(request.getCustId(), request.getPdIds()));
        return toResponse(info);
    }

    @Operation(summary = "대출신청가능여부확인",
            description = "고객의 연령·부결이력·진행중인 신청·대출잔액 등을 검증하여 신청 가능 여부를 반환합니다.")
    @PostMapping("/eligibility")
    public LoanEligibilityResponse checkEligibility(@Valid @RequestBody LoanEligibilityRequest request) {
        LoanEligibilityInfo info = eligibilityService.checkEligibility(
                new LoanEligibilityCommand(request.custId(), request.pdId(), request.lnRcvChnlCd()));
        return new LoanEligibilityResponse(
                info.eligible() ? "Y" : "N",
                info.inquiryId(),
                info.eligible() ? "S" : "F",
                info.message());
    }

    @Operation(summary = "대출 실행",
            description = "한도조회 결과(inquiryId)를 기반으로 선택한 상품의 대출 계약을 생성합니다.")
    @PostMapping
    public LoanApplyResponse apply(@Valid @RequestBody LoanApplyRequest request) {
        LoanApplyInfo info = executionService.apply(
                new LoanApplyCommand(request.getInquiryId(), request.getPdId(), request.getLnAmt(), request.getCustRrn()));
        return toResponse(info);
    }

    private LoanInquiryResponse toResponse(LoanAssessmentInfo info) {
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
