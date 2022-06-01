package com.dc.rpc.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
为了方便解耦使用ConfigurationProperties将配置文件与字段相绑定
 @author lailai
 **/
@Data
@ConfigurationProperties(prefix = "rpc")
public class RpcProperties {

    //服务端暴露的端口
    private int servicePort;
    //注册的中心地址
    private String registryAddr;
    //注册中心类型
    private String registryType;

}