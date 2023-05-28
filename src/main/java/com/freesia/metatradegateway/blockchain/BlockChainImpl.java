package com.freesia.metatradegateway.blockchain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.freesia.metatradegateway.CryptoUtils;
import com.freesia.metatradegateway.MetaTradeGatewayProperties;
import com.freesia.metatradegateway.blockchain.model.Block;
import com.freesia.metatradegateway.blockchain.model.RawBlock;
import com.freesia.metatradegateway.blockchain.model.Trade;
import com.freesia.metatradegateway.jni.JniSigner;
import com.freesia.metatradegateway.message.ProofMessage;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
@Slf4j
public class BlockChainImpl implements BlockChainService{
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final MetaTradeGatewayProperties properties;

    private String adminPrivateKey = "8F72F6B29E6E225A36B68DFE333C7CE5E55D83249D3D2CD6332671FA445C4DD3";
    private String adminPublicKey = "0259dee66ab619c4a9e215d070052d1ae3a2075e5f58c67516b2e4884a88c79be9";
    private String adminAddress = "16RAWnYJhHB2M9c1k9LQywo6CtxGe38vi5";
    private String broadcastAddress = "*";
    private long initCoins = 10000;
    private long fixedMineReward = 500;
    private String initHash = "1";
    private int proofLevel = 4;
    private int genesisProofLevel = 1;
    private long spawnSecond = 600;
    private long minTradeCount = 1;

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
    public BlockChainImpl(MetaTradeGatewayProperties properties){
        this.properties = properties;
    }

    private Block CreateGenesisBlock(){
        Block block = new Block(initHash, genesisProofLevel);
        log.info("Waiting sign trade...");
        
        Trade trade = new Trade(adminAddress, broadcastAddress,
                initCoins, 0, System.currentTimeMillis(), "", adminPublicKey, "INIT");
        JniSigner signer = new JniSigner();
        String signature = signer.SignTrade(trade.getHash(), adminPrivateKey);
        trade.setSignature(signature);

        block.getBlockBody().add(trade);
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
        log.info(String.format("InitCoins: %d", initCoins));
        fixedMineReward = properties.getFixedMineReward();
        log.info(String.format("FixedMineReward: %d", fixedMineReward));
        initHash = properties.getInitHash();
        log.info(String.format("InitHash: %s", initHash));
        proofLevel = properties.getProofLevel();
        log.info(String.format("ProofLevel: %d", proofLevel));
        genesisProofLevel = properties.getGenesisProofLevel();
        log.info(String.format("GenesisProofLevel: %d", genesisProofLevel));
        spawnSecond = properties.getSpawnSecond();
        log.info(String.format("SpawnSecond: %d", spawnSecond));
        minTradeCount = properties.getMinTradeCount();
        log.info(String.format("MinTradeCount: %d", minTradeCount));
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
        RewardMiner(block, adminAddress);

        status = Status.FINISHED;
        log.info("BlockChain Status: FINISHED");
    }

    @Override
    public void insertTrade(Trade trade){
        lock.writeLock().lock();
        tradeList.add(trade);
        lock.writeLock().unlock();
    }

    @Override
    public int spawnRawBlock(){
        if(status != Status.FINISHED || tradeList.size() < minTradeCount){
            return -1;
        }
        lock.writeLock().lock();
        rawBlockDeque.push(new RawBlock(proofLevel, new CopyOnWriteArrayList<>(tradeList)));
        this.tradeList.clear();
        lock.writeLock().unlock();
        return proofLevel;
    }

    @Override
    public List<Block> getChain(){
        lock.readLock().lock();
        var chain = this.chain;
        lock.readLock().unlock();
        return chain;
    }

    @Override
    public List<Block> getChainByIndex(int index){
        lock.readLock().lock();
        var chain = this.chain;
        if(index < this.chain.size() && index >= 0){
            chain = this.chain.subList(index, this.chain.size());
        }        
        lock.readLock().unlock();
        return chain;
    }

    @Override
    public List<RawBlock> getRawBlockList(){
        lock.readLock().lock();
        var rawBlockList = this.rawBlockDeque.stream().toList();
        lock.readLock().unlock();
        return rawBlockList;
    }

    @Override
    public List<Trade> getTradeList(){
        lock.readLock().lock();
        var tradeList = this.tradeList;
        lock.readLock().unlock();
        return tradeList;
    }


    private void RewardMiner(Block block, String address){
        Trade trade = new Trade(adminAddress, address,
            block.getBlockCommission() + fixedMineReward, 0, System.currentTimeMillis(), "", adminPublicKey, "REWARD");
        JniSigner signer = new JniSigner();
        trade.setSignature(signer.SignTrade(trade.getHash(), adminPrivateKey));
        lock.writeLock().lock();
        if (rawBlockDeque.size() == 0){
            this.tradeList.add(0, trade);
        }
        else{
            rawBlockDeque.getFirst().blockBody().add(0, trade);
        }
        lock.writeLock().unlock();
        log.info(String.format("BlockChain Rewarding Miner: %s %d", trade.getReceiverAddress(), trade.getAmount()));

    }


    private void addBlock(Block block){
        lock.writeLock().lock();
        chain.add(block);
        rawBlockDeque.removeFirst();

        log.info(String.format("BlockChain Add Block: PrevHash - %s MerkleHash - %s", block.getPrevHash(), block.getMerkleHash()));

        RewardMiner(block, proofMap.get(block.getProof()));

        proofMap.clear();
        agreeMap.clear();
        blockMap.clear();
        isFoundFlag.set(false);
        lock.writeLock().unlock();
    }

    @Override
    public Block getLastBlock(){
        lock.readLock().lock();
        var block = this.chain.get(this.chain.size()-1);
        lock.readLock().unlock();
        return block;
    }

    @Override
    public void addProof(ProofMessage message){
        if(!proofMap.containsKey(message.getProof())){
            lock.writeLock().lock();
            proofMap.put(message.getProof(), message.address());
            agreeMap.put(message.getProof(), 0);
            blockMap.put(message.getProof(), message.block());
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean addAgree(int proof, long counter){
        if(!isFoundFlag.get()){
            lock.writeLock().lock();
            agreeMap.put(proof, agreeMap.get(proof)+1);
            lock.writeLock().unlock();
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
        lock.readLock().lock();
        var spawnSecond = this.spawnSecond;
        lock.readLock().unlock();
        return spawnSecond;
    }

}
