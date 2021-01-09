package org.example.serializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.example.HalResource;

import java.io.IOException;


// see https://stackoverflow.com/questions/14714328/jackson-how-to-add-custom-property-to-the-json-without-modifying-the-pojo/15365957#15365957

public class HalTypedResourceSerializer<T extends HalResource<?>> extends StdSerializer<T> {

    private final JsonSerializer<T> _source;

    public HalTypedResourceSerializer(JsonSerializer<T> src){
        super(src.handledType());
        this._source = src;
    }

    @Override
    public void serialize(T bean, JsonGenerator gen, SerializerProvider provider) throws IOException {

        // add type information at resource level, because jackson doesn't serialize type prefix for polymorphic class not do it with @JsonUnwrapped ans AS_PROPERTY


        // Find type serializer of the content
        TypeSerializer contentTypeSerializer = bean.getContent() == null ? null
                : provider.findTypeSerializer(provider.constructType(bean.getContent().getClass()));

        if (contentTypeSerializer != null && JsonTypeInfo.As.PROPERTY == contentTypeSerializer.getTypeInclusion()){
            // polymorphic content -> build a custom type serializer (reusing content typeSerializer)
            AsPropertyTypeSerializer ser = new AsPropertyTypeSerializer(
                    new HalTypedResourceTypeIdResolver(contentTypeSerializer.getTypeIdResolver()),
                    null,
                    contentTypeSerializer.getPropertyName());

            _source.serializeWithType(bean, gen, provider, ser);
        } else  {
            // use source serializer
            _source.serialize(bean, gen, provider);
        }

    }

}
