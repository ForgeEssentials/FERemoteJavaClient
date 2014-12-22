package com.forgeessentials.remote.client.gui;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainController {

    @FXML
    public void openServerManager(ActionEvent event)
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(ServerManagerController.class.getResource("serverManager.fxml"));
            Parent root = fxmlLoader.load();
            ServerManagerController controller = fxmlLoader.getController();
            Scene scene = new Scene(root);
            
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Server Manager");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());
            stage.showAndWait();
            
            if (controller.getSelectedServer() != null)
                System.out.println(controller.getSelectedServer().nameProperty().get());
            
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
