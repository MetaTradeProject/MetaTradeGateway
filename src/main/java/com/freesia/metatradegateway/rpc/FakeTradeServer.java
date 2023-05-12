package com.freesia.metatradegateway.rpc;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.freesia.metatradegateway.blockchain.BlockChainService;
import com.freesia.metatradegateway.blockchain.Trade;
import com.freesia.metatradegateway.rpc.proto.FakeTradeMessage;
import com.freesia.metatradegateway.rpc.proto.SubmitResult;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.stub.StreamObserver;

public class FakeTradeServer {
    private Server server;
    private BlockChainService service;

    public FakeTradeServer(int port, BlockChainService service){
        this.service = service;
        this.server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create()).
            addService(new FakeTradeService(this.service)).build();
    }

    /** Start serving requests. */
    public void Start() throws IOException {
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    FakeTradeServer.this.Stop();
                } 
                catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    /** Stop serving requests and shutdown resources. */
    public void Stop() throws InterruptedException {
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
        private BlockChainService service;

        FakeTradeService(BlockChainService service){
            this.service = service;
        }

        @Override
        public void submitFakeTrade(FakeTradeMessage request, StreamObserver<SubmitResult> responseObserver) {
            Trade trade = new Trade(request.getSenderAddress(), request.getReceiverAddress(), 
                request.getAmount(), 0, request.getTimestamp(), request.getSignature(), request.getSenderPublicKey(), request.getDescription());
            
            service.insertTrade(trade);
            
            var res = SubmitResult.newBuilder().setResult(true).build();
            responseObserver.onNext(res);
            responseObserver.onCompleted();
        }
        
    }
}