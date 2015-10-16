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
import io.protostuff.ProtostuffException;
import io.protostuff.Schema;
import io.protostuff.StatefulOutput;

import static io.protostuff.runtime.RuntimeFieldFactory.ID_THROWABLE;
import static io.protostuff.runtime.RuntimeFieldFactory.STR_THROWABLE;

/**
 * Used when the type is assignable from {@link java.lang.Throwable}.
 *
 * @author David Yu
 */
public abstract class PolymorphicThrowableSchema extends PolymorphicSchema {

    static final java.lang.reflect.Field __cause;

    static {
        java.lang.reflect.Field cause;
        try {
            cause = Throwable.class.getDeclaredField("cause");
            cause.setAccessible(true);
        } catch (Exception e) {
            cause = null;
        }
        __cause = cause;
    }

    public PolymorphicThrowableSchema(IdStrategy strategy) {
        super(strategy);
    }

    static String name(int number) {
        return number == ID_THROWABLE ? STR_THROWABLE : null;
    }

    static int number(String name) {
        return name.length() == 1 && name.charAt(0) == 'Z' ? ID_THROWABLE : 0;
    }

    @SuppressWarnings("unchecked")
    static void writeObjectTo(Output output, Object value,
                              Schema<?> currentSchema, IdStrategy strategy) throws IOException {
        final Schema<Object> schema = strategy.writePojoIdTo(output,
                ID_THROWABLE, (Class<Object>) value.getClass()).getSchema();

        if (output instanceof StatefulOutput) {
            // update using the derived schema.
            ((StatefulOutput) output).updateLast(schema, currentSchema);
        }

        if (tryWriteWithoutCause(output, value, schema))
            return;

        schema.writeTo(output, value);
    }

    static boolean tryWriteWithoutCause(Output output, Object value,
                                        Schema<Object> schema) throws IOException {
        if (schema instanceof RuntimeSchema && __cause != null) {
            // ignore the field "cause" if its references itself (cyclic)
            final RuntimeSchema<Object> ms = (RuntimeSchema<Object>) schema;
            if (ms.getFieldCount() > 1 && ms.getFields().get(1).name.equals("cause")) {
                final Object cause;
                try {
                    cause = __cause.get(value);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                if (cause == value) {
                    // its cyclic, skip the second field "cause"
                    ms.getFields().get(0).writeTo(output, value);

                    for (int i = 2, len = ms.getFieldCount(); i < len; i++)
                        ms.getFields().get(i).writeTo(output, value);

                    return true;
                }
            }
        }

        return false;
    }

    static Object readObjectFrom(Input input, Schema<?> schema, Object owner,
                                 IdStrategy strategy) throws IOException {
        final int number = input.readFieldNumber(schema);
        if (number != ID_THROWABLE)
            throw new ProtostuffException("Corrupt input.");

        return readObjectFrom(input, schema, owner, strategy, number);
    }

    static Object readObjectFrom(Input input, Schema<?> schema, Object owner,
                                 IdStrategy strategy, int number) throws IOException {
        final Schema<Object> derivedSchema = strategy.resolvePojoFrom(input,
                number).getSchema();

        final Object pojo = derivedSchema.newMessage();

        if (input instanceof GraphInput) {
            // update the actual reference.
            ((GraphInput) input).updateLast(pojo, owner);
        }

        if (__cause != null) {
            final Object cause;
            try {
                cause = __cause.get(pojo);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            if (cause == null) {
                // was not written because it was cyclic
                // so we set it here manually for correctness
                try {
                    __cause.set(pojo, cause);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        derivedSchema.mergeFrom(input, pojo);
        return pojo;
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
        return Throwable.class.getName();
    }

    @Override
    public String messageName() {
        return Throwable.class.getSimpleName();
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
