package com.rpc.protocol.serialization;

import com.rpc.protocol.serialization.hession2.HessianSerialization;

/**
 * 序列化工厂
 */
public class SerializationFactory {

    public static RpcSerialization getRpcSerialization(byte serializationType) {
        SerializationTypeEnum typeEnum = SerializationTypeEnum.findByType(serializationType);

        switch (typeEnum) {
            case HESSIAN:
                return new HessianSerialization();
//            case JSON:
//                return new JsonSerialization();
            default:
                throw new IllegalArgumentException("serialization type is illegal, " + serializationType);
        }
    }
}
