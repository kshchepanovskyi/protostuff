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

import java.io.IOException;
import java.lang.reflect.Array;

import io.protostuff.GraphInput;
import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.ProtostuffException;
import io.protostuff.Schema;
import io.protostuff.StatefulOutput;
import io.protostuff.runtime.ObjectSchema.ArrayWrapper;

import static io.protostuff.runtime.RuntimeFieldFactory.ID_ARRAY;
import static io.protostuff.runtime.RuntimeFieldFactory.ID_ARRAY_MAPPED;
import static io.protostuff.runtime.RuntimeFieldFactory.STR_ARRAY;
import static io.protostuff.runtime.RuntimeFieldFactory.STR_ARRAY_MAPPED;

/**
 * Used when a field is an array (Object[] or any polymorphic component type).
 *
 * @author David Yu
 */
public abstract class ArraySchema extends PolymorphicSchema {

    static final int ID_ARRAY_LEN = 3;
    static final int ID_ARRAY_DIMENSION = 2;

    static final String STR_ARRAY_LEN = "c";
    static final String STR_ARRAY_DIMENSION = "b";

    public ArraySchema(IdStrategy strategy) {
        super(strategy);
    }

    static String name(int number) {
        switch (number) {
            case ID_ARRAY_DIMENSION:
                return STR_ARRAY_DIMENSION;
            case ID_ARRAY_LEN:
                return STR_ARRAY_LEN;
            case ID_ARRAY:
                return STR_ARRAY;
            case ID_ARRAY_MAPPED:
                return STR_ARRAY_MAPPED;
            default:
                return null;
        }
    }

    static int number(String name) {
        if (name.length() != 1)
            return 0;

        switch (name.charAt(0)) {
            case 'b':
                return ID_ARRAY_DIMENSION;
            case 'c':
                return ID_ARRAY_LEN;
            case 'o':
                return ID_ARRAY;
            case 'q':
                return ID_ARRAY_MAPPED;
            default:
                return 0;
        }
    }

    static void writeObjectTo(Output output, Object value,
                              Schema<?> currentSchema, IdStrategy strategy) throws IOException {
        final Class<?> clazz = value.getClass();
        int dimensions = 1;
        Class<?> componentType = clazz.getComponentType();
        while (componentType.isArray()) {
            dimensions++;
            componentType = componentType.getComponentType();
        }

        strategy.writeArrayIdTo(output, componentType);
        // write the length of the array
        output.writeUInt32(ID_ARRAY_LEN, Array.getLength(value), false);
        // write the dimensions of the array
        output.writeUInt32(ID_ARRAY_DIMENSION, dimensions, false);

        if (output instanceof StatefulOutput) {
            // update using the derived schema.
            ((StatefulOutput) output).updateLast(strategy.ARRAY_SCHEMA,
                    currentSchema);
        }

        strategy.ARRAY_SCHEMA.writeTo(output, value);
    }

    static Object readObjectFrom(Input input, Schema<?> schema, Object owner,
                                 IdStrategy strategy) throws IOException {
        final int number = input.readFieldNumber(schema);
        final boolean mapped;
        switch (number) {
            case ID_ARRAY:
                mapped = false;
                break;

            case ID_ARRAY_MAPPED:
                mapped = true;
                break;

            default:
                throw new ProtostuffException("Corrupt input.");
        }

        final ArrayWrapper mArrayWrapper = ObjectSchema.newArrayWrapper(input,
                schema, mapped, strategy);

        if (input instanceof GraphInput) {
            // update the actual reference.
            ((GraphInput) input).updateLast(mArrayWrapper.array, owner);
        }

        strategy.COLLECTION_SCHEMA.mergeFrom(input, mArrayWrapper);

        return mArrayWrapper.array;
    }

    @Override
    public String getFieldName(int number) {
        return name(number);
    }

    @Override
    public int getFieldNumber(String name) {
        return number(name);
    }

    @Override
    public String messageFullName() {
        return Array.class.getName();
    }

    @Override
    public String messageName() {
        return Array.class.getSimpleName();
    }

    @Override
    public void mergeFrom(Input input, Object owner) throws IOException {
        setValue(readObjectFrom(input, this, owner, strategy), owner);
    }

    @Override
    public void writeTo(Output output, Object value) throws IOException {
        writeObjectTo(output, value, this, strategy);
    }

}
