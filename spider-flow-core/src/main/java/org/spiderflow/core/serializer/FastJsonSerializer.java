package org.spiderflow.core.serializer;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerialContext;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.IdentityHashMap;

/**
 * Created on 2019-12-23
 */
public class FastJsonSerializer implements ObjectSerializer {

    public static SerializeConfig serializeConfig;

    static {
        serializeConfig = new SerializeConfig();
        FastJsonSerializer serializer = new FastJsonSerializer();
        serializeConfig.put(Long.TYPE, serializer);
        serializeConfig.put(Long.class, serializer);
        serializeConfig.put(BigDecimal.class, serializer);
        serializeConfig.put(BigInteger.class, serializer);
    }

    public static String toDisplayJson(Object value) {
        try (SerializeWriter writer = new SerializeWriter()) {
            JSONSerializer serializer = new DisplaySerializer(writer);
            serializer.write(value);
            return writer.toString();
        }
    }

    private static class DisplaySerializer extends JSONSerializer {

        DisplaySerializer(SerializeWriter writer) {
            super(writer, serializeConfig);
            // 同一个对象可以在不同分支展开，仅保留当前路径上的循环引用
            references = new IdentityHashMap<Object, SerialContext>() {
                @Override
                public SerialContext get(Object object) {
                    for (SerialContext current = getContext(); current != null; current = current.parent) {
                        if (current.object == object) {
                            return current;
                        }
                    }
                    return null;
                }

                @Override
                public boolean containsKey(Object object) {
                    return get(object) != null;
                }

                @Override
                public SerialContext put(Object object, SerialContext context) {
                    // 引用从当前序列化路径查询，无需缓存已访问对象
                    return null;
                }
            };
        }
    }

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        if(object == null){
            if(serializer.isEnabled(SerializerFeature.WriteNullNumberAsZero)){
                serializer.out.write("0");
            }else{
                serializer.out.writeNull();
            }
            return;
        }
        serializer.out.writeString(object.toString());
    }
}
