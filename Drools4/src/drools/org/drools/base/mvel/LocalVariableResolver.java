/**
 * MVEL (The MVFLEX Expression Language)
 *
 * Copyright (C) 2007 Christopher Brock, MVFLEX/Valhalla Project and the Codehaus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.drools.base.mvel;

import org.mvel.CompileException;
import org.mvel.DataConversion;
import org.mvel.integration.VariableResolver;

import java.util.Map;

public class LocalVariableResolver implements VariableResolver {
    private String name;
    private Class knownType;
    private DroolsMVELFactory factory;

    private boolean cache = false;

    public LocalVariableResolver(DroolsMVELFactory factory, String name) {
        this.factory = factory;
        this.name = name;
    }

    public LocalVariableResolver(DroolsMVELFactory factory, String name, Class knownType) {
        this.name = name;
        this.knownType = knownType;
        this.factory = factory;
    }

    public LocalVariableResolver(DroolsMVELFactory factory, String name, boolean cache) {
        this.factory = factory;
        this.name = name;
        this.cache = cache;
    }

    public LocalVariableResolver(DroolsMVELFactory factory, String name, Class knownType, boolean cache) {
        this.name = name;
        this.knownType = knownType;
        this.factory = factory;
        this.cache = cache;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setStaticType(Class knownType) {
        this.knownType = knownType;
    }

    public String getName() {
        return name;
    }

    public Class getType() {
        return knownType;
    }

    public void setValue(Object value) {
        if (knownType != null && value != null && value.getClass() != knownType) {
            if (!DataConversion.canConvert(knownType, value.getClass())) {
                throw new CompileException("cannot assign " + value.getClass().getName() + " to type: "
                        + knownType.getName());
            }
            try {
                value = DataConversion.convert(value, knownType);
            }
            catch (Exception e) {
                throw new CompileException("cannot convert value of " + value.getClass().getName()
                        + " to: " + knownType.getName());
            }
        }
        
        this.factory.setLocalValue( this.name, value );
    }

    public Object getValue() {
        return this.factory.getLocalValue( this.name );
    }

    public int getFlags() {
        return 0;
    }


    public boolean isCache() {
        return cache;
    }

    public void setCache(boolean cache) {
        this.cache = cache;
    }
}
