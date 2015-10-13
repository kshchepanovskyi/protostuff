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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * Test generic types.
 *
 * @author David Yu
 */
public class GenericTypeTest extends TestCase {

    static Class<?> genericTypeFrom(Class<?> c, String fieldName, int index)
            throws Exception {
        return RuntimeFieldFactory.getGenericType(
                c.getDeclaredField(fieldName), index);
    }

    public void testIt() throws Exception {
        Class<PojoWithCollectionAndMapGenericTypes> c = PojoWithCollectionAndMapGenericTypes.class;

        assertTrue(byte[].class == genericTypeFrom(c, "cByteArray", 0));
        assertTrue(int[].class == genericTypeFrom(c, "cIntArray", 0));
        assertTrue(String[].class == genericTypeFrom(c, "cStringArray", 0));
        assertTrue(Throwable[].class == genericTypeFrom(c, "cThrowableArray", 0));

        assertTrue(long[][].class == genericTypeFrom(c, "cLongArray2D", 0));
        assertTrue(long[][][].class == genericTypeFrom(c, "cLongArray3D", 0));

        assertTrue(Class.class == genericTypeFrom(c, "cClass", 0));
        assertTrue(Enum.class == genericTypeFrom(c, "cEnum", 0));

        assertTrue(PojoWithCollectionAndMapGenericTypes.class == genericTypeFrom(
                c, "cPojo", 0));

        assertTrue(Class[].class == genericTypeFrom(c, "cClassArray", 0));

        assertTrue(Class.class == genericTypeFrom(c, "mClass", 0));
        assertTrue(Class.class == genericTypeFrom(c, "mClass", 1));

        assertTrue(Enum.class == genericTypeFrom(c, "mEnum", 0));
        assertTrue(Enum.class == genericTypeFrom(c, "mEnum", 1));
    }

    static class PojoWithCollectionAndMapGenericTypes {
        Collection<byte[]> cByteArray;
        List<int[]> cIntArray;
        Set<String[]> cStringArray;
        SortedSet<Throwable[]> cThrowableArray;

        Collection<long[][]> cLongArray2D;
        List<long[][][]> cLongArray3D;

        Collection<Class<?>> cClass;
        List<Enum<?>> cEnum;

        Collection<PojoWithCollectionAndMapGenericTypes> cPojo;

        @SuppressWarnings("rawtypes")
        Set<Class[]> cClassArray;

        // Not handled. ObjectSchema will be used which means it will not be
        // as fast as ArraySchema (same serialized output though).
        // Set<Class<?>[]> cClassArray2;

        @SuppressWarnings("rawtypes")
        Map<Class, Class<?>> mClass;

        @SuppressWarnings("rawtypes")
        SortedMap<Enum, Enum<?>> mEnum;
    }

}
