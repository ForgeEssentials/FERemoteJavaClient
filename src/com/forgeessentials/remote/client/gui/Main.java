package com.forgeessentials.remote.client.gui;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.forgeessentials.remote.client.gui.model.Server;
import com.forgeessentials.remote.client.gui.type.IntegerPropertyAdapter;
import com.forgeessentials.remote.client.gui.type.StringPropertyAdapter;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class Main extends Application {

    public static final Gson gson;

    static
    {
        GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
        gsonBuilder.registerTypeAdapter(StringProperty.class, new StringPropertyAdapter());
        gsonBuilder.registerTypeAdapter(IntegerProperty.class, new IntegerPropertyAdapter());
        gsonBuilder.setExclusionStrategies(new ExposeExclusionStrategy());
        gson = gsonBuilder.create();
    }

    public static Server serversRoot = new Server("root");

    @Override
    public void start(Stage primaryStage)
    {
        load();

        try
        {
            Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
            Scene scene = new Scene(root); // new Scene(root, 400, 400);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void stop()
    {
        save();
    }

    public static void save()
    {
        try (Writer fw = new FileWriter(new File("servers.json")))
        {
            Main.gson.toJson(serversRoot, fw);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void load()
    {
        try
        {
            serversRoot = Main.gson.fromJson(new FileReader(new File("servers.json")), Server.class);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        launch(args);
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
