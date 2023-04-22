package com.freesia.metatradegateway;

public class AgreeMessage {
    String address;
    int proof;

    public AgreeMessage(String address, int proof) {
        this.address = address;
        this.proof = proof;
    }

    public int getProof() {
        return proof;
    }
}
