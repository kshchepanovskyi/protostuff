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
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;

import io.protostuff.GraphInput;
import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.ProtostuffException;
import io.protostuff.Schema;
import io.protostuff.StatefulOutput;
import io.protostuff.runtime.IdStrategy.Wrapper;
import io.protostuff.runtime.RuntimeEnv.Instantiator;

import static io.protostuff.runtime.RuntimeFieldFactory.ID_ENUM_MAP;
import static io.protostuff.runtime.RuntimeFieldFactory.ID_MAP;
import static io.protostuff.runtime.RuntimeFieldFactory.STR_ENUM_MAP;
import static io.protostuff.runtime.RuntimeFieldFactory.STR_MAP;

/**
 * Used when the type is an interface (Map/SortedMap).
 *
 * @author David Yu
 */
public abstract class PolymorphicMapSchema extends PolymorphicSchema {

    static final int ID_EMPTY_MAP = 1, ID_SINGLETON_MAP = 2,
            ID_UNMODIFIABLE_MAP = 3, ID_UNMODIFIABLE_SORTED_MAP = 4,
            ID_SYNCHRONIZED_MAP = 5, ID_SYNCHRONIZED_SORTED_MAP = 6,
            ID_CHECKED_MAP = 7, ID_CHECKED_SORTED_MAP = 8;

    static final String STR_EMPTY_MAP = "a", STR_SINGLETON_MAP = "b",
            STR_UNMODIFIABLE_MAP = "c", STR_UNMODIFIABLE_SORTED_MAP = "d",
            STR_SYNCHRONIZED_MAP = "e", STR_SYNCHRONIZED_SORTED_MAP = "f",
            STR_CHECKED_MAP = "g", STR_CHECKED_SORTED_MAP = "h";

    static final IdentityHashMap<Class<?>, Integer> __nonPublicMaps = new IdentityHashMap<>();

    static final Field fSingletonMap_k, fSingletonMap_v,

    fUnmodifiableMap_m,

    fUnmodifiableSortedMap_sm,

    fSynchronizedMap_m,

    fSynchronizedSortedMap_sm,

    fSynchronizedMap_mutex,

    fCheckedMap_m, fCheckedSortedMap_sm, fCheckedMap_keyType,
            fCheckedMap_valueType;

    static final Instantiator<?> iSingletonMap,

    iUnmodifiableMap, iUnmodifiableSortedMap,

    iSynchronizedMap, iSynchronizedSortedMap,

    iCheckedMap, iCheckedSortedMap;

