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

public final class Student implements Externalizable, Message<Student> {

    static final Student DEFAULT_INSTANCE = new Student();
    // non-private fields
    // see http://developer.android.com/guide/practices/design/performance.html#package_inner
    String name;
    List<Club> club;
    static final Schema<Student> SCHEMA = new Schema<Student>() {
        // schema methods

        final java.util.HashMap<String, Integer> fieldMap = new java.util.HashMap<>();

        {
            fieldMap.put("name", 1);
            fieldMap.put("club", 2);
        }

        @Override
        public Student newMessage() {
            return new Student();
        }

        @Override
        public Class<Student> typeClass() {
            return Student.class;
        }

        @Override
        public String messageName() {
            return Student.class.getSimpleName();
        }

        @Override
        public String messageFullName() {
            return Student.class.getName();
        }

        @Override
        public void mergeFrom(Input input, Student message) throws IOException {
            for (int number = input.readFieldNumber(this); ; number = input.readFieldNumber(this)) {
                switch (number) {
                    case 0:
                        return;
                    case 1:
                        message.name = input.readString();
                        break;
                    case 2:
                        if (message.club == null)
                            message.club = new ArrayList<>();
                        message.club.add(input.mergeObject(null, Club.getSchema()));
                        break;

                    default:
                        input.handleUnknownField(number, this);
                }
            }
        }

        @Override
        public void writeTo(Output output, Student message) throws IOException {
            if (message.name != null)
                output.writeString(1, message.name, false);

            if (message.club != null) {
                for (Club club : message.club) {
                    if (club != null)
                        output.writeObject(2, club, Club.getSchema(), true);
                }
            }

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
    public Student() {

    }

    public static Schema<Student> getSchema() {
        return SCHEMA;
    }

    // getters and setters

    // name

    public static Student getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    public String getName() {
        return name;
    }

    // club

    public void setName(String name) {
        this.name = name;
    }

    public List<Club> getClubList() {
        return club;
    }

    public void setClubList(List<Club> club) {
        this.club = club;
    }

    public Club getClub(int index) {
        return club == null ? null : club.get(index);
    }

    public int getClubCount() {
        return club == null ? 0 : club.size();
    }

    // java serialization

    public void addClub(Club club) {
        if (this.club == null)
            this.club = new ArrayList<>();
        this.club.add(club);
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
    public Schema<Student> cachedSchema() {
        return SCHEMA;
    }

}
