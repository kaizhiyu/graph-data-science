/*
 * Copyright (c) 2017-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.graphalgo.core;

import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.compat.MapUtil;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcedureConfigurationTest {

    private final String testUsername = "test";

    @Test
    void useDefault() {
        Map<String, Object> map = Collections.emptyMap();
        ProcedureConfiguration procedureConfiguration = ProcedureConfiguration.create(map, testUsername);
        String value = procedureConfiguration.get("partitionProperty", "defaultValue");
        assertEquals("defaultValue", value);
        assertEquals(testUsername, procedureConfiguration.username());
    }

    @Test
    void returnValueIfPresent() {
        Map<String, Object> map = MapUtil.map("partitionProperty", "partition");
        ProcedureConfiguration procedureConfiguration = ProcedureConfiguration.create(map, testUsername);
        String value = procedureConfiguration.get("partitionProperty", "defaultValue");
        assertEquals("partition", value);
    }

    @Test
    void newKeyIfPresent() {
        Map<String, Object> map = MapUtil.map("partitionProperty", "old", "writeProperty", "new");
        ProcedureConfiguration procedureConfiguration = ProcedureConfiguration.create(map, testUsername);
        String value = procedureConfiguration.get("writeProperty", "partitionProperty", "defaultValue");
        assertEquals("new", value);
    }

    @Test
    void oldKeyIfNewKeyNotPresent() {
        Map<String, Object> map = MapUtil.map("partitionProperty", "old");
        ProcedureConfiguration procedureConfiguration = ProcedureConfiguration.create(map, testUsername);
        String value = procedureConfiguration.get("writeProperty", "partitionProperty", "defaultValue");
        assertEquals("old", value);
    }

    @Test
    void defaultIfNoKeysPresent() {
        Map<String, Object> map = Collections.emptyMap();
        ProcedureConfiguration procedureConfiguration = ProcedureConfiguration.create(map, testUsername);
        String value = procedureConfiguration.get("writeProperty", "partitionProperty", "defaultValue");
        assertEquals("defaultValue", value);
    }

    @Test
    void defaultIfKeyMissing() {
        Map<String, Object> map = Collections.emptyMap();
        ProcedureConfiguration procedureConfiguration = ProcedureConfiguration.create(map, testUsername);
        assertEquals("defaultValue", procedureConfiguration.getString("writeProperty", "defaultValue"));
    }

    @Test
    void defaultIfKeyPresentButNoValue() {
        Map<String, Object> map = MapUtil.map("writeProperty", "");
        ProcedureConfiguration procedureConfiguration = ProcedureConfiguration.create(map, testUsername);
        assertEquals("defaultValue", procedureConfiguration.getString("writeProperty", "defaultValue"));
    }

    @Test
    void valueIfKeyPresent() {
        Map<String, Object> map = MapUtil.map("writeProperty", "scc");
        ProcedureConfiguration procedureConfiguration = ProcedureConfiguration.create(map, testUsername);
        assertEquals("scc", procedureConfiguration.getString("writeProperty", "defaultValue"));
    }

}
