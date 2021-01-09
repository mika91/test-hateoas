package org.example.hal.mvc;

import org.example.hal.model.Circle;
import org.example.hal.model.Person;
import org.example.hal.model.PersonResource;
import org.example.hal.model.ShapeResource;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DummyController {

    @GetMapping(path = "/person")
    public ResponseEntity<PersonResource> getPerson() {

        // instantiate
        var pojo = new Person();
        pojo.setAge(33);
        pojo.setName("mguerin");

        // resource
        var resource = new PersonResource(pojo);
        resource.add(Link.of("http://local/person", IanaLinkRelations.SELF));


        return ResponseEntity.ok(resource);
    }

    @GetMapping(path = "/shape")
    public ResponseEntity<ShapeResource> getShape() {

        // instantiate circle
        var circle = new Circle();
        circle.setX(2);
        circle.setY(3);
        circle.setRadius(5.6f);

        // resource
        var resource = new ShapeResource(circle);
        resource.add(Link.of("http://local/shape", IanaLinkRelations.SELF));


        return ResponseEntity.ok(resource);
    }
}
