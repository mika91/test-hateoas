package org.example.serializer;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.example.deserializer.HalTypedResourceDeserializerModifier;

public class HalTypedResourceModule extends SimpleModule {

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        // TODO: must be add at the end of other modifiers
        context.addBeanSerializerModifier(new HalTypedResourceSerializerModifier());
        context.addBeanDeserializerModifier(new HalTypedResourceDeserializerModifier());
    }

}
