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

import java.io.IOException;

import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Pipe;
import io.protostuff.Tag;
import io.protostuff.WireFormat;

/**
 * Represents a field of a message/pojo.
 */
public abstract class Field<T>
{
    public final WireFormat.FieldType type;
    public final int number;
    public final String name;
    public final boolean repeated;
    public final int groupFilter;

    // public final Tag tag;

    public Field(WireFormat.FieldType type, int number, String name, boolean repeated,
            Tag tag)
    {
        this.type = type;
        this.number = number;
        this.name = name;
        this.repeated = repeated;
        this.groupFilter = tag == null ? 0 : tag.groupFilter();
        // this.tag = tag;
    }

    public Field(WireFormat.FieldType type, int number, String name, Tag tag)
    {
        this(type, number, name, false, tag);
    }

    /**
     * Writes the value of a field to the {@code output}.
     */
    protected abstract void writeTo(Output output, T message)
            throws IOException;

    /**
     * Reads the field value into the {@code message}.
     */
    protected abstract void mergeFrom(Input input, T message)
            throws IOException;

    /**
     * Transfer the input field to the output field.
     */
    protected abstract void transfer(Pipe pipe, Input input, Output output,
            boolean repeated) throws IOException;
}
