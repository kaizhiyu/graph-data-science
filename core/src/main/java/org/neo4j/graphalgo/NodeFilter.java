/*
 * Copyright (c) 2017-2019 "Neo4j,"
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

package org.neo4j.graphalgo;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class NodeFilter extends EntityFilter {

    private static final NodeFilter EMPTY = new NodeFilter("", PropertyMappings.EMPTY);
    private static final String LABEL_KEY = "label";

    public final String label;

    private NodeFilter(String label, PropertyMappings properties) {
        super(properties);
        this.label = label;
    }

    public static NodeFilter of(@Nullable String label) {
        if (StringUtils.isEmpty(label)) {
            return EMPTY;
        }
        return new NodeFilter(label, PropertyMappings.EMPTY);
    }

    public static NodeFilter of(Map<String, Object> map, ElementIdentifier identifier) {
        String label = map.containsKey(LABEL_KEY)? nonEmptyString(map, LABEL_KEY) : identifier.name;
        return create(map, properties -> new NodeFilter(label, properties));
    }

    public static NodeFilter fromObject(Object object, ElementIdentifier identifier) {
        if (object instanceof String) {
            return of((String) object);
        }
        if (object instanceof Map) {
            @SuppressWarnings("unchecked") Map<String, Object> map = (Map) object;
            return of(map, identifier);
        }
        throw new IllegalArgumentException(String.format(
            "Cannot construct a node filter out of a %s",
            object.getClass().getName()
        ));
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    @Override
    void writeToObject(Map<String, Object> value) {
        value.put(LABEL_KEY, label);
    }

    @Override
    public NodeFilter withAdditionalPropertyMappings(PropertyMappings mappings) {
        PropertyMappings newMappings = properties().mergeWith(mappings);
        if (newMappings == properties()) {
            return this;
        }
        return new NodeFilter(label, newMappings);
    }

    public static NodeFilter empty() {
        return EMPTY;
    }
}