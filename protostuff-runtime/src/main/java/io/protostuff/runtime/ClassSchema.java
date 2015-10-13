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

import io.protostuff.GraphInput;
import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Pipe;
import io.protostuff.ProtostuffException;
import io.protostuff.Schema;

import static io.protostuff.runtime.RuntimeFieldFactory.ID_CLASS;
import static io.protostuff.runtime.RuntimeFieldFactory.ID_CLASS_ARRAY;
import static io.protostuff.runtime.RuntimeFieldFactory.ID_CLASS_ARRAY_MAPPED;
import static io.protostuff.runtime.RuntimeFieldFactory.ID_CLASS_MAPPED;
import static io.protostuff.runtime.RuntimeFieldFactory.STR_CLASS;
import static io.protostuff.runtime.RuntimeFieldFactory.STR_CLASS_ARRAY;
import static io.protostuff.runtime.RuntimeFieldFactory.STR_CLASS_ARRAY_MAPPED;
import static io.protostuff.runtime.RuntimeFieldFactory.STR_CLASS_MAPPED;

/**
 * Used when a field is declared as {@code Class<?>} (with or with-out generics).
 *
 * @author David Yu
 */
public abstract class ClassSchema extends PolymorphicSchema {

    static final int ID_ARRAY_DIMENSION = 2;
    static final String STR_ARRAY_DIMENSION = "b";
    protected final Pipe.Schema<Object> pipeSchema = new Pipe.Schema<Object>(
            this) {
        @Override
        protected void transfer(Pipe pipe, Input input, Output output)
                throws IOException {
            transferObject(this, pipe, input, output, strategy);
        }
    };

    public ClassSchema(IdStrategy strategy) {
        super(strategy);
    }

    static String name(int number) {
        switch (number) {
            case ID_ARRAY_DIMENSION:
                return STR_ARRAY_DIMENSION;
            case ID_CLASS:
                return STR_CLASS;
            case ID_CLASS_MAPPED:
                return STR_CLASS_MAPPED;
            case ID_CLASS_ARRAY:
                return STR_CLASS_ARRAY;
            case ID_CLASS_ARRAY_MAPPED:
                return STR_CLASS_ARRAY_MAPPED;
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
            case 'r':
                return ID_CLASS;
            case 's':
                return ID_CLASS_MAPPED;
            case 't':
                return ID_CLASS_ARRAY;
            case 'u':
                return ID_CLASS_ARRAY_MAPPED;
            default:
                return 0;
        }
    }

    static void writeObjectTo(Output output, Object value,
                              Schema<?> currentSchema, IdStrategy strategy) throws IOException {
        final Class<?> c = ((Class<?>) value);
        if (c.isArray()) {
            int dimensions = 1;
            Class<?> componentType = c.getComponentType();
            while (componentType.isArray()) {
                dimensions++;
                componentType = componentType.getComponentType();
            }

            strategy.writeClassIdTo(output, componentType, true);
            // write the dimensions of the array
            output.writeUInt32(ID_ARRAY_DIMENSION, dimensions, false);
            return;
        }

        strategy.writeClassIdTo(output, c, false);
    }

    static Object readObjectFrom(Input input, Schema<?> schema, Object owner,
                                 IdStrategy strategy) throws IOException {
        final int number = input.readFieldNumber(schema);
        final Object value;
        switch (number) {
            case ID_CLASS:
                value = strategy.resolveClassFrom(input, false, false);
                break;

            case ID_CLASS_MAPPED:
                value = strategy.resolveClassFrom(input, true, false);
                break;

            case ID_CLASS_ARRAY:
                value = ObjectSchema.getArrayClass(input, schema,
                        strategy.resolveClassFrom(input, false, true));
                break;

            case ID_CLASS_ARRAY_MAPPED:
                value = ObjectSchema.getArrayClass(input, schema,
                        strategy.resolveClassFrom(input, true, true));
                break;

            default:
                throw new ProtostuffException("Corrupt input.");
        }

        if (input instanceof GraphInput) {
            // update the actual reference.
            ((GraphInput) input).updateLast(value, owner);
        }

        if (0 != input.readFieldNumber(schema))
            throw new ProtostuffException("Corrupt input.");

        return value;
    }

    static void transferObject(Pipe.Schema<Object> pipeSchema, Pipe pipe,
                               Input input, Output output, IdStrategy strategy) throws IOException {
        final int number = input.readFieldNumber(pipeSchema.wrappedSchema);
        switch (number) {
            case ID_CLASS:
                ObjectSchema.transferClass(pipe, input, output, number, pipeSchema,
                        false, false, strategy);
                break;

            case ID_CLASS_MAPPED:
                ObjectSchema.transferClass(pipe, input, output, number, pipeSchema,
                        true, false, strategy);
                break;

            case ID_CLASS_ARRAY:
                ObjectSchema.transferClass(pipe, input, output, number, pipeSchema,
                        false, true, strategy);
                break;

            case ID_CLASS_ARRAY_MAPPED:
                ObjectSchema.transferClass(pipe, input, output, number, pipeSchema,
                        true, true, strategy);
                break;

            default:
                throw new ProtostuffException("Corrupt input.");
        }

        if (0 != input.readFieldNumber(pipeSchema.wrappedSchema))
            throw new ProtostuffException("Corrupt input.");
    }

    @Override
    public Pipe.Schema<Object> getPipeSchema() {
        return pipeSchema;
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
        return Class.class.getName();
    }

    @Override
    public String messageName() {
        return Class.class.getSimpleName();
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
