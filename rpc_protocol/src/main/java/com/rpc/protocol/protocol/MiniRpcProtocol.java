package com.rpc.protocol.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * 构建的协议类型
 * @param <T>泛型对象接受rpc请求调用或者rpc返回调用
 */
@Data
public class MiniRpcProtocol<T> implements Serializable {
    private MsgHeader header;
    private T body;
}
