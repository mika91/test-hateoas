package org.example.hal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.hal.model.Person;
import org.example.hal.model.PersonResource;
import org.example.hal.model.Shape;
import org.example.hal.model.ShapeResource;
import org.example.hal.serializer.HalTypedResourceModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.MockRestServiceServer.createServer;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class MvcTest {


    @Configuration
    @EnableHypermediaSupport(type = { EnableHypermediaSupport.HypermediaType.HAL, EnableHypermediaSupport.HypermediaType.COLLECTION_JSON })
    static class Config {

        public @Bean
        RestTemplate template() {
            return new RestTemplate();
        }

        @Bean
        @Primary
        public ObjectMapper objectMapper() {

            var objectMapper = new ObjectMapper()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL);

            // deserilization : Unrecognized field "@type"
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.registerModule(new Jackson2HalModule());

            // for deserilization -> ADD a mixin to remove JsonUNwrapped annotation ?
            objectMapper.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);
            objectMapper.registerModule(new HalTypedResourceModule());

            return objectMapper;

        }
    }


    @Autowired
    RestTemplate template;
    MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        this.server = createServer(template);

    }

    @Test
    void deser_SimpleConcrete() {

        var mockResponse = "{\"@type\":\"CIRCLE\",\"x\":2,\"y\":3,\"radius\":5.6,\"extraField\":\"FOR TEST ONLY\",\"_links\":{\"self\":{\"href\":\"http://local/shape\"}}}";

        server.expect(requestTo("/person")).andRespond(withSuccess(mockResponse, MediaTypes.HAL_JSON));

        var response = template.exchange("/person", HttpMethod.GET, null, ShapeResource.class);

        assertNotNull(response.getBody());
        assertTrue(response.getBody().getLinks().hasLink(IanaLinkRelations.SELF));
        assertNotNull(response.getBody().getContent());
        assertEquals("FOR TEST ONLY", response.getBody().getExtraField());
    }

    @Test
    void deser_PolyGeneric() {

        var mockResponse = "{\"@type\":\"CIRCLE\",\"x\":2,\"y\":3,\"radius\":5.6,\"_links\":{\"self\":{\"href\":\"http://local/shape\"}}}";

        server.expect(requestTo("/shape")).andRespond(withSuccess(mockResponse, MediaTypes.HAL_JSON));

        var response = template.exchange("/shape", HttpMethod.GET, null, new ParameterizedTypeReference<TypedHalResource<Shape>>(){});

        assertNotNull(response.getBody());
        assertTrue(response.getBody().getLinks().hasLink(IanaLinkRelations.SELF));
        assertNotNull(response.getBody().getContent());
    }

    @Test
    void deser_PolyConcrete() {

        var mockResponse = "{\"age\":33,\"name\":\"mguerin\",\"gender\":\"male\",\"_links\":{\"self\":{\"href\":\"http://local/person\"}}}";

        server.expect(requestTo("/person")).andRespond(withSuccess(mockResponse, MediaTypes.HAL_JSON));

        var response = template.exchange("/person", HttpMethod.GET, null, PersonResource.class);

        assertNotNull(response.getBody());
        assertTrue(response.getBody().getLinks().hasLink(IanaLinkRelations.SELF));
        assertNotNull(response.getBody().getContent());
        assertEquals("male", response.getBody().getGender());
    }

    @Test
    void deser_SimpleGeneric() {

        var mockResponse = "{\"age\":33,\"name\":\"mguerin\",\"_links\":{\"self\":{\"href\":\"http://local/person\"}}}";

        server.expect(requestTo("/person")).andRespond(withSuccess(mockResponse, MediaTypes.HAL_JSON));

        var response = template.exchange("/person", HttpMethod.GET, null, new ParameterizedTypeReference<HalResource<Person>>(){});

        assertNotNull(response.getBody());
        assertTrue(response.getBody().getLinks().hasLink(IanaLinkRelations.SELF));
        assertNotNull(response.getBody().getContent());
    }

}
