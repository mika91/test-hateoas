package org.example.serializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.example.HalResource;

import java.io.IOException;


@SuppressWarnings("rawtypes")
class HalTypedResourceTypeIdResolver extends TypeIdResolverBase {

    private final TypeIdResolver _resolver;

    public HalTypedResourceTypeIdResolver(TypeIdResolver contentTypeIdResolver){
        _resolver = contentTypeIdResolver;
    }

    @Override
    public String idFromValue(Object value) {
        return _resolver.idFromValue(((HalResource) value).getContent());
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        return _resolver.idFromValueAndType(((HalResource) value).getContent(), suggestedType);
    }

    @Override
    public String idFromBaseType() {
        return _resolver.idFromBaseType();
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id)  throws IOException {
        throw new IllegalStateException("TypeIdResolverBase: "+getClass().getName()+" MUST only be used for serialization (NO deserialization)");
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return _resolver.getMechanism();
    }
}
