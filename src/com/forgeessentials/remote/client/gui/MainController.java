package com.forgeessentials.remote.client.gui;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.forgeessentials.remote.client.gui.model.Server;
import com.forgeessentials.remote.client.gui.type.DataManager;
import com.google.gson.JsonParseException;

public class MainController implements Initializable {

    @FXML
    TabPane servers;

    public ObjectProperty<Server> serversRoot;

    public File serversFile = new File("servers.json");

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        loadServers();
    }

    public void init()
    {
        // Hack to close tabs on application-close, because no more on-closed events get fired
        servers.getTabs().addListener(new ListChangeListener<Tab>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Tab> tabs)
            {
                while (tabs.next())
                    for (Tab tab : tabs.getRemoved())
                        ((ServerTab) tab).close();
            }
        });
    }

    public void stop()
    {
        servers.getTabs().clear();
    }

    public void disconnect(ServerController controller)
    {
        controller.close();
    }

    public void loadServers()
    {
        serversRoot = null;
        try
        {
            if (serversFile.exists())
                serversRoot = new SimpleObjectProperty<Server>(DataManager.gson.fromJson(new FileReader(serversFile), Server.class));
        }
        catch (JsonParseException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        if (serversRoot == null)
            serversRoot = new SimpleObjectProperty<Server>(new Server("root"));
        serversRoot.addListener(new ChangeListener<Server>() {
            @Override
            public void changed(ObservableValue<? extends Server> observable, Server oldValue, Server newValue)
            {
                saveServers();
            }
        });
    }

    public void saveServers()
    {
        try (Writer fw = new FileWriter(serversFile))
        {
            DataManager.gson.toJson(serversRoot.get(), fw);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @FXML
    public void openServerManager(ActionEvent event)
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(ClassLoader.getSystemResource("serverManager.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Server Manager");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());

            ServerManagerController controller = fxmlLoader.getController();
            controller.init(serversRoot);
            stage.showAndWait();

            if (controller.getResult())
            {
                connect(controller.getSelectedServer());
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void connect(Server server)
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(ClassLoader.getSystemResource("server.fxml"));
            Parent content = fxmlLoader.load();
            ServerController controller = fxmlLoader.getController();
            if (controller.init(server))
                servers.getTabs().add(new ServerTab(controller, content));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public class ServerTab extends Tab {

        private ServerController controller;

        public ServerTab(ServerController controller, Node content)
        {
            super(controller.getServer().nameProperty().get());
            this.controller = controller;
            this.setContent(content);
            this.setOnClosed(new EventHandler<Event>() {
                @Override
                public void handle(Event event)
                {
                    close();
                }
            });
            this.setOnCloseRequest(new EventHandler<Event>() {
                @Override
                public void handle(Event event)
                {
                    // TODO: Confirmation
                }
            });
            controller.setTab(this);
        }

        public ServerController getController()
        {
            return controller;
        }

        public void close()
        {
            controller.close();
        }

    }

}
