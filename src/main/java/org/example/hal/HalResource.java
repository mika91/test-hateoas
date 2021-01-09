package org.example.hal;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.lang.Nullable;

@Data
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
