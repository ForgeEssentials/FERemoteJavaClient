package com.forgeessentials.remote.client.gui;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

import com.forgeessentials.remote.client.gui.model.Server;

public class ServerManagerController implements Initializable {

    @FXML
    BorderPane root;

    @FXML
    TreeTableView<Server> serverTable;

    @FXML
    TreeTableColumn<Server, String> serverTableName;

    @FXML
    TreeTableColumn<Server, String> serverTableAddress;

    @FXML
    TreeTableColumn<Server, String> serverTablePort;

    @FXML
    TreeTableColumn<Server, String> serverTableUsername;

    @FXML
    TextField name;

    @FXML
    TextField address;

    @FXML
    TextField port;

    @FXML
    TextField username;

    @FXML
    TextField passkey;

    private Server serversRoot;

    private Server selectedServer;

    public Server getServersRoot()
    {
        return serversRoot;
    }

    public Server getSelectedServer()
    {
        return selectedServer;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        serversRoot = new Server(Main.serversRoot);

        serverTableName.setCellValueFactory(new TreeItemPropertyValueFactory<Server, String>("name"));
        serverTableAddress.setCellValueFactory(new TreeItemPropertyValueFactory<Server, String>("address"));
        serverTableUsername.setCellValueFactory(new TreeItemPropertyValueFactory<Server, String>("username"));
        serverTablePort.setCellValueFactory(new TreeItemPropertyValueFactory<Server, String>("port"));
        serverTable.setRoot(new ServerTreeItem(serversRoot));

        makeIntegerTextField(port);

        final DecimalFormat numberFormat = new DecimalFormat();
        numberFormat.setGroupingUsed(false);
        final NumberStringConverter numberStringConverter = new NumberStringConverter(numberFormat);
        serverTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<Server>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<Server>> observable, TreeItem<Server> oldValue, TreeItem<Server> newValue)
            {
                if (oldValue != null && oldValue.getValue() != null)
                {
                    name.textProperty().unbindBidirectional(oldValue.getValue().nameProperty());
                    if (!oldValue.getValue().isFolder())
                    {
                        address.textProperty().unbindBidirectional(oldValue.getValue().addressProperty());
                        port.textProperty().unbindBidirectional(oldValue.getValue().portProperty());
                        username.textProperty().unbindBidirectional(oldValue.getValue().usernameProperty());
                        passkey.textProperty().unbindBidirectional(oldValue.getValue().passkeyProperty());
                    }
                }
                name.clear();
                address.clear();
                port.clear();
                username.clear();
                passkey.clear();
                if (newValue != null && newValue.getValue() != null)
                {
                    name.textProperty().bindBidirectional(newValue.getValue().nameProperty());
                    if (!newValue.getValue().isFolder())
                    {
                        selectedServer = newValue.getValue();
                        address.textProperty().bindBidirectional(newValue.getValue().addressProperty());
                        port.textProperty().bindBidirectional(newValue.getValue().portProperty(), numberStringConverter);
                        username.textProperty().bindBidirectional(newValue.getValue().usernameProperty());
                        passkey.textProperty().bindBidirectional(newValue.getValue().passkeyProperty());
                    }
                }
                else
                {
                    selectedServer = null;
                }
            }
        });

        serverTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                if (event.getClickCount() == 2 && selectedServer != null)
                    ok();
            }
        });
    }

    public void makeIntegerTextField(TextField field)
    {
        field.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent)
            {
                if (!keyEvent.getCharacter().matches("\\d+"))
                {
                    keyEvent.consume();
                }
            }
        });
        field.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue)
            {
                try
                {
                    if (!newValue.isEmpty())
                    {
                        Integer.parseInt(newValue);
                    }
                }
                catch (NumberFormatException e)
                {
                    ((StringProperty) observableValue).setValue(oldValue);
                }
            }
        });
    }

    @FXML
    public void newFolder(ActionEvent event)
    {
        ServerTreeItem item = (ServerTreeItem) serverTable.getSelectionModel().getSelectedItem();
        if (!item.getValue().isFolder())
            item = (ServerTreeItem) item.getParent();
        item.getValue().subServers.add(new Server("New folder"));
        item.setChanged();
        serverTable.getSelectionModel().select(item.getChildren().get(item.getChildren().size() - 1));
    }

    @FXML
    public void deleteEntry(ActionEvent event)
    {
        ServerTreeItem item = (ServerTreeItem) serverTable.getSelectionModel().getSelectedItem();
        ServerTreeItem parent = (ServerTreeItem) item.getParent();
        if (parent != null)
        {
            int idx = parent.getChildren().indexOf(item);
            parent.getValue().subServers.remove(item.getValue());
            parent.setChanged();
            serverTable.getSelectionModel().select(parent.getChildren().isEmpty() ? parent : parent.getChildren().get(idx <= 0 ? 0 : idx - 1));
        }
    }

    @FXML
    public void newServer(ActionEvent event)
    {
        ServerTreeItem item = (ServerTreeItem) serverTable.getSelectionModel().getSelectedItem();
        if (!item.getValue().isFolder())
            item = (ServerTreeItem) item.getParent();
        item.getValue().subServers.add(new Server("New server", "localhost", 27020, "ForgeDevName", ""));
        item.setChanged();
        serverTable.getSelectionModel().select(item.getChildren().get(item.getChildren().size() - 1));
    }

    @FXML
    public void ok()
    {
        Main.serversRoot = serversRoot;
        ((Stage) root.getScene().getWindow()).close();
    }

    @FXML
    public void cancel(ActionEvent event)
    {
        selectedServer = null;
        ((Stage) root.getScene().getWindow()).close();
    }

    @FXML
    public void apply(ActionEvent event)
    {
        Main.serversRoot = serversRoot;
        serversRoot = new Server(serversRoot);
        Main.save();
    }

    public class ServerTreeItem extends TreeItem<Server> {

        protected boolean changed = true;

        public ServerTreeItem(Server server)
        {
            super(server);
            if (server.isFolder())
                setExpanded(true);
        }

        @Override
        public ObservableList<TreeItem<Server>> getChildren()
        {
            ObservableList<TreeItem<Server>> items = super.getChildren();
            if (changed)
            {
                Set<Server> checkedServers = new HashSet<>();
                Iterator<TreeItem<Server>> it = items.iterator();
                while (it.hasNext())
                {
                    TreeItem<Server> item = it.next();
                    if (getValue().subServers.contains(item.getValue()))
                        checkedServers.add(item.getValue());
                    else
                        it.remove();
                }
                for (Server s : getValue().subServers)
                {
                    if (!checkedServers.contains(s))
                        items.add(new ServerTreeItem(s));
                }
                changed = false;
            }
            return items;
        }

        @Override
        public boolean isLeaf()
        {
            return !getValue().isFolder();
        }

        public void setChanged()
        {
            changed = true;
        }

    }

}
