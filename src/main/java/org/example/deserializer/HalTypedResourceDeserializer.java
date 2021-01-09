package org.example.deserializer;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.DelegatingDeserializer;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.databind.util.NameTransformer;
import lombok.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HalTypedResourceDeserializer<T> extends DelegatingDeserializer {


    public HalTypedResourceDeserializer(JsonDeserializer<?> source){
        super(source);
    }

    @Override
    protected JsonDeserializer<?> newDelegatingInstance(JsonDeserializer<?> newDelegatee) {
        return new HalTypedResourceDeserializer(newDelegatee);
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        var extendedContext = ExtraContext.of(ctxt, _delegatee.handledType(), _delegatee);

        Object result;
        if (extendedContext.isPresent()) {
            result = customDeser(p, extendedContext.get());
        } else {
            result = _delegatee.deserialize(p, ctxt);
        }


        return result;

    }

    private T customDeser(JsonParser p, ExtraContext exctxt) throws IOException {

        ObjectNode node = (ObjectNode) p.readValueAsTree();
        var ownNode = new ObjectNode(exctxt.ctxt.getNodeFactory());
        var unwrappedNode = new ObjectNode(exctxt.ctxt.getNodeFactory());


        node.fields().forEachRemaining( entry ->{
            var key = entry.getKey();
            var value = entry.getValue();
            var transformed = exctxt.nameTransformer.reverse(key);
            if (transformed != null && !exctxt.ownPropertyNames.contains(key)) {
                unwrappedNode.replace(transformed, value);
            } else {
                ownNode.replace(key, value);
            }
        });

        ownNode.replace(exctxt.unwrappedPropertyName, unwrappedNode);





//        //return (T) p.getCodec().treeToValue(node, type.getRawClass());
//
        var syntheticParser = new TreeTraversingParser(ownNode);
        syntheticParser.nextToken();

        return (T) exctxt.beanDeserializer.deserialize(syntheticParser, exctxt.ctxt);
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


    @Data
    static class ExtraContext<T>{

        JavaType type;
        Set<String> ownPropertyNames;
        BeanPropertyDefinition unwrappedProperty;
        JavaType unwrappedType;
        NameTransformer nameTransformer;
        String unwrappedPropertyName;
        DeserializationContext ctxt;
        JsonDeserializer<T> beanDeserializer;

        public static Optional<ExtraContext> of(DeserializationContext ctxt, Class<?> type, JsonDeserializer source) throws JsonMappingException {

            ExtraContext result = new ExtraContext();

            result.ctxt = ctxt;
            //result.type = ctxt.getContextualType();
            result.type = ctxt.getTypeFactory().constructType(type);

            // init
            var description = ctxt.getConfig().introspect(result.type);

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
                case 0:
                    return Optional.empty();

                //throw new IllegalStateException("JsonUnwrapped properties not found in ${type.typeName}"); // TODO
                case 1 : result.unwrappedProperty = unwrappedProperties.get(0); break; // OK
                default: throw new IllegalStateException("JMultiple @JsonUnwrapped properties found in ${type.typeName}"); // TODO
            }

            // ???
            if (tempUnwrappedAnnotation.size() == 1){
                result.nameTransformer = NameTransformer.simpleTransformer(tempUnwrappedAnnotation.get(0).prefix(), tempUnwrappedAnnotation.get(0).suffix());
            }


            result.unwrappedPropertyName = result.unwrappedProperty.getName();

            result.ownPropertyNames = description.findProperties().stream().map(it->it.getName()).collect(Collectors.toSet());
            result.ownPropertyNames.remove(result.unwrappedPropertyName);
            result.ownPropertyNames.removeAll(description.getIgnoredPropertyNames());

            result.unwrappedType = result.unwrappedProperty.getPrimaryType();

            var rawBeanDeserializer = ctxt.getFactory().createBeanDeserializer(ctxt, result.type, description);
            if (rawBeanDeserializer instanceof ResolvableDeserializer){
                ((ResolvableDeserializer) rawBeanDeserializer).resolve(ctxt);
            }


            //result.beanDeserializer =  rawBeanDeserializer;
            result.beanDeserializer =  source;



            return Optional.of(result);
        }


    }

}
