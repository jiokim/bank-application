package com.bank.loan.client.phone;

public interface PhoneVerificationClient {

    PhoneVerificationResult send(String phoneNo, String custNm, String rnmNbr);
}