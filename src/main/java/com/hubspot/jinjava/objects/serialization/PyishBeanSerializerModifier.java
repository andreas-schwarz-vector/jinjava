package com.hubspot.jinjava.objects.serialization;

import com.google.common.annotations.Beta;
import java.util.Map;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.ValueSerializerModifier;
import tools.jackson.databind.ser.bean.BeanSerializerBase;

@Beta
public class PyishBeanSerializerModifier extends ValueSerializerModifier {

  public static final PyishBeanSerializerModifier INSTANCE =
    new PyishBeanSerializerModifier();

  private PyishBeanSerializerModifier() {}

  @Override
  public ValueSerializer<?> modifySerializer(
    SerializationConfig config,
    BeanDescription.Supplier beanDesc,
    ValueSerializer<?> serializer
  ) {
    // Use the PyishSerializer if it extends the PyishSerializable class.
    // For example, a Map implementation could then have custom string serialization.
    if (!(PyishSerializable.class.isAssignableFrom(beanDesc.getBeanClass()))) {
      if (Map.Entry.class.isAssignableFrom(beanDesc.getBeanClass())) {
        return MapEntrySerializer.INSTANCE;
      }
      if (serializer instanceof BeanSerializerBase) {
        return BothCasingBeanSerializer.wrapping(serializer);
      }
      return serializer;
    } else {
      return PyishSerializer.INSTANCE;
    }
  }
}
