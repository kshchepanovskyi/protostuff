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

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;

/**
 * @author Kostiantyn Shchepanovskyi
 */
public class AlwaysUseSunReflectionFactoryOptionTest {

    @Test
    public void forceUseSunReflectionFactory() throws Exception {
        System.setProperty("protostuff.runtime.always_use_sun_reflection_factory", "true");
        Schema<MyClass> schema = RuntimeSchema.getSchema(MyClass.class);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        MyClass myClass = new MyClass(); // constructor initializes list with one element
        ProtostuffIOUtil.writeTo(output, myClass, schema, LinkedBuffer.allocate());
        byte[] bytes = output.toByteArray();
        Assert.assertEquals(1, myClass.getList().size());
        MyClass myClassNew = schema.newMessage(); // default constructor should not be used
        ProtostuffIOUtil.mergeFrom(bytes, myClassNew, schema);
        Assert.assertEquals(1, myClassNew.getList().size());
    }

    final static class MyClass {
        private List<String> list;

        public MyClass() {
            list = new ArrayList<>();
            list.add("hello");
        }

        public List<String> getList() {
            return list;
        }
    }

}
