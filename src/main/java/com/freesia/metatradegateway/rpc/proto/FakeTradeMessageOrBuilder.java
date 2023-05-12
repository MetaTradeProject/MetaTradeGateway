// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: fake-trade.proto

package com.freesia.metatradegateway.rpc.proto;

public interface FakeTradeMessageOrBuilder extends
    // @@protoc_insertion_point(interface_extends:rpc.FakeTradeMessage)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string senderAddress = 1;</code>
   * @return The senderAddress.
   */
  java.lang.String getSenderAddress();
  /**
   * <code>string senderAddress = 1;</code>
   * @return The bytes for senderAddress.
   */
  com.google.protobuf.ByteString
      getSenderAddressBytes();

  /**
   * <code>string receiverAddress = 2;</code>
   * @return The receiverAddress.
   */
  java.lang.String getReceiverAddress();
  /**
   * <code>string receiverAddress = 2;</code>
   * @return The bytes for receiverAddress.
   */
  com.google.protobuf.ByteString
      getReceiverAddressBytes();

  /**
   * <code>double amount = 3;</code>
   * @return The amount.
   */
  double getAmount();

  /**
   * <code>int64 timestamp = 4;</code>
   * @return The timestamp.
   */
  long getTimestamp();

  /**
   * <code>string signature = 5;</code>
   * @return The signature.
   */
  java.lang.String getSignature();
  /**
   * <code>string signature = 5;</code>
   * @return The bytes for signature.
   */
  com.google.protobuf.ByteString
      getSignatureBytes();

  /**
   * <code>string senderPublicKey = 6;</code>
   * @return The senderPublicKey.
   */
  java.lang.String getSenderPublicKey();
  /**
   * <code>string senderPublicKey = 6;</code>
   * @return The bytes for senderPublicKey.
   */
  com.google.protobuf.ByteString
      getSenderPublicKeyBytes();

  /**
   * <code>string description = 7;</code>
   * @return The description.
   */
  java.lang.String getDescription();
  /**
   * <code>string description = 7;</code>
   * @return The bytes for description.
   */
  com.google.protobuf.ByteString
      getDescriptionBytes();
}