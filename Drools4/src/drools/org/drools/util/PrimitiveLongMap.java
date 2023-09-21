package org.drools.util;

/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.Serializable;

/**
 * @author Mark Proctor
 */
public class PrimitiveLongMap
    implements
    Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 400L;

    private final LongMap<Object> longMap;

    public PrimitiveLongMap() {
        this(32,
            8);
    }

    public PrimitiveLongMap(final int tableSize) {
        this(tableSize,
            8);
    }

    public PrimitiveLongMap(final int tableSize,
                            final int indexIntervals) {
        longMap = new LongMap<>();
    }

    public Object get(long id) {
        return longMap.get(id);
    }

    public Object remove(long id) {
        Object result = get(id);
        if (result != null) {
            longMap.clearId(id);
        }

        return result;
    }

    public void put(long id, Object set) {
        longMap.put(id, set);
    }

    public void clear() {
        longMap.clear();
    }

    public int size() {
        return longMap.size();
    }

    public long getNext(long index) {
        return 0;
    }

    public boolean isEmpty() {
        return longMap.size() == 0;
    }
}