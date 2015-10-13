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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

public final class SampleClass implements Externalizable, Message<SampleClass> {

    static final SampleClass DEFAULT_INSTANCE = new SampleClass();
    // non-private fields
    // see http://developer.android.com/guide/practices/design/performance.html#package_inner
    List<String> testString;
    static final Schema<SampleClass> SCHEMA = new Schema<SampleClass>() {
        // schema methods

        final java.util.HashMap<String, Integer> fieldMap = new java.util.HashMap<>();

        {
            fieldMap.put("testString", 1);
        }

        public SampleClass newMessage() {
            return new SampleClass();
        }

        public Class<SampleClass> typeClass() {
            return SampleClass.class;
        }

        public String messageName() {
            return SampleClass.class.getSimpleName();
        }

        public String messageFullName() {
            return SampleClass.class.getName();
        }

        public void mergeFrom(Input input, SampleClass message) throws IOException {
            for (int number = input.readFieldNumber(this); ; number = input.readFieldNumber(this)) {
                switch (number) {
                    case 0:
                        return;
                    case 1:
                        if (message.testString == null)
                            message.testString = new ArrayList<>();
                        message.testString.add(input.readString());
                        break;
                    default:
                        input.handleUnknownField(number, this);
                }
            }
        }

        public void writeTo(Output output, SampleClass message) throws IOException {
            if (message.testString != null) {
                for (String testString : message.testString) {
                    if (testString != null)
                        output.writeString(1, testString, true);
                }
            }
        }

        public String getFieldName(int number) {
            switch (number) {
                case 1:
                    return "testString";
                default:
                    return null;
            }
        }

        public int getFieldNumber(String name) {
            final Integer number = fieldMap.get(name);
            return number == null ? 0 : number.intValue();
        }
    };

    public SampleClass() {

    }

    public static Schema<SampleClass> getSchema() {
        return SCHEMA;
    }

    // getters and setters

    // testString

    public static SampleClass getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    public List<String> getTestStringList() {
        return testString;
    }

    // java serialization

    public void setTestStringList(List<String> testString) {
        this.testString = testString;
    }

    public void readExternal(ObjectInput in) throws IOException {
        GraphIOUtil.mergeDelimitedFrom(in, this, SCHEMA);
    }

    // message method

    public void writeExternal(ObjectOutput out) throws IOException {
        GraphIOUtil.writeDelimitedTo(out, this, SCHEMA);
    }

    public Schema<SampleClass> cachedSchema() {
        return SCHEMA;
    }

}
