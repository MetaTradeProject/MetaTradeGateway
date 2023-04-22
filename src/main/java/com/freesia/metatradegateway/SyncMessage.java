package com.freesia.metatradegateway;

import java.util.List;

public record SyncMessage(List<Block> chain,
                          List<RawBlock> rawBlocks,
                          List<Trade> tradeList) {
}
