package com.freesia.metatradegateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class BlockChain implements BlockChainService{
    private final MetaTradeGatewayProperties properties;

    private String adminAddress = "0000000000000000";
    private String broadcastAddress = "FFFFFFFFFFFFFFFF";
    private double initCoins = 100;
    private double fixedMineReward = 5.00;
    private String initHash = "1";
    private int proofLevel = 4;
    private int genesisProofLevel = 1;
    private long spawnSecond = 600;

    public enum Status {
        NULL, GENESIS, MINING, FINISHED
    }

    private Status status = Status.NULL;

    private final List<Block> chain = new CopyOnWriteArrayList<>();
    private final Deque<RawBlock> rawBlockDeque = new LinkedBlockingDeque<>();
    private final List<Trade> tradeList = new CopyOnWriteArrayList<>();

    private final Map<Integer, String> proofMap = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> agreeMap = new ConcurrentHashMap<>();
    private final Map<Integer, Block> blockMap = new ConcurrentHashMap<>();
    private final AtomicBoolean isFoundFlag = new AtomicBoolean(false);

    @Autowired
    public BlockChain(MetaTradeGatewayProperties properties){
        this.properties = properties;
    }

    private Block CreateGenesisBlock(){
        Block block = new Block(initHash, genesisProofLevel);
        block.getBlockBody().add(
                new Trade(adminAddress, broadcastAddress, initCoins, 0, System.currentTimeMillis()));
        return block;
    }

    private void MiningBlock(Block block){
        String rawData = block.getMineData();

        log.info(String.format("BlockChain Genesis Block Mining: Staring with %s", rawData));
        int proof = 0;
        String targetPrefix = "0".repeat(block.getProofLevel());
        while (true){
            String guess = rawData + proof;
            String result = CryptoUtils.getSHA256(CryptoUtils.getSHA256(guess));
            if(result.startsWith(targetPrefix)){
                break;
            }
            else {
                proof++;
            }
        }
        log.info(String.format("BlockChain Genesis Block Mining: Proof %d", proof));
        block.setProof(proof);
    }

    private void ReadConfig(){
        log.info("Use Config: ");
        adminAddress = properties.getAdminAddress();
        log.info(String.format("AdminAddress: %s", adminAddress));
        broadcastAddress = properties.getBroadcastAddress();
        log.info(String.format("BroadcastAddress: %s", broadcastAddress));
        initCoins = properties.getInitCoins();
        log.info(String.format("InitCoins: %f", initCoins));
        fixedMineReward = properties.getFixedMineReward();
        log.info(String.format("FixedMineReward: %f", fixedMineReward));
        initHash = properties.getInitHash();
        log.info(String.format("InitHash: %s", initHash));
        proofLevel = properties.getProofLevel();
        log.info(String.format("ProofLevel: %d", proofLevel));
        genesisProofLevel = properties.getGenesisProofLevel();
        log.info(String.format("GenesisProofLevel: %d", genesisProofLevel));
        spawnSecond = properties.getSpawnSecond();
        log.info(String.format("SpawnSecond: %d", spawnSecond));
    }

    @Override
    public void Init(){
        ReadConfig();
        status = Status.GENESIS;
        log.info("BlockChain Status: GENESIS");
        Block block = CreateGenesisBlock();
        status = Status.MINING;
        log.info("BlockChain Status: MINING");
        MiningBlock(block);
        chain.add(block);
        status = Status.FINISHED;
        log.info("BlockChain Status: FINISHED");
    }

    @Override
    public void insertTrade(Trade trade){
        tradeList.add(trade);
    }

    @Override
    public int spawnRawBlock(){
        if(status != Status.FINISHED || tradeList.size() == 0){
            return -1;
        }
        rawBlockDeque.push(new RawBlock(proofLevel, tradeList));
        this.tradeList.clear();
        return proofLevel;
    }

    @Override
    public List<Block> getChain(){
        return this.chain;
    }

    @Override
    public List<RawBlock> getRawBlockList(){
        return this.rawBlockDeque.stream().toList();
    }

    @Override
    public List<Trade> getTradeList(){
        return this.tradeList;
    }


    private void RewardMiner(Block block, String address){
        Trade trade = new Trade(adminAddress, address,
                block.getBlockCommission() + fixedMineReward, 0, System.currentTimeMillis());
        rawBlockDeque.getFirst().blockBody().add(0, trade);
        log.info(String.format("BlockChain Rewarding Miner: %s %f", trade.receiverAddress(), trade.amount()));
    }


    private void addBlock(Block block){
        chain.add(block);
        rawBlockDeque.removeFirst();

        log.info(String.format("BlockChain Add Block: PrevHash - %s MerkleHash - %s", block.getPrevHash(), block.getMerkleHash()));

        RewardMiner(block, proofMap.get(block.getProof()));

        proofMap.clear();
        agreeMap.clear();
        blockMap.clear();
        isFoundFlag.set(false);
    }

    @Override
    public Block getLastBlock(){
        return this.chain.get(this.chain.size()-1);
    }

    @Override
    public void addProof(ProofMessage message){
        if(!proofMap.containsKey(message.getProof())){
            proofMap.put(message.getProof(), message.address());
            agreeMap.put(message.getProof(), 0);
            blockMap.put(message.getProof(), message.block());
        }
    }

    @Override
    public boolean addAgree(int proof, long counter){
        if(!isFoundFlag.get()){
            agreeMap.put(proof, agreeMap.get(proof)+1);
            if(agreeMap.get(proof) * 2L >= counter){
                isFoundFlag.set(true);
                addBlock(blockMap.get(proof));
                return true;
            }
        }
        return false;
    }

    @Override
    public long getSpawnSecond(){
        return spawnSecond;
    }

}
