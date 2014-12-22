package com.forgeessentials.remote.client.gui;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainController {

    @FXML
    public Button serverManager;

    @FXML
    public void openServerManager(ActionEvent event)
    {
        try
        {
            Stage stage = new Stage();
            stage.setScene(new Scene(FXMLLoader.load(ServerManagerController.class.getResource("serverManager.fxml"))));
            stage.setTitle("My modal window");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());
            stage.show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
