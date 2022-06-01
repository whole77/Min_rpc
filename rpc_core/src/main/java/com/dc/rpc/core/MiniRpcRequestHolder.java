package com.dc.rpc.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author lailai
 */
public class MiniRpcRequestHolder {

    public final static AtomicLong REQUEST_ID_GEN = new AtomicLong(0);
    //key:协议头中消息的id   value：MiniRpcFuture<MiniRpcResponse>
    public static final Map<Long, MiniRpcFuture<MiniRpcResponse>> REQUEST_MAP = new ConcurrentHashMap<>();
}
