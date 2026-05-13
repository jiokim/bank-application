package com.bank.loan.service.application.assessment.strategy;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class LnAssessmentStrategyFinder {

    private final Map<Long, LnAssessmentStrategy> strategies;

    public LnAssessmentStrategyFinder(List<LnAssessmentStrategy> strategies) {
        this.strategies = strategies.stream()
                .collect(Collectors.toMap(LnAssessmentStrategy::supportedPdId, Function.identity()));
    }

    public LnAssessmentStrategy get(Long pdId) {
        LnAssessmentStrategy strategy = strategies.get(pdId);
        if (strategy == null) {
            throw new IllegalArgumentException("지원하지 않는 상품입니다: " + pdId);
        }
        return strategy;
    }
}
