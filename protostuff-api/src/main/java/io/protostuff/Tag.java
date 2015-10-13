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
package io.protostuff;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A field annotation to configure the field number explicitly.
 *
 * @author Brice Jaglin
 * @author David Yu
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Tag {
    int value();

    /**
     * Overrides the field name (useful for non-binary formats like json/xml/yaml). Optional.
     */
    String alias() default "";

    /**
     * A value of 0x1F means the first 5 groups (1,2,4,8,16 - bits) will include this field. A <b>negative</b> value of
     * 0x1F means the first 5 groups will <b>exclude</b> this field. Optional.
     */
    int groupFilter() default 0;
}
