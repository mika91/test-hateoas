package org.example.hal.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Circle.class, name = "CIRCLE" ),
        @JsonSubTypes.Type(value = Square.class, name = "SQUARE" )
})
public abstract class Shape {
    private int x;
    private int y;
}
