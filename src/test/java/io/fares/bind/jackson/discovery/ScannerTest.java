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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonTypeName;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.ClassAnnotationMatchProcessor;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.SubclassMatchProcessor;
import zoo.Animal;

public class ScannerTest {

    Logger log = LoggerFactory.getLogger(ScannerTest.class);

    @Test
    public void testSubclassScan() {

        FastClasspathScanner s = new FastClasspathScanner("zoo");

        final List<Class<?>> found = new ArrayList<>();

        s.matchSubclassesOf(Animal.class, new SubclassMatchProcessor<Animal>() {
            @Override
            public void processMatch(Class<? extends Animal> foundClass) {
                JsonTypeName idAnnotation = foundClass.getAnnotation(JsonTypeName.class);
                log.info("found {} as {}", (idAnnotation == null ? null : idAnnotation.value()), foundClass.getName());
                found.add(foundClass);
            }
        });

        s.scan();

        assertEquals(2, found.size());

    }

    @Test
    public void testAnnotationScan() {

        final List<Class<?>> found = new ArrayList<>();

        FastClasspathScanner s = new FastClasspathScanner("zoo");
        s.matchClassesWithAnnotation(JsonTypeName.class, new ClassAnnotationMatchProcessor() {

            @Override
            public void processMatch(Class<?> foundClass) {
                log.info("found {}", foundClass.getName());
                JsonTypeName jsonType = foundClass.getAnnotation(JsonTypeName.class);
                log.info("  {}", jsonType == null ? null : jsonType.value());
                found.add(foundClass);
            }
        });

        s.scan();

        assertEquals(3, found.size());

    }

}
