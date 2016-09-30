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
package zoo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.NamedType;

import io.fares.bind.jackson.discovery.SubtypeDiscoveryModule;

public class ZooTest {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private ObjectMapper mapper;

    private Zoo zoo;

    @Before
    public void setupTest() {

        zoo = new SydneyZoo().addAnimal(new Tiger().withName("freddy")).addAnimal(new Tiger().withName("frieda"))
                .addAnimal(new Monkey().withName("baba"));

        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(SerializationFeature.WRAP_ROOT_VALUE);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Test
    public void visitSubtypeZoo() throws Exception {

        mapper.registerSubtypes(Tiger.class, Monkey.class);

        String zooString = mapper.writeValueAsString(zoo);

        log.info("\nSimple Subtype:\n{}", zooString);

        Zoo again = mapper.readValue(zooString, Zoo.class);

        assertEquals(zoo.getAnimals().size(), again.getAnimals().size());

    }

    @Test
    public void visitNamedTypeZoo() throws Exception {

        mapper.registerSubtypes(new NamedType(Tiger.class, "Tigger"), new NamedType(Monkey.class, "Ape"));

        String zooString = mapper.writeValueAsString(zoo);

        log.info("\nNamed Subtype:\n{}", zooString);

        Zoo again = mapper.readValue(zooString, Zoo.class);

        assertEquals(zoo.getAnimals().size(), again.getAnimals().size());

    }

    @Test
    public void visitSubTypeRegisteredZoo() throws Exception {

        mapper.registerModule(new SubtypeDiscoveryModule("zoo"));

        String zooString = mapper.writeValueAsString(zoo);

        log.info("\nDiscovery Module Subtype:\n{}", zooString);

        Zoo again = mapper.readValue(zooString, Zoo.class);

        assertEquals(zoo.getAnimals().size(), again.getAnimals().size());

    }

}
