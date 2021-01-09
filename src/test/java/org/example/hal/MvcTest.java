package org.example.hal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.hal.model.ShapeResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

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

//            // for deserilization -> ADD a mixin to remove JsonUNwrapped annotation ?
//            objectMapper.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);
//            objectMapper.registerModule(new HalTypedResourceModule());

            return objectMapper;

        }

//        @Autowired
//        private ObjectMapper objectMapper;// created elsewhere

//        @Autowired(required = true)
//        public void configureJackson(ObjectMapper objectMapper) {
//
//        // default Jackson HAL module (_links, _embedded...)
//        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        objectMapper.registerModule(new Jackson2HalModule());
//
//        // HAL content polymorphism
//        objectMapper.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);
//        objectMapper.registerModule(new HalTypedResourceModule());
//        }

//        @Override
//        public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
//            int toot = 0;
////            // this won't add a 2nd MappingJackson2HttpMessageConverter
////            // as the SOLUTION 2 is doing but also might seem complicated
////            converters.stream().filter(c -> c instanceof MappingJackson2HttpMessageConverter).forEach(c -> {
////                // check default included objectMapper._registeredModuleTypes,
////                // e.g. Jdk8Module, JavaTimeModule when creating the ObjectMapper
////                // without Jackson2ObjectMapperBuilder
////                ((MappingJackson2HttpMessageConverter) c).setObjectMapper(this.objectMapper);
////            });
//        }

    }


    @Autowired
    RestTemplate template;
    MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        this.server = createServer(template);

    }

    private static final String HAL_USER = "\"firstname\" : \"Dave\", \"lastname\" : \"Matthews\"";
    private static final String RESOURCE_HAL = String.format("{ \"_links\" : { \"self\" : \"/resource\" }, %s }", HAL_USER);

    @Test
    void usesResourceTypeReferenceWithHal() {

        var mockResponse = "{\"@type\":\"CIRCLE\",\"x\":2,\"y\":3,\"radius\":5.6,\"extraField\":\"FOR TEST ONLY\",\"_links\":{\"self\":{\"href\":\"http://local/shape\"}}}";

        server.expect(requestTo("/shape")).andRespond(withSuccess(mockResponse, MediaTypes.HAL_JSON));

        var response = template.exchange("/shape", HttpMethod.GET, null, ShapeResource.class);

        int toto = 0;
    }

}
