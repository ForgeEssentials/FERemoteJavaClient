package com.forgeessentials.remote.client.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    
    MainController controller;

    @Override
    public void start(Stage primaryStage)
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(ClassLoader.getSystemResource("main.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(ClassLoader.getSystemResource("application.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setTitle("FE Remote");

            controller = fxmlLoader.getController();
            controller.init();
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
        controller.stop();
    }

    public static void main(String[] args)
    {
        launch(args);
    }

}
