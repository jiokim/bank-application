package com.bank.loan.client.realname;

public interface RealNameVerificationClient {
    RealNameVerificationResult verify(String custNm, String rnmNbr);
}
