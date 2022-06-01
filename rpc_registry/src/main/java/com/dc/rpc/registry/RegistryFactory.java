package com.dc.rpc.registry;

/**
 * @Author: LaiLai
 * @Date: 2022/05/04/9:51
 */
public class RegistryFactory {
    /**
     *工厂模式创建注册中心实现类
     * 双重检验锁保证多线程情况下的线程安全性问题
     * 第一个if为了提高性能
     * 第二个if防止创建多了实例对象
     * 单例模式
     * @param registryAddr  注册中心地址
     * @param type  注册中心类型
     * @return
     * @throws Exception
     */
    //使用volatile保证多线程情况下的可见性
    private static volatile RegistryService registryService;//注册接口,由对应的实现类来实现

    public static RegistryService getInstance(String registryAddr, RegistryType type) throws Exception {//参数注册中心的地址，注册中心的类型

        if (null == registryService) {
            synchronized (RegistryFactory.class) {
                if (null == registryService) {
                    switch (type) {
                        case ZOOKEEPER:
                            registryService = new ZookeeperRegistryService(registryAddr);
                            break;
                    }
                }
            }
        }
        return registryService;
    }

}
