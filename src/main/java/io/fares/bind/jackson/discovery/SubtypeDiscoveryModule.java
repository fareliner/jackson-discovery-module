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

import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.ClassAnnotationMatchProcessor;

/**
 * This Jackson module will scan the classpath for {@link JsonTypeName}
 * annotated classes and register these with the object mapper.
 *
 * @author Niels Bertram
 */
public class SubtypeDiscoveryModule extends SimpleModule {

    public final static Version VERSION;
    private static final long serialVersionUID = -6261683958278596260L;

    static {
        ResourceBundle bundle = ResourceBundle.getBundle(SubtypeDiscoveryModule.class.getName());
        VERSION = VersionUtil.parseVersion(bundle.getString("version"), "io.fares.bind.jackson.discovery",
                "dynamic-subtype-discovery");
    }

    private final Logger log = LoggerFactory.getLogger(SubtypeDiscoveryModule.class);

    private BiMap<String, Class<?>> typeMap = new BiMap<>();

    private FastClasspathScanner scanner;

    public SubtypeDiscoveryModule() {
        super(VERSION);
        scanner = new FastClasspathScanner();

    }

    public SubtypeDiscoveryModule(String... scanSpecs) {
        super(VERSION);
        scanner = new FastClasspathScanner(scanSpecs);
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
    public void setupModule(SetupContext context) {

        // find all classes annotated with JSON type name
        scanner.matchClassesWithAnnotation(JsonTypeName.class, new ClassAnnotationMatchProcessor() {

            @Override
            public void processMatch(Class<?> foundClazz) {

                JsonTypeName ann = foundClazz.getAnnotation(JsonTypeName.class);
                String foundId = ann.value() == null ? constructDefaultTypeId(foundClazz) : ann.value();

                if (log.isDebugEnabled()) {
                    log.debug("found id[{}] class[{}]", foundId, foundClazz.getName());
                }

                // make sure neither key nor clazz is yet registered and
                // if registered under a different name, fail
                if (typeMap.containsValue(foundClazz) && !typeMap.containsKey(foundId)) {
                    throw new IllegalArgumentException("JSON sub type for class [" + foundClazz.getName()
                            + "] is already registered with id [" + typeMap.getKey(foundClazz)
                            + "] and cannot be registered with new id [" + foundId + "]");
                } else if (!typeMap.containsKey(foundId)) {
                    typeMap.put(foundId, foundClazz);
                }

                registerSubtypes(new NamedType(foundClazz, foundId));

            }

        });

        scanner.scan();

        super.setupModule(context);

    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

}
