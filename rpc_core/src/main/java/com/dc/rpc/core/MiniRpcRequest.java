package com.dc.rpc.core;

import lombok.Data;

import java.io.Serializable;

@Data
public class MiniRpcRequest implements Serializable {
    //服务版本
    private String serviceVersion;
    //服务类型名称
    private String className;
    //方法名称
    private String methodName;
    //参数列表
    private Object[] params;
    //参数类型
    private Class<?>[] parameterTypes;
}
