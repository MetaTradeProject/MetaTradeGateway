package com.freesia.metatradegateway.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.freesia.metatradegateway.blockchain.Block;

public record ProofMessage(Block block, String address) {
    @JsonIgnore
    public int getProof() {
        return block.getProof();
    }

}
