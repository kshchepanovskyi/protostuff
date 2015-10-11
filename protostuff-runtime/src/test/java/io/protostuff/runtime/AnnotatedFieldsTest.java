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

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import io.protostuff.Tag;

/**
 * Test for runtime schemas to handle annotation-based field mapping.
 * 
 * @author Brice Jaglin
 * @author David Yu
 */
public class AnnotatedFieldsTest
{

    public static class EntityFullyAnnotated
    {

        @Tag(3)
        int id;

        @Tag(5)
        String name;

        @Tag(2)
        @Deprecated
        String alias;
    }

    public static class EntityPartlyAnnotated1
    {

        @Tag(3)
        int id;

        // Missing annotation
        String name;

        @Tag(2)
        @Deprecated
        String alias;
    }

    public static class EntityPartlyAnnotated2
    {

        // Missing annotation
        int id;

        @Tag(4)
        String name;
    }

    public static class EntityInvalidAnnotated1
    {

        @Tag(-1)
        int id;
    }

    public static class EntityInvalidAnnotated2
    {

        @Tag(2)
        int id;

        @Tag(2)
        int other;
    }

    static class EntityInvalidTagNumber
    {
        @Tag(0)
        int id;
    }

    static class EntityWithFieldAlias
    {
        @Tag(400)
        double field400;

        @Tag(value = 200, alias = "f200")
        int field200;
    }

    @Test
    public void testEntityFullyAnnotated()
    {
        RuntimeSchema<EntityFullyAnnotated> schema = (RuntimeSchema<EntityFullyAnnotated>) RuntimeSchema
                .getSchema(EntityFullyAnnotated.class, RuntimeEnv.ID_STRATEGY);

        assertTrue(schema.getFieldCount() == 2);
        assertEquals(schema.getFields().get(0).name, "id");
        assertEquals(schema.getFields().get(0).number, 3);

        assertEquals(schema.getFields().get(1).name, "name");
        assertEquals(schema.getFields().get(1).number, 5);

        assertTrue(schema.getFieldNumber("alias") == 0);
        assertNull(schema.getFieldByName("alias"));
    }

    @Test
    public void testEntityPartlyAnnotated1()
    {
        try
        {
            RuntimeSchema.getSchema(EntityPartlyAnnotated1.class);
            fail();
        }
        catch (RuntimeException e)
        {
            // expected
        }
    }

    @Test
    public void testEntityPartlyAnnotated2()
    {
        try
        {
            RuntimeSchema.getSchema(EntityPartlyAnnotated2.class);
            fail();
        }
        catch (RuntimeException e)
        {
            // expected
        }
    }

    @Test
    public void testEntityInvalidAnnotated1()
    {
        try
        {
            RuntimeSchema.getSchema(EntityInvalidAnnotated1.class);
            fail();
        }
        catch (RuntimeException e)
        {
            // expected
        }
    }

    @Test
    public void testEntityInvalidAnnotated2()
    {
        try
        {
            RuntimeSchema.getSchema(EntityInvalidAnnotated1.class);
            fail();
        }
        catch (RuntimeException e)
        {
            // expected
        }
    }

    @Test
    public void testEntityInvalidTagNumber() throws Exception
    {
        try
        {
            RuntimeSchema.getSchema(EntityInvalidTagNumber.class);
            fail();
        }
        catch (RuntimeException e)
        {
            // expected
        }
    }

    static <T> void verify(RuntimeSchema<T> schema, int number, String name,
            int offset)
    {
        assertEquals(schema.getFields().get(offset).name, name);
        assertEquals(schema.getFields().get(offset).number, number);

        assertEquals(name, schema.getFieldName(number));
        assertEquals(number, schema.getFieldNumber(name));
    }

    @Test
    public void testEntityWithFieldAlias()
    {
        RuntimeSchema<EntityWithFieldAlias> schema = (RuntimeSchema<EntityWithFieldAlias>) RuntimeSchema
                .getSchema(EntityWithFieldAlias.class, RuntimeEnv.ID_STRATEGY);

        assertTrue(schema.getFieldCount() == 2);

        // The field with the smallest field number will be written first.
        // In this case, field200 (despite field400 being declared 1st)
        verify(schema, 200, "f200", 0);
        verify(schema, 400, "field400", 1);
    }

}
