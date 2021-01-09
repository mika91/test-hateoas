package org.example;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.lang.Nullable;

@Data
//@JsonSerialize(using = HalResourceSerializer.class)
//@JsonDeserialize(using = SinglePolyUnwrappedDeserializer.class)
@NoArgsConstructor
public class HalResource<T> extends RepresentationModel<HalResource<T>> {

    @Getter
    @Nullable
    @JsonUnwrapped
    private T content;



    public HalResource(@Nullable T content) {
        this.content = content;
    }
}
