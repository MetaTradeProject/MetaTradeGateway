package com.freesia.metatradegateway.blockchain.model;

import java.util.List;

public record RawBlock(int proofLevel, List<Trade> blockBody) {
}
