package com.forgeessentials.remote.client.gui.type;

import java.io.IOException;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class StringPropertyAdapter extends TypeAdapter<StringProperty> {

    @Override
    public StringProperty read(JsonReader reader) throws IOException
    {
        if (reader.peek() == JsonToken.NULL)
        {
            reader.nextNull();
            return new SimpleStringProperty();
        }
        return new SimpleStringProperty(reader.nextString());
    }

    @Override
    public void write(JsonWriter writer, StringProperty value) throws IOException
    {
        if (value == null || value.isEmpty().get())
        {
            writer.nullValue();
            return;
        }
        writer.value(value.get());
    }

}