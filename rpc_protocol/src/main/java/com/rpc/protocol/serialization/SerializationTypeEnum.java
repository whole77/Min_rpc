package com.rpc.protocol.serialization;

import lombok.Getter;

/**
 * @author  lailai
 * 枚举类
 */
public enum SerializationTypeEnum {
    HESSIAN(0x10),
    JSON(0x20);

    @Getter
    private final int type;

    SerializationTypeEnum(int type) {
        this.type = type;
    }
    /*
      匹配byte，如果没有匹配的返回默认hession
     */
    public static SerializationTypeEnum  findByType(byte serializationType) {
        for (SerializationTypeEnum typeEnum : SerializationTypeEnum.values()) {
            if (typeEnum.getType() == serializationType) {
                return typeEnum;
            }
        }
        return HESSIAN;
    }
}
