package com.rpc.protocol.handler;
import com.dc.rpc.core.MiniRpcRequest;
import com.dc.rpc.core.MiniRpcResponse;
import com.dc.rpc.core.RpcServiceHelper;
import com.rpc.protocol.protocol.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import java.util.Map;

@Slf4j
public class RpcRequestHandler extends SimpleChannelInboundHandler<MiniRpcProtocol<MiniRpcRequest>> {
    //key：接口名称+服务版本  value：bean对象
    private final Map<String, Object> rpcServiceMap;

    public RpcRequestHandler(Map<String, Object> rpcServiceMap) {
        this.rpcServiceMap = rpcServiceMap;
    }

    /**
     * 服务提供者读取到rpc请求调用时，先经过解码器解码成MiniRpcProtocol<MiniRpcRequest>对象，然后通过预先放入map中的bean对象
     * 反射调用方法
     * 处理rpc请求调用耗时所以使用线程池
     * @param ctx
     * @param protocol
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MiniRpcProtocol<MiniRpcRequest> protocol) {
        //将任务放入线程池中执行
        RpcRequestProcessor.submitRequest(() -> {
            MiniRpcProtocol<MiniRpcResponse> resProtocol = new MiniRpcProtocol<>();
            //构建出响应的消息对象
            MiniRpcResponse response = new MiniRpcResponse();
            //获取协议头
            MsgHeader header = protocol.getHeader();
            //消息类型
            header.setMsgType((byte) MsgType.RESPONSE.getType());
            try {
                //获取服务实现类调用返回数据
                Object result = handle(protocol.getBody());
                response.setData(result);
                header.setStatus((byte) MsgStatus.SUCCESS.getCode());
                resProtocol.setHeader(header);
                resProtocol.setBody(response);
            } catch (Throwable throwable) {
                header.setStatus((byte) MsgStatus.FAIL.getCode());
                response.setMessage(throwable.toString());
                log.error("process request {} error", header.getRequestId(), throwable);
            }
            ctx.writeAndFlush(resProtocol);
        });
    }

    /**
     * 服务提供者会将服务元数据注册到注册中心并且会将标识了@RpcService的服务实现类放入到map结构中，那么根据map中的bean对象通过fastclass机制反射调用方法返回结果
     * @param request
     * @return
     * @throws Throwable
     */
    private Object handle(MiniRpcRequest request) throws Throwable {
        //将服务类型和版本号拼接
        String serviceKey = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getServiceVersion());
        //获取到服务接口实现类的bean对象
        Object serviceBean = rpcServiceMap.get(serviceKey);

        if (serviceBean == null) {
            throw new RuntimeException(String.format("service not exist: %s:%s", request.getClassName(), request.getMethodName()));
        }
        //获取类型信息
        Class<?> serviceClass = serviceBean.getClass();
        //获取方法名
        String methodName = request.getMethodName();
        //获取请求阐述类型
        Class<?>[] parameterTypes = request.getParameterTypes();
        //获取对应的请求参数
        Object[] parameters = request.getParams();

        FastClass fastClass = FastClass.create(serviceClass);
        //根据方法名和方法描述符(参数类型)来获得方法索引
        int methodIndex = fastClass.getIndex(methodName, parameterTypes);
        //代理调用bean对象方法
        return fastClass.invoke(methodIndex, serviceBean, parameters);
    }
}
