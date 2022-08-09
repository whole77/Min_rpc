package com.rpc.protocol.protocol;

import lombok.Data;

/**
 * @Author: LaiLai
 * @Date: 2022/05/04/17:19
 * 消息 ID 将消息id 与 MiniRpcFuture<MiniRpcResponse>相绑定 里面封装了异步执行的结果
 * 数据长度  避免tcp拆包粘包问题
 * 魔术 快速判断报数是否合法
 */
@Data
public class MsgHeader {
    /*
    +---------------------------------------------------------------+
    | 魔数 2byte | 协议版本号 1byte | 序列化算法 1byte | 报文类型 1byte  |
    +---------------------------------------------------------------+
    | 状态 1byte |        消息 ID 8byte     |      数据长度 4byte     |
    +---------------------------------------------------------------+
    */
    // 魔数
    private short magic;
    // 协议版本号
    private byte version;
    // 序列化方式
//    private byte serialization;
    //消息类型
    private byte msgType;
    // 消息状态
    private byte status;
    // 消息 ID
    private long requestId;
    // 数据长度
    private int msgLen;
}
