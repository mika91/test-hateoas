package org.example.serializer;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import org.example.HalResource;

public class HalTypedResourceSerializerModifier extends BeanSerializerModifier {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {

        // override HalResource serializer to fix content polymorphism serialization/deserialization
        if (serializer != null && HalResource.class.isAssignableFrom(serializer.handledType())
                &&  beanDesc != null && HalResource.class.isAssignableFrom(beanDesc.getBeanClass())) {
            return new HalTypedResourceSerializer(serializer);
        }

        return serializer;
    }
}
