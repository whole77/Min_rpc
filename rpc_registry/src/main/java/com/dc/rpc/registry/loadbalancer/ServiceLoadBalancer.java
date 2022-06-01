package com.dc.rpc.registry.loadbalancer;

import java.util.List;

/**
 * 服务负载均衡发现结点
 * @param <T>
 */
public interface ServiceLoadBalancer<T> {
    T select(List<T> servers, int hashCode);
}