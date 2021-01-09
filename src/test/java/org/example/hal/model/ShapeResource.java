package org.example.hal.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.HalResource;

@Data
@NoArgsConstructor // need for jackson deserialization
public class ShapeResource extends HalResource<Shape> {
    private String extraField = "FOR TEST ONLY";

    public ShapeResource(Shape shape){
        super(shape);
    }
}