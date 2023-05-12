package com.freesia.metatradegateway.blockchain;

import org.springframework.stereotype.Service;

import com.freesia.metatradegateway.blockchain.model.Block;
import com.freesia.metatradegateway.blockchain.model.RawBlock;
import com.freesia.metatradegateway.blockchain.model.Trade;
import com.freesia.metatradegateway.message.ProofMessage;

import java.util.List;

@Service
public interface BlockChainService {
    void Init();
    List<Block> getChain();
    List<RawBlock> getRawBlockList();
    List<Trade> getTradeList();
    void insertTrade(Trade trade);
    void addProof(ProofMessage message);
    int spawnRawBlock();
    Block getLastBlock();
    boolean addAgree(int proof, long counter);
    long getSpawnSecond();

}
