package org.example.hal;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor
public class HalResource<T> extends EntityModel<T> {

    @Override
    @Nullable
    @JsonUnwrapped
    @JsonSerialize(using = JsonSerializer.None.class) // f**k off buggy MapSuppressingUnwrappingSerializer
    public T getContent() {
        return super.getContent();
    }

    public HalResource(@Nullable T content) {
      super(content);
    }
}
