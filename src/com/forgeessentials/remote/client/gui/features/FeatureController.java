package com.forgeessentials.remote.client.gui.features;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.layout.Region;

import com.forgeessentials.remote.client.RemoteResponse.JsonRemoteResponse;
import com.forgeessentials.remote.client.gui.ServerController;

public abstract class FeatureController implements Initializable {

    @FXML
    protected Region root;

    protected ServerController serverController;

    protected Tab tab;

    public void setParent(ServerController serverController, Tab tab)
    {
        this.serverController = serverController;
        this.tab = tab;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        root.setUserData(this);
    }

    public abstract void init();

    public abstract void stop();

    public abstract boolean handleResponse(JsonRemoteResponse response);

}
