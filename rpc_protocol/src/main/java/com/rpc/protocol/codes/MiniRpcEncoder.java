package com.rpc.protocol.codes;

import com.rpc.protocol.protocol.MiniRpcProtocol;
import com.rpc.protocol.protocol.MsgHeader;
import com.rpc.protocol.serialization.RpcSerialization;
import com.rpc.protocol.serialization.SerializationFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Author: LaiLai
 * @Date: 2022/05/05/23:17
 */
public class MiniRpcEncoder extends MessageToByteEncoder<MiniRpcProtocol<Object>> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, MiniRpcProtocol<Object> miniRpcProtocol, ByteBuf byteBuf) throws Exception {
        MsgHeader header = miniRpcProtocol.getHeader();
        //将请求头进行编码处理
        byteBuf.writeShort(header.getMagic());
        byteBuf.writeByte(header.getVersion());
        byteBuf.writeByte(header.getSerialization());
        byteBuf.writeByte(header.getMsgType());
        byteBuf.writeByte(header.getStatus());
        byteBuf.writeLong(header.getRequestId());
        //请求体先序列化再编码
        RpcSerialization rpcSerialization = SerializationFactory.getRpcSerialization(header.getSerialization());
        byte[] serializeByte = rpcSerialization.serialize(miniRpcProtocol.getBody());
        int len = serializeByte.length;
        byteBuf.writeInt(len);
        byteBuf.writeBytes(serializeByte);
    }
}
