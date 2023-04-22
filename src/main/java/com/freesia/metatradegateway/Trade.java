package com.freesia.metatradegateway;

public record Trade(String senderAddress, String receiverAddress, double amount, double commission,
                    long timestamp) {

    public String getHash() {
        return CryptoUtils.getSHA256(senderAddress + receiverAddress + amount + commission + timestamp);
    }
}
