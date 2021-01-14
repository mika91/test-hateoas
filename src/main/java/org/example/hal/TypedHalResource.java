package org.example.hal;

import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@NoArgsConstructor
public class TypedHalResource<T> extends HalResource<T> {

    public TypedHalResource(@Nullable T content) {
       super(content);
    }
}
