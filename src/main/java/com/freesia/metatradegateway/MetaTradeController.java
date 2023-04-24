package com.freesia.metatradegateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.Duration;

@Slf4j
@Configuration
@RestController
@EnableScheduling
public class MetaTradeController implements SchedulingConfigurer {

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final WebSocketConnCounter connCounter;

    private final BlockChainService blockChainService;

    @Autowired
    public MetaTradeController(WebSocketConnCounter connCounter, SimpMessagingTemplate simpMessagingTemplate, BlockChainService blockChainService){
        this.connCounter = connCounter;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.blockChainService = blockChainService;
        log.info("BlockChain Service initializing...");
        this.blockChainService.Init();
        log.info("BlockChain Service initialized successfully...");
    }


    @SubscribeMapping("/init")
    public SyncMessage handleInit(){
        return new SyncMessage(blockChainService.getChain(), blockChainService.getRawBlockList(), blockChainService.getTradeList());
    }

    @MessageMapping("/sync")
    @SendToUser(value = "/meta-trade/subscribe/sync", broadcast = false)
    public SyncMessage handleSync(Principal principal){
        log.info(String.format("Send Sync Msg to %s", principal.getName()));
        return new SyncMessage(blockChainService.getChain(), blockChainService.getRawBlockList(), blockChainService.getTradeList());
    }

    @MessageMapping("/trade")
    @SendTo("/meta-trade/subscribe/trade")
    public Trade handleNewTrade(Trade trade){
        log.info(String.format("Transferring trade: %s --> %s %f: %f",
                trade.senderAddress(), trade.receiverAddress(), trade.amount(), trade.commission()));
        blockChainService.insertTrade(trade);
        return trade;
    }

    @MessageMapping("/proof")
    @SendTo("/meta-trade/subscribe/judge")
    public JudgeMessage handleNewProof(ProofMessage message){
        log.info(String.format("Transferring proof: %s %d", message.address(), message.getProof()));
        blockChainService.addProof(message);
        return new JudgeMessage(message.getProof());
    }

    @MessageMapping("/agree")
    public void handleNewAgreement(AgreeMessage message){
        log.info(String.format("Receiving Agree Msg from %s: %d", message.address(), message.proof()));
        if(blockChainService.addAgree(message.proof(), connCounter.onlineUsers())){
            //semi-sync
            log.info("Broadcasting semi-sync message...");
            simpMessagingTemplate.convertAndSend("/meta-trade/subscribe/semi-sync",
                    new SemiSyncMessage(blockChainService.getLastBlock(), blockChainService.getRawBlockList(), blockChainService.getTradeList()));
        }
    }

    public void handleSpawn(){
        int proofLevel = blockChainService.spawnRawBlock();
        log.info("tick");
        if(proofLevel != -1){
            log.info("BroadCasting spawning message...");
            simpMessagingTemplate.convertAndSend("/meta-trade/subscribe/spawn", new SpawnMessage(proofLevel));
        }
    }

    private final Runnable spawnTask = this::handleSpawn;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(spawnTask, triggerContext -> {
            long sec = blockChainService.getSpawnSecond();
            PeriodicTrigger trigger = new PeriodicTrigger(Duration.ofSeconds(sec));
            log.info(String.format("Spawn Task: Reset SpawnSecond to %d", sec));
            return trigger.nextExecution(triggerContext);
        });
    }
}
