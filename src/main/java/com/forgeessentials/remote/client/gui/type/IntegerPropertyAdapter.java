package com.forgeessentials.remote.client.gui.type;

import java.io.IOException;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class IntegerPropertyAdapter extends TypeAdapter<IntegerProperty> {

    @Override
    public IntegerProperty read(JsonReader reader) throws IOException
    {
        if (reader.peek() == JsonToken.NULL)
        {
            reader.nextNull();
            return new SimpleIntegerProperty();
        }
        return new SimpleIntegerProperty(reader.nextInt());
    }

    @Override
    public void write(JsonWriter writer, IntegerProperty value) throws IOException
    {
        if (value == null)
        {
            writer.nullValue();
            return;
        }
        writer.value(value.get());
    }

}