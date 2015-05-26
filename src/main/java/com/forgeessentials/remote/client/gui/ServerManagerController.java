package com.forgeessentials.remote.client.gui;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
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
    protected BorderPane root;

    @FXML
    protected TreeTableView<Server> serverTable;

    @FXML
    protected TreeTableColumn<Server, String> serverTableName;

    @FXML
    protected TreeTableColumn<Server, String> serverTableAddress;

    @FXML
    protected TreeTableColumn<Server, String> serverTablePort;

    @FXML
    protected TreeTableColumn<Server, String> serverTableUsername;

    @FXML
    protected TreeTableColumn<Server, Boolean> serverTableSsl;

    @FXML
    protected TextField name;

    @FXML
    protected TextField address;

    @FXML
    protected TextField port;

    @FXML
    protected TextField username;

    @FXML
    protected TextField passkey;

    @FXML
    protected CheckBox ssl;

    private ObjectProperty<Server> serversRootProperty;

    private Server serversRoot;

    private Server selectedServer;

    private boolean result;

    public Server getSelectedServer()
    {
        return selectedServer;
    }

    public boolean getResult()
    {
        return result;
    }

    public void init(ObjectProperty<Server> serversRootProperty)
    {
        this.serversRootProperty = serversRootProperty;
        this.serversRoot = new Server(serversRootProperty.get());
        serverTable.setRoot(new ServerTreeItem(serversRoot));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        serverTableName.setCellValueFactory(new TreeItemPropertyValueFactory<Server, String>("name"));
        serverTableAddress.setCellValueFactory(new TreeItemPropertyValueFactory<Server, String>("address"));
        serverTableUsername.setCellValueFactory(new TreeItemPropertyValueFactory<Server, String>("username"));
        serverTablePort.setCellValueFactory(new TreeItemPropertyValueFactory<Server, String>("port"));
        serverTableSsl.setCellValueFactory(new TreeItemPropertyValueFactory<Server, Boolean>("ssl"));

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
                        ssl.selectedProperty().unbindBidirectional(oldValue.getValue().sslProperty());
                    }
                }
                name.clear();
                address.clear();
                port.clear();
                username.clear();
                passkey.clear();
                ssl.setSelected(false);
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
                        ssl.selectedProperty().bindBidirectional(newValue.getValue().sslProperty());
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
        result = selectedServer != null;
        serversRootProperty.set(serversRoot);
        ((Stage) root.getScene().getWindow()).close();
    }

    @FXML
    public void cancel(ActionEvent event)
    {
        result = false;
        ((Stage) root.getScene().getWindow()).close();
    }

    @FXML
    public void apply(ActionEvent event)
    {
        serversRootProperty.set(serversRoot);
        serversRoot = new Server(serversRoot);
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
