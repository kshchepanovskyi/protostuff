/**
 * Copyright (C) 2007-2015 Protostuff
 * http://www.protostuff.io/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.protostuff.runtime;

import io.protostuff.Pipe;
import io.protostuff.Schema;
import io.protostuff.runtime.PolymorphicSchema.Handler;

/**
 * Wraps a schema.
 */
public abstract class HasSchema<T> implements PolymorphicSchema.Factory
{

    /**
     * Gets the schema.
     */
    public abstract Schema<T> getSchema();

    /**
     * Gets the pipe schema.
     */
    public abstract Pipe.Schema<T> getPipeSchema();

    // for the array of this type

    @SuppressWarnings("unchecked")
    public final ArraySchemas.Base genericElementSchema = new ArraySchemas.PojoArray(
            null, (HasSchema<Object>) this);

    @Override
    @SuppressWarnings("unchecked")
    public PolymorphicSchema newSchema(Class<?> typeClass, IdStrategy strategy,
            Handler handler)
    {
        return new ArraySchemas.PojoArray(handler, (HasSchema<Object>) this);
    }

}