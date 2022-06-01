package com.rpc.protocol.codes;

import com.dc.rpc.core.MiniRpcRequest;
import com.dc.rpc.core.MiniRpcResponse;
import com.rpc.protocol.protocol.MiniRpcProtocol;
import com.rpc.protocol.protocol.MsgHeader;
import com.rpc.protocol.protocol.MsgType;
import com.rpc.protocol.protocol.ProtocolConstants;
import com.rpc.protocol.serialization.RpcSerialization;
import com.rpc.protocol.serialization.SerializationFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Author: LaiLai
 * @Date: 2022/05/05/23:08
 * 自定义解码器
 * 解码器在客户端和服务端均存在所以设计为复用
 */
public class MiniRpcDecoder extends ByteToMessageDecoder {

    /*
    +---------------------------------------------------------------+
    | 魔数 2byte | 协议版本号 1byte | 序列化算法 1byte | 报文类型 1byte  |
    +---------------------------------------------------------------+
    | 状态 1byte |        消息 ID 8byte     |      数据长度 4byte     |
    +---------------------------------------------------------------+
    |                   数据内容 （长度不定）                          |
    +---------------------------------------------------------------+
    */
    @Override
    public final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //协议头占用18个字节
        if (in.readableBytes() < ProtocolConstants.HEADER_TOTAL_LEN) {
            return;
        }
        // 标记读索引
        // 0 (已读)readindex (可读)writeindex(可写)
        in.markReaderIndex();

        //读取两个字节(魔术信息)
        short magic = in.readShort();
        //如果魔术信息不正确抛出异常说明不是规定的协议格式
        if (magic != ProtocolConstants.MAGIC) {
            throw new IllegalArgumentException("magic number is illegal, " + magic);
        }
        //读取一个字节的协议版本号
        byte version = in.readByte();
        //读取协议消息体的序列化方式
        byte serializeType = in.readByte();
        //读取报文的类型
        byte msgType = in.readByte();
        //读取报文的状态
        byte status = in.readByte();
        //读取消息id
        long requestId = in.readLong();
        //读取消息体的数据长度
        int dataLength = in.readInt();
        //如果读取的消息体长度小于了协议约定的长度则返回
        if (in.readableBytes() < dataLength) {
            //读索引归位
            in.resetReaderIndex();
            return;
        }
        //将读取到的数据放入在字节数组中
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        //获取rpc的类型
        MsgType msgTypeEnum = MsgType.findByType(msgType);
        if (msgTypeEnum == null) {
            return;
        }
        //设置协议头
        MsgHeader header = new MsgHeader();
        header.setMagic(magic);
        header.setVersion(version);
        header.setSerialization(serializeType);
        header.setStatus(status);
        header.setRequestId(requestId);
        header.setMsgType(msgType);
        header.setMsgLen(dataLength);

        //获取序列化接口实现类
        RpcSerialization rpcSerialization = SerializationFactory.getRpcSerialization(serializeType);
        //因为编解码器在服务端和客户端都存在所以设置为复用类型
        //根据MsgType，需要反序列化出不同的协议体对象
        switch (msgTypeEnum) {
            case REQUEST:
                //反序列化
                MiniRpcRequest request = rpcSerialization.deserialize(data, MiniRpcRequest.class);
                if (request != null) {
                    MiniRpcProtocol<MiniRpcRequest> protocol = new MiniRpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(request);
                    out.add(protocol);
                }
                break;
            case RESPONSE:
                MiniRpcResponse response = rpcSerialization.deserialize(data, MiniRpcResponse.class);
                if (response != null) {
                    MiniRpcProtocol<MiniRpcResponse> protocol = new MiniRpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(response);
                    out.add(protocol);
                }
                break;
            case HEARTBEAT:
                // TODO
                break;
        }
    }
}
