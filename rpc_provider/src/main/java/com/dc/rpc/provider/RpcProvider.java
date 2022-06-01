package com.dc.rpc.provider;



import com.dc.rpc.core.RpcServiceHelper;
import com.dc.rpc.core.ServiceMeta;
import com.dc.rpc.provider.annotation.RpcService;
import com.dc.rpc.registry.RegistryService;
import com.rpc.protocol.codes.MiniRpcDecoder;
import com.rpc.protocol.codes.MiniRpcEncoder;
import com.rpc.protocol.handler.RpcRequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: LaiLai
 * @Date: 2022/05/04/15:13
 */
@Slf4j
public class RpcProvider implements InitializingBean, BeanPostProcessor {
    //服务器ip地址
    private String serverAddress;
    //服务器端口
    private final int serverPort;
    //使用的注册中心
    private final RegistryService serviceRegistry;
    //key：服务类型名称 + 服务版本  value：服务接口实现bean对象
    private final Map<String, Object> rpcServiceMap = new HashMap<>();

    public RpcProvider(int serverPort, RegistryService serviceRegistry) {//构造方法实例化
        this.serverPort = serverPort;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void afterPropertiesSet() {
        new Thread(() -> {
            try {
                startRpcServer();
            } catch (Exception e) {
                log.error("start rpc server error.", e);
            }
        }).start();
    }

    private void startRpcServer() throws Exception {
        //获取本地ip地址
        this.serverAddress = InetAddress.getLocalHost().getHostAddress();

        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE,true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new MiniRpcEncoder())
                                    //解码后可以获取到Rpc调用的请求协议MiniRpcProtocol<MiniRpcRequest>
                                    .addLast(new MiniRpcDecoder())
                                    .addLast(new IdleStateHandler(5,0,0, TimeUnit.SECONDS))
                                    //在这个handler里面就可以获取到请求协议MiniRpcProtocol<MiniRpcRequest>，并且传入了服务实现bean对象
                                    //返回服务调用结果
                                    .addLast(new RpcRequestHandler(rpcServiceMap));
//                                    .addLast(new HeartBeatServerHandler());

                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);//tpc保活机制

            ChannelFuture channelFuture = bootstrap.bind(this.serverAddress, this.serverPort).sync();
            log.info("server addr {} started on port {}", this.serverAddress, this.serverPort);
            channelFuture.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    /**
     * spring中后继处理器会将所有的bean对象都进行后继处理
     * springBean一个重要的扩展点，在bean初始化后调用
     * 将服务RpcService将服务元数据信息注册到注册中心，然后将bean对象保存在map结构里
     **/
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //看springioc容器中所有的bean是由加了RpcService注解
        RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
        if (rpcService != null) {
            //获取服务类型名称
            String serviceName = rpcService.serviceInterface().getName();
            //获取版本号
            String serviceVersion = rpcService.serviceVersion();

            try {
                //服务的元数据信息
                ServiceMeta serviceMeta = new ServiceMeta();
                //服务器ip
                serviceMeta.setServiceAddr(serverAddress);
                //服务器端口
                serviceMeta.setServicePort(serverPort);
                //服务类型名称(接口名称)
                serviceMeta.setServiceName(serviceName);
                //服务版本
                serviceMeta.setServiceVersion(serviceVersion);
                //将服务的元数据注册到注册中心
                serviceRegistry.register(serviceMeta);
                //key：由服务类型+服务版本组成的字符串  value：提供服务的bean对象
                rpcServiceMap.put(RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion()), bean);
            } catch (Exception e) {
                log.error("failed to register service {}#{}", serviceName, serviceVersion, e);
            }
        }
        return bean;
    }

}