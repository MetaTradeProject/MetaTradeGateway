package com.freesia.metatradegateway;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record Trade(String senderAddress, String receiverAddress, double amount, double commission,
                    long timestamp) {

    @JsonIgnore
    public String getHash() {
        return CryptoUtils.getSHA256(senderAddress + receiverAddress + amount + commission + timestamp);
    }
}
