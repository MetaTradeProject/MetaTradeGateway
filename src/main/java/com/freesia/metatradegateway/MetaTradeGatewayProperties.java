package com.freesia.metatradegateway;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = {"classpath:metatrade-gateway.properties","file:./config/metatrade-gateway.properties"},ignoreResourceNotFound = true)
@ConfigurationProperties(prefix = "metatrade-gateway")
public class MetaTradeGatewayProperties {
    private String grpcServicePort;
    private String adminPrivateKey;
    private String adminPublicKey;
    private String adminAddress;
    private String broadcastAddress;
    private long initCoins;
    private long fixedMineReward;
    private String initHash;
    private int proofLevel;
    private int genesisProofLevel;
    private long spawnSecond;
    private long minTradeCount;

    public String getAdminPrivateKey() {
        return adminPrivateKey;
    }

    public void setAdminPrivateKey(String adminPrivateKey) {
        this.adminPrivateKey = adminPrivateKey;
    }

    public String getAdminPublicKey() {
        return adminPublicKey;
    }

    public void setAdminPublicKey(String adminPublicKey) {
        this.adminPublicKey = adminPublicKey;
    }

    public String getAdminAddress() {
        return adminAddress;
    }

    public void setAdminAddress(String adminAddress) {
        this.adminAddress = adminAddress;
    }

    public String getBroadcastAddress() {
        return broadcastAddress;
    }

    public void setBroadcastAddress(String broadcastAddress) {
        this.broadcastAddress = broadcastAddress;
    }

    public long getInitCoins() {
        return initCoins;
    }

    public void setInitCoins(long initCoins) {
        this.initCoins = initCoins;
    }

    public long getFixedMineReward() {
        return fixedMineReward;
    }

    public void setFixedMineReward(long fixedMineReward) {
        this.fixedMineReward = fixedMineReward;
    }

    public String getInitHash() {
        return initHash;
    }

    public void setInitHash(String initHash) {
        this.initHash = initHash;
    }

    public int getProofLevel() {
        return proofLevel;
    }

    public void setProofLevel(int proofLevel) {
        this.proofLevel = proofLevel;
    }

    public int getGenesisProofLevel() {
        return genesisProofLevel;
    }

    public void setGenesisProofLevel(int genesisProofLevel) {
        this.genesisProofLevel = genesisProofLevel;
    }

    public long getSpawnSecond() {
        return spawnSecond;
    }

    public void setSpawnSecond(long spawnSecond) {
        this.spawnSecond = spawnSecond;
    }

    public String getGrpcServicePort() {
        return grpcServicePort;
    }

    public void setGrpcServicePort(String grpcServicePort) {
        this.grpcServicePort = grpcServicePort;
    }

    public long getMinTradeCount() {
        return minTradeCount;
    }

    public void setMinTradeCount(long minTradeCount) {
        this.minTradeCount = minTradeCount;
    }
}
