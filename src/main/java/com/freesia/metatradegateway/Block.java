package com.freesia.metatradegateway;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Block {
    private final String prevHash;
    private String merkleHash;
    private final int proofLevel; //prefix 0 counts
    private int proof;


    private final List<Trade> blockBody;

    public Block(String prevHash, int proofLevel) {
        this.prevHash = prevHash;
        this.proofLevel = proofLevel;
        this.blockBody = new CopyOnWriteArrayList<>();
    }

    public int getProofLevel() {
        return proofLevel;
    }

    public List<Trade> getBlockBody() {
        return blockBody;
    }

    public int getProof() {
        return proof;
    }

    @JsonIgnore
    public double getBlockCommission() {
        double ret = 0;
        for(Trade trade: blockBody){
            ret += trade.commission();
        }
        return ret;
    }

    private void calMerkleHash(){
        merkleHash = "";
        Deque<String> hashTree = new ArrayDeque<>();

        for(Trade trade: blockBody){
            hashTree.add(trade.getHash());
        }

        while(true){
            int sz = hashTree.size();
            if(sz == 1){
                break;
            }
            if(sz % 2 != 0){
                hashTree.addLast(hashTree.getLast());
            }

            int target = hashTree.size()/2;
            for(int i = 0; i < target; i++){
                String fir = hashTree.removeFirst();
                String sec = hashTree.removeFirst();
                hashTree.addLast(CryptoUtils.getSHA256(fir.concat(sec)));
            }
        }

        this.merkleHash = hashTree.getFirst();
    }

    public void setProof(int proof) {
        this.proof = proof;
    }

    @JsonIgnore
    public String getMineData(){
        calMerkleHash();
        return prevHash + merkleHash + proofLevel;
    }

    public String getPrevHash() {
        return prevHash;
    }

    public String getMerkleHash() {
        return merkleHash;
    }
}
