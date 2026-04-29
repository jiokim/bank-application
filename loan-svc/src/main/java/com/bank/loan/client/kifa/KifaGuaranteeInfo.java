package com.bank.loan.client.kifa;

import java.math.BigDecimal;

public class KifaGuaranteeInfo {

    private final Long custId;
    private final BigDecimal guaranteeLimit;
    private final BigDecimal guaranteeRate;

    public KifaGuaranteeInfo(Long custId, BigDecimal guaranteeLimit, BigDecimal guaranteeRate) {
        this.custId = custId;
        this.guaranteeLimit = guaranteeLimit;
        this.guaranteeRate = guaranteeRate;
    }

    public Long getCustId() { return custId; }
    public BigDecimal getGuaranteeLimit() { return guaranteeLimit; }
    public BigDecimal getGuaranteeRate() { return guaranteeRate; }
}
