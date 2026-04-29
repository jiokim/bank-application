package com.bank.loan.client.nice;

public class NiceCreditInfo {

    private final Long custId;
    private final int creditScore;
    private final int delinquencyCount;

    public NiceCreditInfo(Long custId, int creditScore, int delinquencyCount) {
        this.custId = custId;
        this.creditScore = creditScore;
        this.delinquencyCount = delinquencyCount;
    }

    public Long getCustId() { return custId; }
    public int getCreditScore() { return creditScore; }
    public int getDelinquencyCount() { return delinquencyCount; }
}
