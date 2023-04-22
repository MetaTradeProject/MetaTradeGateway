package com.freesia.metatradegateway;

import java.util.List;

public record RawBlock(int proofLevel, List<Trade> blockBody) {
}
