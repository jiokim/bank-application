package com.bank.loan.client.kcb;

public class KcbCreditInfo {

    private final Long custId;
    private final int creditScore;
    private final int delinquencyCount;

    public KcbCreditInfo(Long custId, int creditScore, int delinquencyCount) {
        this.custId = custId;
        this.creditScore = creditScore;
        this.delinquencyCount = delinquencyCount;
    }

    public Long getCustId() { return custId; }
    public int getCreditScore() { return creditScore; }
    public int getDelinquencyCount() { return delinquencyCount; }
}
