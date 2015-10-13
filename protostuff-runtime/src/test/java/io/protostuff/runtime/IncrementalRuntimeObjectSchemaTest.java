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

import junit.framework.TestCase;

import io.protostuff.runtime.AbstractRuntimeObjectSchemaTest.AcousticGuitar;
import io.protostuff.runtime.AbstractRuntimeObjectSchemaTest.BassGuitar;
import io.protostuff.runtime.AbstractRuntimeObjectSchemaTest.Bat;
import io.protostuff.runtime.AbstractRuntimeObjectSchemaTest.CustomArrayList;
import io.protostuff.runtime.AbstractRuntimeObjectSchemaTest.CustomHashMap;
import io.protostuff.runtime.AbstractRuntimeObjectSchemaTest.GuitarPickup;
import io.protostuff.runtime.AbstractRuntimeObjectSchemaTest.Pojo;
import io.protostuff.runtime.AbstractRuntimeObjectSchemaTest.PojoWithArray;
import io.protostuff.runtime.AbstractRuntimeObjectSchemaTest.PojoWithArray2D;
import io.protostuff.runtime.AbstractRuntimeObjectSchemaTest.PojoWithCollection;
import io.protostuff.runtime.AbstractRuntimeObjectSchemaTest.PojoWithCustomArrayListAndHashMap;
import io.protostuff.runtime.AbstractRuntimeObjectSchemaTest.PojoWithMap;
import io.protostuff.runtime.AbstractRuntimeObjectSchemaTest.Size;
import io.protostuff.runtime.AbstractRuntimeObjectSchemaTest.WrapsBat;
import io.protostuff.runtime.SampleDelegates.ShortArrayDelegate;

import static io.protostuff.runtime.SampleDelegates.SINGLETON_DELEGATE;

/**
 * Test for {@link IncrementalIdStrategy}.
 *
 * @author David Yu
 */
public class IncrementalRuntimeObjectSchemaTest extends TestCase {

    static final boolean runTest;

    static {
        // check whether test/run from root module
        String strategy = System.getProperty("test_id_strategy");
        runTest = strategy == null || strategy.equals("incremental");

        if (runTest) {
            System.setProperty("protostuff.runtime.id_strategy_factory",
                    "io.protostuff.runtime.IncrementalRuntimeObjectSchemaTest$IdStrategyFactory");
        }
    }

    public void testProtostuff() throws Exception {
        if (runTest && RuntimeEnv.ID_STRATEGY instanceof IncrementalIdStrategy) {
            junit.textui.TestRunner tr = new junit.textui.TestRunner();
            tr.doRun(tr.getTest(
                    "io.protostuff.runtime.ProtostuffRuntimeObjectSchemaTest"
            ), false);

            assertTrue(IdStrategyFactory.INSTANCE_COUNT != 0);
        }
    }

    public void testProtobuf() throws Exception {
        if (runTest && RuntimeEnv.ID_STRATEGY instanceof IncrementalIdStrategy) {
            junit.textui.TestRunner tr = new junit.textui.TestRunner();
            tr.doRun(tr.getTest(
                    "io.protostuff.runtime.ProtobufRuntimeObjectSchemaTest"
            ), false);

            assertTrue(IdStrategyFactory.INSTANCE_COUNT != 0);
        }
    }

    public static class IdStrategyFactory implements IdStrategy.Factory {

        static int INSTANCE_COUNT = 0;

        IncrementalIdStrategy.Registry r = new IncrementalIdStrategy.Registry(
                20, 11,
                20, 11,
                20, 11,
                80, 11);

        public IdStrategyFactory() {
            ++INSTANCE_COUNT;
            System.out.println("@INCREMENTAL");
        }

        @Override
        public IdStrategy create() {
            return r.strategy;
        }

        @Override
        public void postCreate() {
            r.registerCollection(CollectionSchema.MessageFactories.ArrayList, 1)
                    .registerCollection(CollectionSchema.MessageFactories.HashSet, 2)
                    .registerCollection(CustomArrayList.MESSAGE_FACTORY, 3);

            r.registerMap(MapSchema.MessageFactories.HashMap, 1)
                    .registerMap(MapSchema.MessageFactories.LinkedHashMap, 2)
                    .registerMap(CustomHashMap.MESSAGE_FACTORY, 3);

            r.registerEnum(Size.class, 1)
                    .registerEnum(GuitarPickup.class, 2);

            r.registerPojo(AcousticGuitar.class, 1)
                    .registerPojo(BassGuitar.class, 2)
                    .registerPojo(Pojo.class, 3)
                    .registerPojo(PojoWithArray.class, 4)
                    .registerPojo(PojoWithArray2D.class, 5)
                    .registerPojo(PojoWithCollection.class, 6)
                    .registerPojo(PojoWithMap.class, 7)
                    .registerPojo(Bat.SCHEMA, Bat.PIPE_SCHEMA, 8)
                    .registerPojo(WrapsBat.class, 9)
                    .registerPojo(PojoWithCustomArrayListAndHashMap.class, 10);

            r.registerDelegate(new ShortArrayDelegate(), 1);
            r.registerDelegate(SINGLETON_DELEGATE, 2);

            r = null;
        }

    }

}
