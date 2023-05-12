package com.freesia.metatradegateway.message;

import java.util.List;

import com.freesia.metatradegateway.blockchain.model.Block;
import com.freesia.metatradegateway.blockchain.model.RawBlock;
import com.freesia.metatradegateway.blockchain.model.Trade;

public record SemiSyncMessage(Block block,
                              List<RawBlock> rawBlocks,
                              List<Trade> tradeList) {
}