    static {
        map("java.util.Collections$EmptyMap", ID_EMPTY_MAP);

        Class<?> cSingletonMap = map("java.util.Collections$SingletonMap",
                ID_SINGLETON_MAP);

        Class<?> cUnmodifiableMap = map(
                "java.util.Collections$UnmodifiableMap", ID_UNMODIFIABLE_MAP);

        Class<?> cUnmodifiableSortedMap = map(
                "java.util.Collections$UnmodifiableSortedMap",
                ID_UNMODIFIABLE_SORTED_MAP);

        Class<?> cSynchronizedMap = map(
                "java.util.Collections$SynchronizedMap", ID_SYNCHRONIZED_MAP);

        Class<?> cSynchronizedSortedMap = map(
                "java.util.Collections$SynchronizedSortedMap",
                ID_SYNCHRONIZED_SORTED_MAP);

        Class<?> cCheckedMap = map("java.util.Collections$CheckedMap",
                ID_CHECKED_MAP);

        Class<?> cCheckedSortedMap = map(
                "java.util.Collections$CheckedSortedMap", ID_CHECKED_SORTED_MAP);

        try {
            fSingletonMap_k = cSingletonMap.getDeclaredField("k");
            fSingletonMap_v = cSingletonMap.getDeclaredField("v");

            fUnmodifiableMap_m = cUnmodifiableMap.getDeclaredField("m");
            fUnmodifiableSortedMap_sm = cUnmodifiableSortedMap
                    .getDeclaredField("sm");

            fSynchronizedMap_m = cSynchronizedMap.getDeclaredField("m");
            fSynchronizedSortedMap_sm = cSynchronizedSortedMap
                    .getDeclaredField("sm");
            fSynchronizedMap_mutex = cSynchronizedMap.getDeclaredField("mutex");

            fCheckedMap_m = cCheckedMap.getDeclaredField("m");
            fCheckedSortedMap_sm = cCheckedSortedMap.getDeclaredField("sm");
            fCheckedMap_keyType = cCheckedMap.getDeclaredField("keyType");
            fCheckedMap_valueType = cCheckedMap.getDeclaredField("valueType");

            iSingletonMap = RuntimeEnv.newInstantiator(cSingletonMap);

            iUnmodifiableMap = RuntimeEnv.newInstantiator(cUnmodifiableMap);
            iUnmodifiableSortedMap = RuntimeEnv
                    .newInstantiator(cUnmodifiableSortedMap);

            iSynchronizedMap = RuntimeEnv.newInstantiator(cSynchronizedMap);
            iSynchronizedSortedMap = RuntimeEnv
                    .newInstantiator(cSynchronizedSortedMap);

            iCheckedMap = RuntimeEnv.newInstantiator(cCheckedMap);
            iCheckedSortedMap = RuntimeEnv.newInstantiator(cCheckedSortedMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        fSingletonMap_k.setAccessible(true);
        fSingletonMap_v.setAccessible(true);

        fUnmodifiableMap_m.setAccessible(true);
        fUnmodifiableSortedMap_sm.setAccessible(true);

        fSynchronizedMap_m.setAccessible(true);
        fSynchronizedSortedMap_sm.setAccessible(true);
        fSynchronizedMap_mutex.setAccessible(true);

        fCheckedMap_m.setAccessible(true);
        fCheckedSortedMap_sm.setAccessible(true);
        fCheckedMap_keyType.setAccessible(true);
        fCheckedMap_valueType.setAccessible(true);
    }

    public PolymorphicMapSchema(IdStrategy strategy) {
        super(strategy);
    }

    private static Class<?> map(String className, int id) {
        Class<?> clazz = RuntimeEnv.loadClass(className);
        __nonPublicMaps.put(clazz, id);
        return clazz;
    }

    static String name(int number) {
        switch (number) {
            case ID_EMPTY_MAP:
                return STR_EMPTY_MAP;
            case ID_SINGLETON_MAP:
                return STR_SINGLETON_MAP;
            case ID_UNMODIFIABLE_MAP:
                return STR_UNMODIFIABLE_MAP;
            case ID_UNMODIFIABLE_SORTED_MAP:
                return STR_UNMODIFIABLE_SORTED_MAP;
            case ID_SYNCHRONIZED_MAP:
                return STR_SYNCHRONIZED_MAP;
            case ID_SYNCHRONIZED_SORTED_MAP:
                return STR_SYNCHRONIZED_SORTED_MAP;
            case ID_CHECKED_MAP:
                return STR_CHECKED_MAP;
            case ID_CHECKED_SORTED_MAP:
                return STR_CHECKED_SORTED_MAP;
            case ID_ENUM_MAP:
                return STR_ENUM_MAP;
            case ID_MAP:
                return STR_MAP;
            default:
                return null;
        }
    }

    static int number(String name) {
        if (name.length() != 1)
            return 0;

        switch (name.charAt(0)) {
            case 'a':
                return 1;
            case 'b':
                return 2;
            case 'c':
                return 3;
            case 'd':
                return 4;
            case 'e':
                return 5;
            case 'f':
                return 6;
            case 'g':
                return 7;
            case 'h':
                return 8;
            case 'w':
                return ID_ENUM_MAP;
            case 'z':
                return ID_MAP;
            default:
                return 0;
        }
    }

    static int idFrom(Class<?> clazz) {
        final Integer id = __nonPublicMaps.get(clazz);
        if (id == null)
            throw new RuntimeException("Unknown map: " + clazz);

        return id.intValue();
    }

    static Object instanceFrom(final int id) {
        switch (id) {
            case ID_EMPTY_MAP:
                return Collections.EMPTY_MAP;

            case ID_SINGLETON_MAP:
                return iSingletonMap.newInstance();

            case ID_UNMODIFIABLE_MAP:
                return iUnmodifiableMap.newInstance();
            case ID_UNMODIFIABLE_SORTED_MAP:
                return iUnmodifiableSortedMap.newInstance();

            case ID_SYNCHRONIZED_MAP:
                return iSynchronizedMap.newInstance();
            case ID_SYNCHRONIZED_SORTED_MAP:
                return iSynchronizedSortedMap.newInstance();

            case ID_CHECKED_MAP:
                return iCheckedMap.newInstance();
            case ID_CHECKED_SORTED_MAP:
                return iCheckedSortedMap.newInstance();

            default:
                throw new RuntimeException("Unknown id: " + id);
        }
    }

    @SuppressWarnings("unchecked")
    static void writeObjectTo(Output output, Object value,
                              Schema<?> currentSchema, IdStrategy strategy) throws IOException {
        if (Collections.class == value.getClass().getDeclaringClass()) {
            writeNonPublicMapTo(output, value, currentSchema, strategy);
            return;
        }

        Class<Object> clazz = (Class<Object>) value.getClass();
        if (EnumMap.class.isAssignableFrom(clazz)) {
            strategy.writeEnumIdTo(output, ID_ENUM_MAP,
                    EnumIO.getKeyTypeFromEnumMap(value));

            // TODO use enum schema
        } else {
            strategy.writeMapIdTo(output, ID_MAP, clazz);
        }

        if (output instanceof StatefulOutput) {
            // update using the derived schema.
            ((StatefulOutput) output).updateLast(strategy.MAP_SCHEMA,
                    currentSchema);
        }

        strategy.MAP_SCHEMA.writeTo(output, (Map<Object, Object>) value);
    }

    static void writeNonPublicMapTo(Output output, Object value,
                                    Schema<?> currentSchema, IdStrategy strategy) throws IOException {
        final Integer n = __nonPublicMaps.get(value.getClass());
        if (n == null)
            throw new RuntimeException("Unknown collection: "
                    + value.getClass());
        final int id = n.intValue();
        switch (id) {
            case ID_EMPTY_MAP:
                output.writeUInt32(id, 0, false);
                break;

            case ID_SINGLETON_MAP: {
                final Object k, v;
                try {
                    k = fSingletonMap_k.get(value);
                    v = fSingletonMap_v.get(value);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                output.writeUInt32(id, 0, false);
                if (k != null)
                    output.writeObject(1, k, strategy.OBJECT_SCHEMA, false);
                if (v != null)
                    output.writeObject(3, v, strategy.OBJECT_SCHEMA, false);
                break;
            }

            case ID_UNMODIFIABLE_MAP:
                writeUnmodifiableMapTo(output, value, currentSchema, strategy, id);
                break;

            case ID_UNMODIFIABLE_SORTED_MAP:
                writeUnmodifiableMapTo(output, value, currentSchema, strategy, id);
                break;

            case ID_SYNCHRONIZED_MAP:
                writeSynchronizedMapTo(output, value, currentSchema, strategy, id);
                break;

            case ID_SYNCHRONIZED_SORTED_MAP:
                writeSynchronizedMapTo(output, value, currentSchema, strategy, id);
                break;

            case ID_CHECKED_MAP:
                writeCheckedMapTo(output, value, currentSchema, strategy, id);
                break;

            case ID_CHECKED_SORTED_MAP:
                writeCheckedMapTo(output, value, currentSchema, strategy, id);
                break;

            default:
                throw new RuntimeException("Should not happen.");
        }
    }

    private static void writeUnmodifiableMapTo(Output output, Object value,
                                               Schema<?> currentSchema, IdStrategy strategy, int id)
            throws IOException {
        final Object m;
        try {
            m = fUnmodifiableMap_m.get(value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        output.writeObject(id, m, strategy.POLYMORPHIC_MAP_SCHEMA, false);
    }

    private static void writeSynchronizedMapTo(Output output, Object value,
                                               Schema<?> currentSchema, IdStrategy strategy, int id)
            throws IOException {
        final Object m, mutex;
        try {
            m = fSynchronizedMap_m.get(value);
            mutex = fSynchronizedMap_mutex.get(value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (mutex != value) {
            // TODO for future release, introduce an interface(GraphOutput) so
            // we
            // can check whether the output can retain references.
            throw new RuntimeException(
                    "This exception is thrown to fail fast. "
                            + "Synchronized collections with a different mutex would only "
                            + "work if graph format is used, since the reference is retained.");
        }

        output.writeObject(id, m, strategy.POLYMORPHIC_MAP_SCHEMA, false);
    }

    private static void writeCheckedMapTo(Output output, Object value,
                                          Schema<?> currentSchema, IdStrategy strategy, int id)
            throws IOException {
        final Object m, keyType, valueType;
        try {
            m = fCheckedMap_m.get(value);
            keyType = fCheckedMap_keyType.get(value);
            valueType = fCheckedMap_valueType.get(value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        output.writeObject(id, m, strategy.POLYMORPHIC_MAP_SCHEMA, false);
        output.writeObject(1, keyType, strategy.CLASS_SCHEMA, false);
        output.writeObject(2, valueType, strategy.CLASS_SCHEMA, false);
    }

    @SuppressWarnings("unchecked")
    static Object readObjectFrom(Input input, Schema<?> schema, Object owner,
                                 IdStrategy strategy) throws IOException {
        final boolean graph = input instanceof GraphInput;
        Object ret = null;
        final int number = input.readFieldNumber(schema);
        switch (number) {
            case ID_EMPTY_MAP:
                if (graph) {
                    // update the actual reference.
                    ((GraphInput) input).updateLast(Collections.EMPTY_MAP, owner);
                }

                if (0 != input.readUInt32())
                    throw new ProtostuffException("Corrupt input.");

                ret = Collections.EMPTY_MAP;
                break;

            case ID_SINGLETON_MAP: {
                final Object map = iSingletonMap.newInstance();
                if (graph) {
                    // update the actual reference.
                    ((GraphInput) input).updateLast(map, owner);
                }

                if (0 != input.readUInt32())
                    throw new ProtostuffException("Corrupt input.");

                return fillSingletonMapFrom(input, schema, owner, strategy, graph,
                        map);
            }

            case ID_UNMODIFIABLE_MAP:
                ret = readUnmodifiableMapFrom(input, schema, owner, strategy,
                        graph, iUnmodifiableMap.newInstance(), false);
                break;

            case ID_UNMODIFIABLE_SORTED_MAP:
                ret = readUnmodifiableMapFrom(input, schema, owner, strategy,
                        graph, iUnmodifiableSortedMap.newInstance(), true);
                break;

            case ID_SYNCHRONIZED_MAP:
                ret = readSynchronizedMapFrom(input, schema, owner, strategy,
                        graph, iSynchronizedMap.newInstance(), false);
                break;

            case ID_SYNCHRONIZED_SORTED_MAP:
                ret = readSynchronizedMapFrom(input, schema, owner, strategy,
                        graph, iSynchronizedSortedMap.newInstance(), true);
                break;

            case ID_CHECKED_MAP:
                ret = readCheckedMapFrom(input, schema, owner, strategy, graph,
                        iCheckedMap.newInstance(), false);
                break;

            case ID_CHECKED_SORTED_MAP:
                ret = readCheckedMapFrom(input, schema, owner, strategy, graph,
                        iCheckedSortedMap.newInstance(), true);
                break;

            case ID_ENUM_MAP: {
                final Map<?, Object> em = strategy.resolveEnumFrom(input)
                        .newEnumMap();

                if (input instanceof GraphInput) {
                    // update the actual reference.
                    ((GraphInput) input).updateLast(em, owner);
                }

                strategy.MAP_SCHEMA.mergeFrom(input, (Map<Object, Object>) em);

                return em;
            }
            case ID_MAP: {
                final Map<Object, Object> map = strategy.resolveMapFrom(input)
                        .newMessage();

                if (input instanceof GraphInput) {
                    // update the actual reference.
                    ((GraphInput) input).updateLast(map, owner);
                }

                strategy.MAP_SCHEMA.mergeFrom(input, map);

                return map;
            }

            default:
                throw new ProtostuffException("Corrupt input.");
        }

        if (0 != input.readFieldNumber(schema))
            throw new ProtostuffException("Corrupt input.");

        return ret;
    }

    /**
     * Return true to
     */
    private static Object fillSingletonMapFrom(Input input, Schema<?> schema,
                                               Object owner, IdStrategy strategy, boolean graph, Object map)
            throws IOException {
        switch (input.readFieldNumber(schema)) {
            case 0:
                // both are null
                return map;
            case 1: {
                // key exists
                break;
            }
            case 3: {
                // key is null
                final Wrapper wrapper = new Wrapper();
                Object v = input.mergeObject(wrapper, strategy.OBJECT_SCHEMA);
                if (!graph || !((GraphInput) input).isCurrentMessageReference())
                    v = wrapper.value;

                try {
                    fSingletonMap_v.set(map, v);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                if (0 != input.readFieldNumber(schema))
                    throw new ProtostuffException("Corrupt input.");

                return map;
            }
            default:
                throw new ProtostuffException("Corrupt input.");
        }

        final Wrapper wrapper = new Wrapper();
        Object k = input.mergeObject(wrapper, strategy.OBJECT_SCHEMA);
        if (!graph || !((GraphInput) input).isCurrentMessageReference())
            k = wrapper.value;

        switch (input.readFieldNumber(schema)) {
            case 0:
                // key exists but null value
                try {
                    fSingletonMap_k.set(map, k);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                return map;
            case 3:
                // key and value exist
                break;
            default:
                throw new ProtostuffException("Corrupt input.");
        }

        Object v = input.mergeObject(wrapper, strategy.OBJECT_SCHEMA);
        if (!graph || !((GraphInput) input).isCurrentMessageReference())
            v = wrapper.value;

        try {
            fSingletonMap_k.set(map, k);
            fSingletonMap_v.set(map, v);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (0 != input.readFieldNumber(schema))
            throw new ProtostuffException("Corrupt input.");

        return map;
    }

    private static Object readUnmodifiableMapFrom(Input input,
                                                  Schema<?> schema, Object owner, IdStrategy strategy, boolean graph,
                                                  Object map, boolean sm) throws IOException {
        if (graph) {
            // update the actual reference.
            ((GraphInput) input).updateLast(map, owner);
        }

        final Wrapper wrapper = new Wrapper();
        Object m = input.mergeObject(wrapper, strategy.POLYMORPHIC_MAP_SCHEMA);
        if (!graph || !((GraphInput) input).isCurrentMessageReference())
            m = wrapper.value;
        try {
            fUnmodifiableMap_m.set(map, m);

            if (sm)
                fUnmodifiableSortedMap_sm.set(map, m);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return map;
    }

    private static Object readSynchronizedMapFrom(Input input,
                                                  Schema<?> schema, Object owner, IdStrategy strategy, boolean graph,
                                                  Object map, boolean sm) throws IOException {
        if (graph) {
            // update the actual reference.
            ((GraphInput) input).updateLast(map, owner);
        }

        final Wrapper wrapper = new Wrapper();
        Object m = input.mergeObject(wrapper, strategy.POLYMORPHIC_MAP_SCHEMA);
        if (!graph || !((GraphInput) input).isCurrentMessageReference())
            m = wrapper.value;
        try {
            fSynchronizedMap_m.set(map, m);
            fSynchronizedMap_mutex.set(map, map);

            if (sm)
                fSynchronizedSortedMap_sm.set(map, m);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return map;
    }

    private static Object readCheckedMapFrom(Input input, Schema<?> schema,
                                             Object owner, IdStrategy strategy, boolean graph, Object map,
                                             boolean sm) throws IOException {
        if (graph) {
            // update the actual reference.
            ((GraphInput) input).updateLast(map, owner);
        }

        final Wrapper wrapper = new Wrapper();
        Object m = input.mergeObject(wrapper, strategy.POLYMORPHIC_MAP_SCHEMA);
        if (!graph || !((GraphInput) input).isCurrentMessageReference())
            m = wrapper.value;

        if (1 != input.readFieldNumber(schema))
            throw new ProtostuffException("Corrupt input.");

        Object keyType = input.mergeObject(wrapper, strategy.CLASS_SCHEMA);
        if (!graph || !((GraphInput) input).isCurrentMessageReference())
            keyType = wrapper.value;

        if (2 != input.readFieldNumber(schema))
            throw new ProtostuffException("Corrupt input.");

        Object valueType = input.mergeObject(wrapper, strategy.CLASS_SCHEMA);
        if (!graph || !((GraphInput) input).isCurrentMessageReference())
            valueType = wrapper.value;

        try {
            fCheckedMap_m.set(map, m);
            fCheckedMap_keyType.set(map, keyType);
            fCheckedMap_valueType.set(map, valueType);

            if (sm)
                fCheckedSortedMap_sm.set(map, m);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return map;
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
        return Collection.class.getName();
    }

    @Override
    public String messageName() {
        return Collection.class.getSimpleName();
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
