package com.dc.rpc.core;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import lombok.Data;

/**
 * 封装异步执行的结果
 * @param <T>
 */
@Data
public class MiniRpcFuture<T> {
    //相对与future来说，promise可以人工设置业务逻辑的成功与失败
    private Promise<T> promise;
    private long timeout;

    public MiniRpcFuture(Promise<T> promise, long timeout) {
        this.promise = promise;
        this.timeout = timeout;
    }
}
