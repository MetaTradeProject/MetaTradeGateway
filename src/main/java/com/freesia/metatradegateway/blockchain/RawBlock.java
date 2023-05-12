package com.freesia.metatradegateway.blockchain;

import java.util.List;

public record RawBlock(int proofLevel, List<Trade> blockBody) {
}
