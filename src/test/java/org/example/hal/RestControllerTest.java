package org.example.hal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.hal.model.PersonResource;
import org.example.hal.model.ShapeResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


// see: https://stackoverflow.com/questions/27494779/cannot-do-haljson-level-3-restful-api-with-spring-hateoas-due-to-lack-of-clarit
// see: https://tech.asimio.net/2020/04/06/Adding-HAL-Hypermedia-to-Spring-Boot-2-applications-using-Spring-HATEOAS.html

@SpringBootTest(classes = SpringTestApplication.class)
@AutoConfigureMockMvc
public class RestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;


    // demonstate : JSON with spring.hateoas.useHalAsDefaultJsonMediaType =  false
    @Test
    public void getSimple_Json() throws Exception {
        var response = mvc.perform(MockMvcRequestBuilders.get("/api/person").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("{\"age\":33,\"name\":\"mguerin\",\"gender\":\"male\",\"links\":[{\"rel\":\"self\",\"href\":\"http://local/person\"}]}")))
                .andReturn().getResponse().getContentAsString();

        var resource = objectMapper.readValue(response, PersonResource.class);
        assertNotNull(resource);
        assertNotNull(resource.getContent());
        assertTrue(resource.hasLink(IanaLinkRelations.SELF));
    }

    // dmeonstate HAL+JSON
    @Test
    public void getSimple_HalJson() throws Exception {
        var response = mvc.perform(MockMvcRequestBuilders.get("/api/person").accept("application/hal+json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("{\"age\":33,\"name\":\"mguerin\",\"gender\":\"male\",\"_links\":{\"self\":{\"href\":\"http://local/person\"}}}")))
                .andReturn().getResponse().getContentAsString();

        var resource = objectMapper.readValue(response, PersonResource.class);
        assertNotNull(resource);
        assertNotNull(resource.getContent());
        assertTrue(resource.hasLink(IanaLinkRelations.SELF));
    }


    @Test
    public void getShape_Json() throws Exception {
        var response = mvc.perform(MockMvcRequestBuilders.get("/api/shape").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("{\"@type\":\"CIRCLE\",\"x\":2,\"y\":3,\"radius\":5.6,\"extraField\":\"FOR TEST ONLY\",\"links\":[{\"rel\":\"self\",\"href\":\"http://local/shape\"}]}")))
                .andReturn().getResponse().getContentAsString();

        var resource = objectMapper.readValue(response, ShapeResource.class);
        assertNotNull(resource);
        assertNotNull(resource.getContent());
        assertTrue(resource.hasLink(IanaLinkRelations.SELF));
    }

    @Test
    public void getShape_HalJson() throws Exception {
        var response = mvc.perform(MockMvcRequestBuilders.get("/api/shape").accept("application/hal+json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("{\"@type\":\"CIRCLE\",\"x\":2,\"y\":3,\"radius\":5.6,\"extraField\":\"FOR TEST ONLY\",\"_links\":{\"self\":{\"href\":\"http://local/shape\"}}}")))
                .andReturn().getResponse().getContentAsString();

        var resource = objectMapper.readValue(response, ShapeResource.class);
        assertNotNull(resource);
        assertNotNull(resource.getContent());
        assertTrue(resource.hasLink(IanaLinkRelations.SELF));
    }


}