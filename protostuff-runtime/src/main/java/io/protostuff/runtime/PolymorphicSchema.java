/**
 * Copyright (C) 2007-2015 Protostuff http://www.protostuff.io/
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.protostuff.runtime;

import io.protostuff.Pipe;
import io.protostuff.Schema;

/**
 * Used when the type is either polymorphic or too complex. Unlike DerivativeSchema, this is designed to have no concept
 * of merging.
 *
 * @author David Yu
 */
public abstract class PolymorphicSchema implements Schema<Object> {

    public final IdStrategy strategy;

    public PolymorphicSchema(IdStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public Object newMessage() {
        // cannot instantiate because the type is dynamic.
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<? super Object> typeClass() {
        return Object.class;
    }

    /**
     * The pipe schema associated with this schema.
     */
    public abstract Pipe.Schema<Object> getPipeSchema();

    /**
     * Set the value to the owner.
     */
    protected abstract void setValue(Object value, Object owner);

    /**
     * The handler who's job is to set the value to the owner.
     */
    public interface Handler {
        void setValue(Object value, Object owner);
    }

    /**
     * A factory which creates a schema with the handler connected to it.
     */
    public interface Factory {
        PolymorphicSchema newSchema(Class<?> typeClass,
                                    IdStrategy strategy, Handler handler);
    }
}
