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

import java.util.Collection;
import java.util.Map;

import io.protostuff.runtime.PolymorphicSchema.Handler;

/**
 * Polymorphic types.
 *
 * @author David Yu
 */
public enum PolymorphicSchemaFactories implements PolymorphicSchema.Factory {

    ARRAY {
        @Override
        public PolymorphicSchema newSchema(Class<?> typeClass,
                                           IdStrategy strategy, final Handler handler) {
            @SuppressWarnings("unchecked")
            Class<Object> ct = (Class<Object>) typeClass.getComponentType();

            RuntimeFieldFactory<?> rff = RuntimeFieldFactory.getFieldFactory(
                    ct, strategy);

            if (rff == RuntimeFieldFactory.DELEGATE) {
                // delegate
                return strategy.getDelegateWrapper(ct).newSchema(typeClass,
                        strategy, handler);
            }

            if (rff.id > 0 && rff.id < 15) {
                // scalar
                return ArraySchemas.newSchema(rff.id, ct, typeClass, strategy,
                        handler);
            }

            if (ct.isEnum()) {
                // enum
                return strategy.getEnumIO(ct).newSchema(typeClass, strategy,
                        handler);
            }

            if (rff == RuntimeFieldFactory.POJO
                    || (rff == RuntimeFieldFactory.POLYMORPHIC_POJO && RuntimeFieldFactory
                    .pojo(ct, null, strategy))) {
                // pojo
                return strategy.getSchemaWrapper(ct, true).newSchema(typeClass,
                        strategy, handler);
            }

            return new ArraySchema(strategy) {
                @Override
                protected void setValue(Object value, Object owner) {
                    handler.setValue(value, owner);
                }
            };
        }
    },
    NUMBER {
        @Override
        public PolymorphicSchema newSchema(Class<?> typeClass,
                                           IdStrategy strategy, final Handler handler) {
            return new NumberSchema(strategy) {
                protected void setValue(Object value, Object owner) {
                    handler.setValue(value, owner);
                }
            };
        }
    },
    CLASS {
        public PolymorphicSchema newSchema(Class<?> typeClass,
                                           IdStrategy strategy, final Handler handler) {
            return new ClassSchema(strategy) {
                @Override
                protected void setValue(Object value, Object owner) {
                    handler.setValue(value, owner);
                }
            };
        }
    },
    ENUM {
        @Override
        public PolymorphicSchema newSchema(Class<?> typeClass,
                                           IdStrategy strategy, final Handler handler) {
            return new PolymorphicEnumSchema(strategy) {
                @Override
                protected void setValue(Object value, Object owner) {
                    handler.setValue(value, owner);
                }
            };
        }
    },
    COLLECTION {
        @Override
        public PolymorphicSchema newSchema(Class<?> typeClass,
                                           IdStrategy strategy, final Handler handler) {
            return new PolymorphicCollectionSchema(strategy) {
                protected void setValue(Object value, Object owner) {
                    handler.setValue(value, owner);
                }
            };
        }
    },
    MAP {
        @Override
        public PolymorphicSchema newSchema(Class<?> typeClass,
                                           IdStrategy strategy, final Handler handler) {
            return new PolymorphicMapSchema(strategy) {
                protected void setValue(Object value, Object owner) {
                    handler.setValue(value, owner);
                }
            };
        }
    },
    THROWABLE {
        public PolymorphicSchema newSchema(Class<?> typeClass,
                                           IdStrategy strategy, final Handler handler) {
            return new PolymorphicThrowableSchema(strategy) {
                @Override
                protected void setValue(Object value, Object owner) {
                    handler.setValue(value, owner);
                }
            };
        }
    },
    OBJECT {
        @Override
        public PolymorphicSchema newSchema(Class<?> typeClass,
                                           IdStrategy strategy, final Handler handler) {
            return new ObjectSchema(strategy) {
                @Override
                protected void setValue(Object value, Object owner) {
                    handler.setValue(value, owner);
                }
            };
        }
    };

    public static PolymorphicSchema.Factory getFactoryFromField(Class<?> clazz) {
        if (clazz.isArray())
            return ARRAY;

        if (Number.class == clazz)
            return NUMBER;

        if (Class.class == clazz)
            return CLASS;

        if (Enum.class == clazz)
            return ENUM;

        if (Map.class.isAssignableFrom(clazz))
            return MAP;

        if (Collection.class.isAssignableFrom(clazz))
            return COLLECTION;

        if (Throwable.class.isAssignableFrom(clazz))
            return THROWABLE;

        return OBJECT;
    }

    public static PolymorphicSchema.Factory getFactoryFromRepeatedValueGenericType(
            Class<?> clazz) {
        if (clazz.isArray())
            return ARRAY;

        if (Number.class == clazz)
            return NUMBER;

        if (Class.class == clazz)
            return CLASS;

        if (Enum.class == clazz)
            return ENUM;

        if (Throwable.class.isAssignableFrom(clazz))
            return THROWABLE;

        if (Object.class == clazz)
            return OBJECT;

        return null;
    }

    public static PolymorphicSchema getSchemaFromCollectionOrMapGenericType(
            Class<?> clazz, IdStrategy strategy) {
        if (clazz.isArray()) {
            @SuppressWarnings("unchecked")
            Class<Object> ct = (Class<Object>) clazz.getComponentType();

            RuntimeFieldFactory<?> rff = RuntimeFieldFactory.getFieldFactory(
                    ct, strategy);

            if (rff == RuntimeFieldFactory.DELEGATE) {
                // delegate
                return strategy.getDelegateWrapper(ct).genericElementSchema;
            }

            if (rff.id > 0 && rff.id < 15) {
                // scalar
                return ArraySchemas.getGenericElementSchema(rff.id);
            }

            if (ct.isEnum()) {
                // enum
                return strategy.getEnumIO(ct).genericElementSchema;
            }

            if (rff == RuntimeFieldFactory.POJO
                    || (rff == RuntimeFieldFactory.POLYMORPHIC_POJO && RuntimeFieldFactory
                    .pojo(ct, null, strategy))) {
                // pojo
                return strategy.getSchemaWrapper(ct, true).genericElementSchema;
            }

            return strategy.ARRAY_ELEMENT_SCHEMA;
        }

        if (Number.class == clazz)
            return strategy.NUMBER_ELEMENT_SCHEMA;

        if (Class.class == clazz)
            return strategy.CLASS_ELEMENT_SCHEMA;

        if (Enum.class == clazz)
            return strategy.POLYMORPHIC_ENUM_ELEMENT_SCHEMA;

        if (Throwable.class.isAssignableFrom(clazz))
            return strategy.POLYMORPHIC_THROWABLE_ELEMENT_SCHEMA;

        if (Object.class == clazz)
            return strategy.OBJECT_ELEMENT_SCHEMA;

        return null;
    }
}
