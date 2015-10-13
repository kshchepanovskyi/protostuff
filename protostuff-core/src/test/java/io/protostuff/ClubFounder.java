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

public final class ClubFounder implements Externalizable, Message<ClubFounder> {

    static final ClubFounder DEFAULT_INSTANCE = new ClubFounder();
    // non-private fields
    // see http://developer.android.com/guide/practices/design/performance.html#package_inner
    String name;
    Club club;
    static final Schema<ClubFounder> SCHEMA = new Schema<ClubFounder>() {
        // schema methods

        final java.util.HashMap<String, Integer> fieldMap = new java.util.HashMap<>();

        {
            fieldMap.put("name", 1);
            fieldMap.put("club", 2);
        }

        @Override
        public ClubFounder newMessage() {
            return new ClubFounder();
        }

        @Override
        public Class<ClubFounder> typeClass() {
            return ClubFounder.class;
        }

        @Override
        public String messageName() {
            return ClubFounder.class.getSimpleName();
        }

        @Override
        public String messageFullName() {
            return ClubFounder.class.getName();
        }

        @Override
        public void mergeFrom(Input input, ClubFounder message) throws IOException {
            for (int number = input.readFieldNumber(this); ; number = input.readFieldNumber(this)) {
                switch (number) {
                    case 0:
                        return;
                    case 1:
                        message.name = input.readString();
                        break;
                    case 2:
                        message.club = input.mergeObject(message.club, Club.getSchema());
                        break;

                    default:
                        input.handleUnknownField(number, this);
                }
            }
        }

        @Override
        public void writeTo(Output output, ClubFounder message) throws IOException {
            if (message.name != null)
                output.writeString(1, message.name, false);

            if (message.club != null)
                output.writeObject(2, message.club, Club.getSchema(), false);

        }

        @Override
        public String getFieldName(int number) {
            switch (number) {
                case 1:
                    return "name";
                case 2:
                    return "club";
                default:
                    return null;
            }
        }

        @Override
        public int getFieldNumber(String name) {
            final Integer number = fieldMap.get(name);
            return number == null ? 0 : number.intValue();
        }
    };
    public ClubFounder() {

    }

    public static Schema<ClubFounder> getSchema() {
        return SCHEMA;
    }

    // getters and setters

    // name

    public static ClubFounder getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    public String getName() {
        return name;
    }

    // club

    public void setName(String name) {
        this.name = name;
    }

    public Club getClub() {
        return club;
    }

    // java serialization

    public void setClub(Club club) {
        this.club = club;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        GraphIOUtil.mergeDelimitedFrom(in, this, SCHEMA);
    }

    // message method

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        GraphIOUtil.writeDelimitedTo(out, this, SCHEMA);
    }

    @Override
    public Schema<ClubFounder> cachedSchema() {
        return SCHEMA;
    }

}
