package com.freesia.metatradegateway;

public record ProofMessage(Block block, String address) {
    public int getProof() {
        return block.getProof();
    }

}
