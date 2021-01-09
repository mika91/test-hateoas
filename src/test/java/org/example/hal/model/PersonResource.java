package org.example.hal.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.HalResource;

@Data
@NoArgsConstructor // need for jackson deserialization
public class PersonResource extends HalResource<Person> {
    private String gender = "male";
    public PersonResource(Person person){
        super(person);
    }
}