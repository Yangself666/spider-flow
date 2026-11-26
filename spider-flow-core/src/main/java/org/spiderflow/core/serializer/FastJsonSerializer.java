package org.spiderflow.core.serializer;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriter.Path;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;

public class FastJsonSerializer {

    private final JSONWriter writer;
    private final Map<Object, Path> ancestors = new IdentityHashMap<>();

    private FastJsonSerializer(JSONWriter writer) {
        this.writer = writer;
    }

    public static String toDisplayJson(Object value) {
        JSONWriter.Context context = JSONFactory.createWriteContext(JSONWriter.Feature.WriteByteArrayAsBase64);
        context.setDateFormat("millis");
        try (JSONWriter writer = JSONWriter.of(context)) {
            new FastJsonSerializer(writer).writeValue(value, Path.ROOT);
            return writer.toString();
        }
    }

    private void writeValue(Object value, Path path) {
        if (value == null) {
            writer.writeNull();
            return;
        }
        // 浏览器将这些数字作为字符串读取，保留精度和小数位
        if (value instanceof Long || value instanceof BigInteger || value instanceof BigDecimal) {
            writer.writeString(value.toString());
            return;
        }

        // 同一对象可以在不同分支展开，只为当前路径上的循环写入引用
        Path ancestor = ancestors.putIfAbsent(value, path);
        if (ancestor != null) {
            writer.writeReference(ancestor == path.parent ? ".." : ancestor.toString());
            return;
        }

        try {
            if (value instanceof Map<?, ?> map) {
                writer.startObject();
                map.forEach((key, item) -> writeProperty(String.valueOf(key), item, path));
                writer.endObject();
            } else if (value instanceof Iterable<?> items) {
                writeItems(items, path);
            } else if (value instanceof Object[] items) {
                writeItems(Arrays.asList(items), path);
            } else if (writer.getObjectWriter(value.getClass()) instanceof ObjectWriterAdapter<?> bean) {
                writer.startObject();
                for (FieldWriter<?> field : bean.getFieldWriters()) {
                    writeBeanProperty(field, value, path);
                }
                writer.endObject();
            } else {
                writer.writeAny(value);
            }
        } finally {
            ancestors.remove(value);
        }
    }

    private <T> void writeBeanProperty(FieldWriter<T> field, Object bean, Path path) {
        @SuppressWarnings("unchecked")
        T value = (T) bean;
        writeProperty(field.fieldName, field.getFieldValue(value), path);
    }

    private void writeProperty(String name, Object value, Path path) {
        if (value != null) {
            writer.writeName(name);
            writer.writeColon();
            writeValue(value, new Path(path, name));
        }
    }

    private void writeItems(Iterable<?> items, Path path) {
        writer.startArray();
        int index = 0;
        for (Object item : items) {
            if (index > 0) {
                writer.writeComma();
            }
            writeValue(item, new Path(path, index++));
        }
        writer.endArray();
    }
}
