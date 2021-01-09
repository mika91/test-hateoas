package org.example.deserializer;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.databind.util.NameTransformer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

class SinglePolyUnwrappedDeserializerImp<T> extends StdDeserializer<T> {

    DeserializationContext ctxt;

    private JavaType type;
    private JsonDeserializer<T> beanDeserializer;
    Set<String> ownPropertyNames;

    BeanPropertyDefinition unwrappedProperty;
    JavaType unwrappedType;
    NameTransformer nameTransformer;
    String unwrappedPropertyName;

    JsonDeserializer<T> _source;

    public SinglePolyUnwrappedDeserializerImp(DeserializationContext ctx) throws JsonMappingException {
        super((JavaType) null);


        this.ctxt = ctx;


        type = ctx.getContextualType();

        // init
        var description = ctxt.getConfig().introspect(type);

        List<JsonUnwrapped> tempUnwrappedAnnotation = new ArrayList<>();



        List<BeanPropertyDefinition> unwrappedProperties = description.findProperties().stream().filter(prop ->
                Stream.of(prop.getConstructorParameter(), prop.getMutator(), prop.getField())
                .filter(x->x!= null).anyMatch(member -> {
            JsonUnwrapped unwrappedAnnotation = member.getAnnotation(JsonUnwrapped.class);
            if (unwrappedAnnotation != null) {
                tempUnwrappedAnnotation.add(unwrappedAnnotation);
                member.getAllAnnotations().add(notUnwrappedAnnotation);
            }
            return unwrappedAnnotation != null;
        })).collect(Collectors.toList());

        // check count
        switch (unwrappedProperties.size()){
            case 0: throw new IllegalStateException("JsonUnwrapped properties not found in ${type.typeName}"); // TODO
            case 1 : unwrappedProperty = unwrappedProperties.get(0); break; // OK
            default: throw new IllegalStateException("JMultiple @JsonUnwrapped properties found in ${type.typeName}"); // TODO
        }

        // ???
        if (tempUnwrappedAnnotation.size() == 1){
            nameTransformer = NameTransformer.simpleTransformer(tempUnwrappedAnnotation.get(0).prefix(), tempUnwrappedAnnotation.get(0).suffix());
        }


        unwrappedPropertyName = unwrappedProperty.getName();

        ownPropertyNames = description.findProperties().stream().map(it->it.getName()).collect(Collectors.toSet());
        ownPropertyNames.remove(unwrappedPropertyName);
        ownPropertyNames.removeAll(description.getIgnoredPropertyNames());

        unwrappedType = unwrappedProperty.getPrimaryType();

        var rawBeanDeserializer = ctxt.getFactory().createBeanDeserializer(ctxt, type, description);
        if (rawBeanDeserializer instanceof ResolvableDeserializer){
            ((ResolvableDeserializer) rawBeanDeserializer).resolve(ctxt);
        }


        beanDeserializer = (JsonDeserializer<T>) rawBeanDeserializer;
    }


    // TODO: do not support JsonCreator

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        return customDeser(p);
    }

    private T customDeser(JsonParser p) throws IOException {

        ObjectNode node = (ObjectNode) p.readValueAsTree();
        var ownNode = new ObjectNode(ctxt.getNodeFactory());
        var unwrappedNode = new ObjectNode(ctxt.getNodeFactory());


        node.fields().forEachRemaining( entry ->{
            var key = entry.getKey();
            var value = entry.getValue();
            var transformed = nameTransformer.reverse(key);
            if (transformed != null && !ownPropertyNames.contains(key)) {
                unwrappedNode.replace(transformed, value);
            } else {
                ownNode.replace(key, value);
            }
        });

        ownNode.replace(unwrappedPropertyName, unwrappedNode);

        var syntheticParser = new TreeTraversingParser(ownNode);
        syntheticParser.nextToken();

        return beanDeserializer.deserialize(syntheticParser, ctxt);

    }

    // TODO: rework
    static JsonUnwrapped notUnwrappedAnnotation;

    static {
        try {
            notUnwrappedAnnotation = NotUnwrapped.class.getDeclaredField("dummy").getAnnotation(JsonUnwrapped.class);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    static class NotUnwrapped {
        @JsonUnwrapped(enabled = false)
        Object dummy = null;
    }
}
