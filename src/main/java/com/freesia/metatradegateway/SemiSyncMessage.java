package com.freesia.metatradegateway;

import java.util.List;

public record SemiSyncMessage(Block block,
                              List<RawBlock> rawBlocks,
                              List<Trade> tradeList) {
}
