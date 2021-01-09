package org.example.hal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.HalResource;
import org.example.hal.model.*;
import org.example.serializer.HalTypedResourceModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HalSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup(){
        objectMapper = new ObjectMapper();
//

        // deserilization : Unrecognized field "@type"
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // for deserilization -> ADD a mixin to remove JsonUNwrapped annotation ?
        objectMapper.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);
        objectMapper.registerModule(new HalTypedResourceModule());
    }

    @Test
    public void test_Polymorphism_Generic() throws JsonProcessingException {

        // instantiate circle
        var circle = new Circle();
        circle.setX(2);
        circle.setY(3);
        circle.setRadius(5.6f);

        // resource
        var resource = new HalResource<Shape>(circle);
        resource.add(Link.of("http://local/shape", IanaLinkRelations.SELF));

        // SERIALIZATION
        var ser = objectMapper.writeValueAsString(resource);
        assertEquals("{\"@type\":\"CIRCLE\",\"x\":2,\"y\":3,\"radius\":5.6,\"links\":[{\"rel\":\"self\",\"href\":\"http://local/shape\"}]}", ser);

        // DESERIALIZATION
        var deser = objectMapper.readValue(ser, new TypeReference<HalResource<Shape>>() {});
        assertNotNull(deser);
    }

    @Test
    public void test_Polymorphism_ConcreteType() throws JsonProcessingException {

        // instantiate circle
        var circle = new Circle();
        circle.setX(2);
        circle.setY(3);
        circle.setRadius(5.6f);

        // resource
        var resource = new ShapeResource(circle);
        resource.add(Link.of("http://local/shape", IanaLinkRelations.SELF));

        // SERIALIZATION
        var ser = objectMapper.writeValueAsString(resource);
        //assertEquals("{\"@type\":\"CIRCLE\",\"x\":2,\"y\":3,\"radius\":5.6,\"extraField\":\"FOR TEST ONLY\",\"links\":[{\"rel\":\"self\",\"href\":\"http://local/shape\"}]}", ser);

        // DESERIALIZATION
        var actualResource = objectMapper.readValue(ser, ShapeResource.class);
        assertNotNull(actualResource);
        assertNotNull(actualResource.getContent());
        assertThat(actualResource.getContent(), instanceOf(Circle.class));
        // circle fields
        var actualCircle = (Circle) actualResource.getContent();
        assertEquals(5.6f, actualCircle.getRadius());
        assertEquals(2, actualCircle.getX());
        assertEquals(3, actualCircle.getY());
        // ShapeResource fields
        assertEquals("FOR TEST ONLY", actualResource.getExtraField());
    }

    @Test
    public void test_NoPolymorphism_Generic() throws JsonProcessingException {

        // instantiate circle
        var pojo = new Person();
        pojo.setAge(33);
        pojo.setName("mguerin");


        // resource
        var resource = new HalResource<Person>(pojo);
        resource.add(Link.of("http://local/person", IanaLinkRelations.SELF));

        // SERIALIZATION
        var ser = objectMapper.writeValueAsString(resource);
        assertEquals("{\"age\":33,\"name\":\"mguerin\",\"links\":[{\"rel\":\"self\",\"href\":\"http://local/person\"}]}", ser);

        // DESERIALIZATION
        var deser = objectMapper.readValue(ser, new TypeReference<HalResource<Person>>() {});
        assertNotNull(deser);
    }

    @Test
    public void test_NoPolymorphism_ConcreteType() throws JsonProcessingException {

        // instantiate circle
        var pojo = new Person();
        pojo.setAge(33);
        pojo.setName("mguerin");

        // resource
        var resource = new PersonResource(pojo);
        resource.add(Link.of("http://local/person", IanaLinkRelations.SELF));

        // SERIALIZATION
        var ser = objectMapper.writeValueAsString(resource);
        assertEquals("{\"age\":33,\"name\":\"mguerin\",\"gender\":\"male\",\"links\":[{\"rel\":\"self\",\"href\":\"http://local/person\"}]}", ser);

        // DESERIALIZATION
        var deser = objectMapper.readValue(ser, PersonResource.class);
        assertNotNull(deser);
    }

}
