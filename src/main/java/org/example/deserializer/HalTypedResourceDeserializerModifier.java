package org.example.deserializer;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import org.example.HalResource;

public class HalTypedResourceDeserializerModifier extends BeanDeserializerModifier {

    // https://stackoverflow.com/questions/18313323/how-do-i-call-the-default-deserializer-from-a-custom-deserializer-in-jackson
    // TODO: test converter for serialization/deserializtion

    // TODO: DelegatingDeserializer

    @Override
    public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {

        // override HalResource serializer to fix content polymorphism serialization/deserialization
        if (deserializer instanceof BeanDeserializerBase && HalResource.class.isAssignableFrom(deserializer.handledType())
                && beanDesc != null && HalResource.class.isAssignableFrom(beanDesc.getBeanClass())) {


            // TODO: logic to kown it it's a polymorphic bean

            return new HalTypedResourceDeserializer((BeanDeserializerBase) deserializer);
        }

        return deserializer;
    }
}
