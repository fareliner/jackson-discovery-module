/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.fares.bind.jackson.discovery;

import java.io.IOException;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.ClassAnnotationMatchProcessor;

/**
 * A dynamic id type resolver that can be used as a replacement for the build in
 * type resolvers. It requires the subtypes to be annotated with
 * {@link JsonTypeName}.
 *
 * @author Niels Bertram
 */
public class DynamicTypeResolver extends TypeIdResolverBase {

    private static FastClasspathScanner scanner;

    private static BiMap<String, Class<?>> typeMap = new BiMap<>();

    protected DynamicTypeResolver() {
        if (scanner == null) {
            scanner = new FastClasspathScanner();
        }
    }

    public static DynamicTypeResolver construct(MapperConfig<?> config, JavaType baseType,
                                                Collection<NamedType> subtypes, boolean forSer, boolean forDeser) {

        if (forSer == forDeser)
            throw new IllegalArgumentException(
                    "Cannot construct for both serilization and deserialization in same transaction.");

        // TODO configure scanner
        return new DynamicTypeResolver();
    }

    /**
     * If no name was explicitly given for a class, we will just use
     * non-qualified class name.
     *
     * @param clazz the class to be used for default id construction
     * @return the id to be used for this class
     */
    private static String constructDefaultTypeId(Class<?> clazz) {
        String n = clazz.getName();
        int ix = n.lastIndexOf('.');
        return (ix < 0) ? n : n.substring(ix + 1);
    }

    @Override
    public String idFromValue(Object value) {

        String id = null;

        JsonTypeName ann = value.getClass().getAnnotation(JsonTypeName.class);
        if (ann != null && ann.value() != null) {
            id = ann.value();
        } else {
            id = constructDefaultTypeId(value.getClass());
        }

        if (!typeMap.containsKey(id)) {
            typeMap.put(id, value.getClass());
        }

        return id;
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        return idFromValue(value);
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) throws IOException {

        if (!typeMap.containsKey(id)) {
            scanJsonTypeNames();
        }

        if (!typeMap.containsKey(id)) {
            throw new IllegalArgumentException("No subtype registered for id " + id);
        } else {
            Class<?> clazz = typeMap.get(id);
            return context.getTypeFactory().constructSimpleType(clazz, null);
        }

    }

    @Override
    public String getDescForKnownTypeIds() {

        StringBuilder sb = new StringBuilder("Known Types: [");
        boolean first = true;
        for (String key : typeMap.keySet()) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            sb.append(key).append("=").append(typeMap.get(key).getName());
        }

        sb.append(']');

        return sb.toString();
    }

    @Override
    public Id getMechanism() {
        return JsonTypeInfo.Id.NAME;
    }

    private void scanJsonTypeNames() {

        scanner.matchClassesWithAnnotation(JsonTypeName.class, new ClassAnnotationMatchProcessor() {

            @Override
            public void processMatch(Class<?> foundClazz) {
                JsonTypeName ann = foundClazz.getAnnotation(JsonTypeName.class);
                String foundId = ann.value() == null ? constructDefaultTypeId(foundClazz) : ann.value();
                // make sure neither key nor clazz is yet registered and if
                // registered under a different name, bail
                if (typeMap.containsValue(foundClazz) && !typeMap.containsKey(foundId)) {
                    throw new IllegalArgumentException("JSON sub type for class [" + foundClazz.getName()
                            + "] is already registered with id [" + typeMap.getKey(foundClazz)
                            + "] and cannot be registered with new id [" + foundId + "]");
                } else if (!typeMap.containsKey(foundId)) {
                    typeMap.put(foundId, foundClazz);
                }

            }
        });

        scanner.scan();

    }

}
