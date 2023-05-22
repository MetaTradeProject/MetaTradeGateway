package com.freesia.metatradegateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.web.bind.annotation.RestController;

import com.freesia.metatradegateway.blockchain.BlockChainService;
import com.freesia.metatradegateway.blockchain.model.Trade;
import com.freesia.metatradegateway.message.AgreeMessage;
import com.freesia.metatradegateway.message.JudgeMessage;
import com.freesia.metatradegateway.message.LocateMessage;
import com.freesia.metatradegateway.message.ProofMessage;
import com.freesia.metatradegateway.message.SemiSyncMessage;
import com.freesia.metatradegateway.message.SpawnMessage;
import com.freesia.metatradegateway.message.SyncMessage;
import com.freesia.metatradegateway.rpc.FakeTradeServer;
import com.freesia.metatradegateway.websocket.WebSocketConnCounter;

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

    private final MetaTradeGatewayProperties properties;

    @Autowired
    public MetaTradeController(WebSocketConnCounter connCounter, SimpMessagingTemplate simpMessagingTemplate, BlockChainService blockChainService, MetaTradeGatewayProperties properties){
        this.connCounter = connCounter;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.blockChainService = blockChainService;
        this.properties = properties;

        log.info("Start grpc server on port " + this.properties.getGrpcServicePort());
        FakeTradeServer fakeTradeServer = new FakeTradeServer(Integer.parseInt(this.properties.getGrpcServicePort()), this.blockChainService);
        Runnable task = () -> {
            try {
                fakeTradeServer.Run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        log.info("BlockChain Service initializing...");
        this.blockChainService.Init();
        log.info("BlockChain Service initialized successfully...");
    }


    @SubscribeMapping("/init/{startIndex}")
    public SyncMessage handleInit(@DestinationVariable int startIndex){
        var msg = new SyncMessage(blockChainService.getChainByIndex(startIndex), blockChainService.getRawBlockList(), blockChainService.getTradeList());

        return msg;
    }

    @MessageMapping("/sync")
    public void handleSync(Principal principal, LocateMessage message){
        log.info(String.valueOf(System.currentTimeMillis()));
        log.info(String.format("Send Sync Msg to %s", principal.getName()));
        var msg = new SyncMessage(blockChainService.getChainByIndex(message.startIndex()), blockChainService.getRawBlockList(), blockChainService.getTradeList());
        simpMessagingTemplate.convertAndSendToUser(principal.getName(), "/meta-trade/subscribe/sync", msg);
    }

    @MessageMapping("/trade")
    public void handleNewTrade(Trade trade){
        log.info(String.valueOf(System.currentTimeMillis()));
        log.info(String.format("Transferring trade: %s --> %s %d: %d",
                trade.getSenderAddress(), trade.getReceiverAddress(), trade.getAmount(), trade.getCommission()));
        blockChainService.insertTrade(trade);
        simpMessagingTemplate.convertAndSend("/meta-trade/subscribe/trade", trade);
    }

    @MessageMapping("/proof")
    public void handleNewProof(ProofMessage message){
        log.info(String.format("Transferring proof: %s %d", message.address(), message.getProof()));
        blockChainService.addProof(message);
        simpMessagingTemplate.convertAndSend("/meta-trade/subscribe/judge", new JudgeMessage(message.getProof()));
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
