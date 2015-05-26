package com.forgeessentials.remote.client.gui.type;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class DataManager {

    public static final Gson gson;

    static
    {
        GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
        gsonBuilder.registerTypeAdapter(StringProperty.class, new StringPropertyAdapter());
        gsonBuilder.registerTypeAdapter(IntegerProperty.class, new IntegerPropertyAdapter());
        gsonBuilder.registerTypeAdapter(BooleanProperty.class, new BooleanPropertyAdapter());
        gsonBuilder.setExclusionStrategies(new ExposeExclusionStrategy());
        gson = gsonBuilder.create();
    }

    public static class ExposeExclusionStrategy implements ExclusionStrategy {

        @Override
        public boolean shouldSkipField(FieldAttributes f)
        {
            Expose expose = f.getAnnotation(Expose.class);
            if (expose != null && (!expose.serialize() || !expose.deserialize()))
                return true;
            return false;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz)
        {
            return false;
        }

    }

}
