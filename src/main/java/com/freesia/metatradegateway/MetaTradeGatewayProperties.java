package com.freesia.metatradegateway;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = {"classpath:metatrade-gateway.properties","file:./config/metatrade-gateway.properties"},ignoreResourceNotFound = true)
@ConfigurationProperties(prefix = "metatrade-gateway")
public class MetaTradeGatewayProperties {
    private String adminAddress;
    private String broadcastAddress;
    private double initCoins;
    private double fixedMineReward;
    private String initHash;
    private int proofLevel;
    private int genesisProofLevel;
    private long spawnSecond;

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

    public double getInitCoins() {
        return initCoins;
    }

    public void setInitCoins(double initCoins) {
        this.initCoins = initCoins;
    }

    public double getFixedMineReward() {
        return fixedMineReward;
    }

    public void setFixedMineReward(double fixedMineReward) {
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
}
