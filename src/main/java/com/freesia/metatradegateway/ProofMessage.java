package com.freesia.metatradegateway;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record ProofMessage(Block block, String address) {
    @JsonIgnore
    public int getProof() {
        return block.getProof();
    }

}
