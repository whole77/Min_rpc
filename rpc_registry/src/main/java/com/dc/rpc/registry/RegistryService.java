package com.dc.rpc.registry;

import com.dc.rpc.core.ServiceMeta;

import java.io.IOException;

/**
 * @Author: LaiLai
 * @Date: 2022/05/04/9:15
 * 定义注册中心实现的接口
 */
public interface RegistryService {

    void register(ServiceMeta serviceMeta) throws Exception;

    void unRegister(ServiceMeta serviceMeta) throws Exception;

    ServiceMeta discovery(String serviceName, int invokerHashCode) throws Exception;

    void destroy() throws IOException;
}

