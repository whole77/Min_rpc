package com.dc.rpc.core;

/**
 * @Author: LaiLai
 * @Date: 2022/05/04/15:04
 * 将服务名称和服务版本构造成一个新的字符串，以便与相同的服务器实例能造能在同一个字符串下
 */
public class RpcServiceHelper {
    public static String buildServiceKey(String serviceName, String serviceVersion) {
        //serviceName + "DC" +  serviceVersion
        return String.join("DC", serviceName, serviceVersion);
    }
}
