package com.freesia.metatradegateway.rpc;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.freesia.metatradegateway.blockchain.BlockChainService;
import com.freesia.metatradegateway.blockchain.model.Trade;
import com.freesia.metatradegateway.rpc.proto.FakeTradeMessage;
import com.freesia.metatradegateway.rpc.proto.SubmitResult;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FakeTradeServer {
    private final Server server;
    private final BlockChainService service;

    public FakeTradeServer(int port, BlockChainService service, SimpMessagingTemplate simpMessagingTemplate){
        this.service = service;
        this.server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create()).
            addService(new FakeTradeService(this.service, simpMessagingTemplate)).build();
    }

    /** Start serving requests. */
    private void Start() throws IOException {
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("shutting down gRPC server since JVM is shutting down");
            try {
                FakeTradeServer.this.Stop();
            }
            catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            log.info("rpc server shut down");
        }));
    }

    /** Stop serving requests and shutdown resources. */
    private void Stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void BlockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public void Run() throws Exception {
        this.Start();
        this.BlockUntilShutdown();
    }

    private static class FakeTradeService extends FakeTradeGrpc.FakeTradeImplBase {
        private final BlockChainService service;
        private final SimpMessagingTemplate simpMessagingTemplate;

        FakeTradeService(BlockChainService service, SimpMessagingTemplate simpMessagingTemplate){
            this.service = service;
            this.simpMessagingTemplate = simpMessagingTemplate;
        }

        @Override
        public void submitFakeTrade(FakeTradeMessage request, StreamObserver<SubmitResult> responseObserver) {
            Trade trade = new Trade(request.getSenderAddress(), request.getReceiverAddress(), 
                request.getAmount(), 0, request.getTimestamp(), request.getSignature(), request.getSenderPublicKey(), request.getDescription());
            
            service.insertTrade(trade);
            simpMessagingTemplate.convertAndSend("/meta-trade/subscribe/trade", trade);

            var res = SubmitResult.newBuilder().setResult(true).build();
            responseObserver.onNext(res);
            responseObserver.onCompleted();
        }
        
    }
}
