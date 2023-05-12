package com.freesia.metatradegateway.message;

import java.util.List;

import com.freesia.metatradegateway.blockchain.Block;
import com.freesia.metatradegateway.blockchain.RawBlock;
import com.freesia.metatradegateway.blockchain.Trade;

public record SemiSyncMessage(Block block,
                              List<RawBlock> rawBlocks,
                              List<Trade> tradeList) {
}
