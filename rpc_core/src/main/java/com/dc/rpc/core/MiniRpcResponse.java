package com.dc.rpc.core;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 封装rpc响应返回的结果参数
 */
@Data
public class MiniRpcResponse implements Serializable {
    //返回结果数据
    private Object data;
    //返回结果错误信息(如果调用成功则默认为空，否则返回值带上对应的错误信息)
    private String message;
}
