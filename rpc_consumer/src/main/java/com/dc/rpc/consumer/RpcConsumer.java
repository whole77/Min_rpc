package com.dc.rpc.consumer;


import com.dc.rpc.core.MiniRpcRequest;
import com.dc.rpc.core.RpcServiceHelper;
import com.dc.rpc.core.ServiceMeta;
import com.dc.rpc.registry.RegistryService;
import com.rpc.protocol.codes.MiniRpcDecoder;
import com.rpc.protocol.codes.MiniRpcEncoder;
import com.rpc.protocol.handler.RpcResponseHandler;
import com.rpc.protocol.protocol.MiniRpcProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class RpcConsumer {
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    public RpcConsumer() {
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new MiniRpcEncoder())
                                .addLast(new MiniRpcDecoder())
                                .addLast(new RpcResponseHandler());
                    }
                });
    }

    public void sendRequest(MiniRpcProtocol<MiniRpcRequest> protocol, RegistryService registryService) throws Exception {
        //获取协议的请求体
        MiniRpcRequest request = protocol.getBody();
        //得到请求体中的参数列表
        Object[] params = request.getParams();
        //通过服务类型名称+服务版本来构建zookeeper红根路径下的服务名
        String serviceKey = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getServiceVersion());
        //为了使服务结点接受流量更加的均匀，一般可以使用RPC 服务接口参数列表中第一个参数的 hashCode 作为参考依据
        int invokerHashCode = params.length > 0 ? params[0].hashCode() : serviceKey.hashCode();
        //同过key得到zookeeper中服务名称下的所有服务结点实例
        //服务名称+服务版本+服务提供者的ip地址+hashcode码
        ServiceMeta serviceMetadata = registryService.discovery(serviceKey, invokerHashCode);

        //服务元数据信息
        if (serviceMetadata != null) {
            //发起连接获得异步结果对象，通过netty调用connect得到ChannelFuture对象
            ChannelFuture future = bootstrap.connect(serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort()).sync();
            //注册一个监听器
            future.addListener((ChannelFutureListener) arg0 -> {
                //如果请求发送成功
                if (future.isSuccess()) {
                    log.info("connect rpc server {} on port {} success.", serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort());
                } else {
                    //如果rpc请求发送失败
                    log.error("connect rpc server {} on port {} failed.", serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort());
                    //打印异常堆栈信息
                    future.cause().printStackTrace();
                    //优雅的关闭EventLoopGroup
                    eventLoopGroup.shutdownGracefully();
                }
            });
            //将协议对象发送过去
            future.channel().writeAndFlush(protocol);
        }
    }
}
