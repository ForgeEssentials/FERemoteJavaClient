package com.forgeessentials.remote.client.gui.features;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;

import com.forgeessentials.remote.client.RemoteResponse.JsonRemoteResponse;
import com.forgeessentials.remote.client.gui.ServerController;

public abstract class FeatureController implements Initializable {

    @FXML
    Tab tab;

    @FXML
    ServerController serverController;

    public void setServerController(ServerController serverController)
    {
        this.serverController = serverController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        tab.setUserData(this);
    }

    public abstract void init();

    public abstract void stop();

    public abstract boolean handleResponse(JsonRemoteResponse response);

}
