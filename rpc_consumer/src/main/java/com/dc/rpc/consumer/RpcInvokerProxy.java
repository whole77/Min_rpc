package com.dc.rpc.consumer;


import com.dc.rpc.core.MiniRpcFuture;
import com.dc.rpc.core.MiniRpcRequest;
import com.dc.rpc.core.MiniRpcRequestHolder;
import com.dc.rpc.core.MiniRpcResponse;
import com.dc.rpc.registry.RegistryService;
import com.rpc.protocol.protocol.MiniRpcProtocol;
import com.rpc.protocol.protocol.MsgHeader;
import com.rpc.protocol.protocol.MsgType;
import com.rpc.protocol.protocol.ProtocolConstants;
import com.rpc.protocol.serialization.SerializationTypeEnum;
import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class RpcInvokerProxy implements InvocationHandler {
    //服务版本号
    private final String serviceVersion;
    //超时时间
    private final long timeout;
    //注册中心
    private final RegistryService registryService;

    public RpcInvokerProxy(String serviceVersion, long timeout, RegistryService registryService) {
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.registryService = registryService;
    }

    @Override
    public String toString() {
        return "RpcInvokerProxy{" +
                "serviceVersion='" + serviceVersion + '\'' +
                ", timeout=" + timeout +
                ", registryService=" + registryService +
                '}';
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //填充发送的协议对象
        MiniRpcProtocol<MiniRpcRequest> protocol = new MiniRpcProtocol<>();
        //协议头
        MsgHeader header = new MsgHeader();
        //自增id  使用AtomicLong类来保证多线程去情况下的线程安全性问题(cas)
        long requestId = MiniRpcRequestHolder.REQUEST_ID_GEN.incrementAndGet();
        //魔术
        header.setMagic(ProtocolConstants.MAGIC);
        //协议版本
        header.setVersion(ProtocolConstants.VERSION);
        //消息id
        header.setRequestId(requestId);
        //序列化方式
//        header.setSerialization((byte) SerializationTypeEnum.HESSIAN.getType());
        //消息类型
        header.setMsgType((byte) MsgType.REQUEST.getType());
        //消息状态
        header.setStatus((byte) 0x1);
        protocol.setHeader(header);
        //rpc请求调用
        MiniRpcRequest request = new MiniRpcRequest();
        //服务版本
        request.setServiceVersion(this.serviceVersion);
        //服务名称(接口名称)
        request.setClassName(method.getDeclaringClass().getName());
        //方法名称
        request.setMethodName(method.getName());
        //方法参数类型
        request.setParameterTypes(method.getParameterTypes());
        //方法参数
        request.setParams(args);
        protocol.setBody(request);

        RpcConsumer rpcConsumer = new RpcConsumer();
        MiniRpcFuture<MiniRpcResponse> future = new MiniRpcFuture<>(new DefaultPromise<>(new DefaultEventLoop()), timeout);
        //key: 协议头中的消息id  value: MiniRpcFuture对象
        MiniRpcRequestHolder.REQUEST_MAP.put(requestId, future);
        //调用rpc发送请求
        rpcConsumer.sendRequest(protocol, this.registryService);
        return future.getPromise().get(future.getTimeout(), TimeUnit.MILLISECONDS).getData();
    }
}
