// Generated by http://code.google.com/p/protostuff/ ... DO NOT EDIT!
// Generated from repeated.proto

package io.protostuff;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

public final class PojoWithRepeated implements Externalizable, Message<PojoWithRepeated>, Schema<PojoWithRepeated>
{

    public static Schema<PojoWithRepeated> getSchema()
    {
        return DEFAULT_INSTANCE;
    }

    public static PojoWithRepeated getDefaultInstance()
    {
        return DEFAULT_INSTANCE;
    }

    static final PojoWithRepeated DEFAULT_INSTANCE = new PojoWithRepeated();

    private List<Integer> someInt32;
    private List<Long> someFixed64;

    public PojoWithRepeated()
    {

    }

    @Override
    public String toString()
    {
        return "PojoWithRepeated{" +
                "someInt32=" + someInt32 +
                ", someFixed64=" + someFixed64 +
                '}';
    }

    // getters and setters

    // someInt32

    public List<Integer> getSomeInt32List()
    {
        return someInt32;
    }

    public PojoWithRepeated setSomeInt32List(List<Integer> someInt32)
    {
        this.someInt32 = someInt32;
        return this;
    }

    public Integer getSomeInt32(int index)
    {
        return someInt32 == null ? null : someInt32.get(index);
    }

    public int getSomeInt32Count()
    {
        return someInt32 == null ? 0 : someInt32.size();
    }

    public PojoWithRepeated addSomeInt32(Integer someInt32)
    {
        if (this.someInt32 == null)
            this.someInt32 = new ArrayList<>();
        this.someInt32.add(someInt32);
        return this;
    }

    // someFixed64

    public List<Long> getSomeFixed64List()
    {
        return someFixed64;
    }

    public PojoWithRepeated setSomeFixed64List(List<Long> someFixed64)
    {
        this.someFixed64 = someFixed64;
        return this;
    }

    public Long getSomeFixed64(int index)
    {
        return someFixed64 == null ? null : someFixed64.get(index);
    }

    public int getSomeFixed64Count()
    {
        return someFixed64 == null ? 0 : someFixed64.size();
    }

    public PojoWithRepeated addSomeFixed64(Long someFixed64)
    {
        if (this.someFixed64 == null)
            this.someFixed64 = new ArrayList<>();
        this.someFixed64.add(someFixed64);
        return this;
    }

    // java serialization

    public void readExternal(ObjectInput in) throws IOException
    {
        GraphIOUtil.mergeDelimitedFrom(in, this, this);
    }

    public void writeExternal(ObjectOutput out) throws IOException
    {
        GraphIOUtil.writeDelimitedTo(out, this, this);
    }

    // message method

    public Schema<PojoWithRepeated> cachedSchema()
    {
        return DEFAULT_INSTANCE;
    }

    // schema methods

    public PojoWithRepeated newMessage()
    {
        return new PojoWithRepeated();
    }

    public Class<PojoWithRepeated> typeClass()
    {
        return PojoWithRepeated.class;
    }

    public String messageName()
    {
        return PojoWithRepeated.class.getSimpleName();
    }

    public String messageFullName()
    {
        return PojoWithRepeated.class.getName();
    }

    public void mergeFrom(Input input, PojoWithRepeated message) throws IOException
    {
        try
        {
            for (int number = input.readFieldNumber(this);; number = input.readFieldNumber(this))
            {
                switch (number)
                {
                    case 0:
                        return;
                    case 1:
                        if (message.someInt32 == null)
                            message.someInt32 = new ArrayList<>();
                        message.someInt32.add(input.readInt32());
                        break;
                    case 2:
                        if (message.someFixed64 == null)
                            message.someFixed64 = new ArrayList<>();
                        message.someFixed64.add(input.readFixed64());
                        break;
                    default:
                        input.handleUnknownField(number, this);
                }
            }
        }
        finally
        {
            if (message.someInt32 != null)
                message.someInt32 = java.util.Collections.unmodifiableList(message.someInt32);
            else
                message.someInt32 = java.util.Collections.emptyList();
            if (message.someFixed64 != null)
                message.someFixed64 = java.util.Collections.unmodifiableList(message.someFixed64);
            else
                message.someFixed64 = java.util.Collections.emptyList();
        }
    }

    public void writeTo(Output output, PojoWithRepeated message) throws IOException
    {
        if (message.someInt32 != null)
        {
            for (Integer someInt32 : message.someInt32)
            {
                if (someInt32 != null)
                    output.writeInt32(1, someInt32, true);
            }
        }

        if (message.someFixed64 != null)
        {
            for (Long someFixed64 : message.someFixed64)
            {
                if (someFixed64 != null)
                    output.writeFixed64(2, someFixed64, true);
            }
        }
    }

    public String getFieldName(int number)
    {
        switch (number)
        {
            case 1:
                return "someInt32";
            case 2:
                return "someFixed64";
            default:
                return null;
        }
    }

    public int getFieldNumber(String name)
    {
        final Integer number = __fieldMap.get(name);
        return number == null ? 0 : number.intValue();
    }

    private static final java.util.HashMap<String, Integer> __fieldMap = new java.util.HashMap<>();
    static
    {
        __fieldMap.put("someInt32", 1);
        __fieldMap.put("someFixed64", 2);
    }

}
