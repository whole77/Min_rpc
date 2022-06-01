package com.dc.rpc.core;

import lombok.Data;

/**
 * @Author: LaiLai
 * @Date: 2022/05/04/9:27
 * 存储在zookeeper上的服务元数据信息
 */
@Data
public class ServiceMeta {//服务的元数据信息
    //服务类型名称
    private String serviceName;
    //服务版本
    private String serviceVersion;
    //服务结点地址
    private String serviceAddr;
    //服务结点端口
    private int servicePort;

}
