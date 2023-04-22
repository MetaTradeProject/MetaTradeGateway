package com.freesia.metatradegateway;

import org.springframework.stereotype.Service;

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
