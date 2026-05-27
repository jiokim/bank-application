package com.bank.loan.client.phone;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class InMemoryPhoneVerificationClient implements PhoneVerificationClient {

    @Override
    public PhoneVerificationResult send(String phoneNo, String custNm, String rnmNbr) {
        return PhoneVerificationResult.success(UUID.randomUUID().toString());
    }
}