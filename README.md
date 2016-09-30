# Subtype Discovery Module for Jackson FasterXML

If a class model contains a inheritance tree where sub types may not be known or cannot be added to the base class using the `@JsonSubTypes` annotation (e.g. spread out over multiple jar files), the object mapper will not be able to figure out what contrete class is required when deserialising JSON content. This module can be used to register unknown subtypes with a Jackson object mapper.

The concrete class just needs to have a `@JsonTypeName` annotation at the class level and the contrete or base class has to carry the `@JsonTypeInfo` annotation with appropriate configuration. Alternatively one can also register a mixin class with  

Basic use case:

```java
private ObjectMapper mapper;
mapper.registerModule(new SubtypeDiscoveryModule("zoo"));
  
// mapping activities
```

Please refer to the `zoo.ZooTest` for some examples on how to use sub typeing and this module.