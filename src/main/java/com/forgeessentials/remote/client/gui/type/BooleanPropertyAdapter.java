package com.forgeessentials.remote.client.gui.type;

import java.io.IOException;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class BooleanPropertyAdapter extends TypeAdapter<BooleanProperty> {

    @Override
    public BooleanProperty read(JsonReader reader) throws IOException
    {
        if (reader.peek() == JsonToken.NULL)
        {
            reader.nextNull();
            return new SimpleBooleanProperty();
        }
        return new SimpleBooleanProperty(reader.nextBoolean());
    }

    @Override
    public void write(JsonWriter writer, BooleanProperty value) throws IOException
    {
        if (value == null)
        {
            writer.nullValue();
            return;
        }
        writer.value(value.get());
    }

}