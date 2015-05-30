package com.forgeessentials.remote.client.gui.features;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

import com.forgeessentials.remote.RemoteMessageID;
import com.forgeessentials.remote.client.RemoteRequest;
import com.forgeessentials.remote.client.RemoteResponse;
import com.forgeessentials.remote.client.RemoteResponse.JsonRemoteResponse;
import com.forgeessentials.remote.client.data.AreaZone;
import com.forgeessentials.remote.client.data.PermissionList;
import com.forgeessentials.remote.client.data.ServerZone;
import com.forgeessentials.remote.client.data.UserIdent;
import com.forgeessentials.remote.client.data.WorldZone;
import com.forgeessentials.remote.client.data.Zone;
import com.forgeessentials.remote.client.gui.control.AutoCompleteLongestMatchListener;
import com.forgeessentials.remote.network.QueryRegisteredPermissionsResponse;
import com.forgeessentials.remote.network.SetPermissionRequest;

public class PermissionsController extends FeatureController
{

    @FXML
    protected TreeView<Zone> zoneTree;

    @FXML
    protected ListView<UserIdent> playerList;

    @FXML
    protected ListView<String> groupList;

    @FXML
    protected ComboBox<String> permissionKey;

    @FXML
    protected TableView<Map.Entry<String, String>> permissionTable;

    @FXML
    protected TableColumn<Map.Entry<String, String>, String> permissionValueColumn;

    @FXML
    protected TableColumn<Map.Entry<String, String>, String> permissionKeyColumn;

    protected String selectedGroup;

    protected UserIdent selectedPlayer;

    @Override
    public void init()
    {
        tab.setText("Permissions");

        permissionKeyColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Entry<String, String>, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<Entry<String, String>, String> value)
            {
                return new SimpleStringProperty(value.getValue().getKey());
            }
        });
        permissionValueColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Entry<String, String>, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<Entry<String, String>, String> value)
            {
                return new SimpleStringProperty(value.getValue().getValue());
            }
        });
        playerList.setCellFactory(new Callback<ListView<UserIdent>, ListCell<UserIdent>>() {
            @Override
            public ListCell<UserIdent> call(ListView<UserIdent> param)
            {
                return new ListCell<UserIdent>() {
                    @Override
                    public void updateItem(UserIdent user, boolean empty)
                    {
                        super.updateItem(user, empty);
                        if(user != null)
                            setText(user.getUsernameOrUuid());
                    }
                };
            }
        });

        zoneTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<Zone>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<Zone>> observable, TreeItem<Zone> oldValue, TreeItem<Zone> newValue)
            {
                updatePermissionList();
            }
        });
        playerList.setOnMouseClicked(new EventHandler<Event>() {
            @Override
            public void handle(Event value)
            {
                selectedPlayer = playerList.getSelectionModel().getSelectedItem();
                selectedGroup = null;
                updatePermissionList();
            }
        });
        groupList.setOnMouseClicked(new EventHandler<Event>() {
            @Override
            public void handle(Event value)
            {
                selectedPlayer = null;
                selectedGroup = groupList.getSelectionModel().getSelectedItem();
                updatePermissionList();
            }
        });
        playerList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<UserIdent>() {
            @Override
            public void changed(ObservableValue<? extends UserIdent> observable, UserIdent oldValue, UserIdent newValue)
            {
                selectedPlayer = newValue;
                selectedGroup = null;
                updatePermissionList();
            }
        });
        groupList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                selectedPlayer = null;
                selectedGroup = newValue;
                updatePermissionList();
            }
        });
        permissionTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Map.Entry<String, String>>() {
            @Override
            public void changed(ObservableValue<? extends Entry<String, String>> observable, Entry<String, String> oldValue, Entry<String, String> newValue)
            {
                if (newValue != null)
                    permissionKey.getEditor().setText(newValue.getKey());
            }
        });
        permissionKey.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event)
            {
                if (event.getCode() == KeyCode.ENTER)
                    addPermission(null);
            }
        });

        RemoteResponse<QueryRegisteredPermissionsResponse> response = serverController.getClient().sendRequestAndWait(
                new RemoteRequest<Object>(QueryRegisteredPermissionsResponse.ID, null), QueryRegisteredPermissionsResponse.class);
        if (response != null && response.success)
            permissionKey.setItems(FXCollections.observableList(response.data));;
        new AutoCompleteLongestMatchListener<String>(permissionKey);
    }

    @Override
    public void activate()
    {
        queryPermissions();
    }

    private void updatePermissionList()
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                permissionTable.getItems().clear();
                TreeItem<Zone> zone = zoneTree.getSelectionModel().getSelectedItem();
                if (zone == null || (selectedGroup == null && selectedPlayer == null))
                    return;
                PermissionList permissions;
                if (selectedGroup == null)
                    permissions = zone.getValue().playerPermissions.get(selectedPlayer);
                else
                    permissions = zone.getValue().groupPermissions.get(selectedGroup);
                if (permissions == null)
                    return;
                permissionTable.setItems(FXCollections.observableArrayList(new ArrayList<>(permissions.entrySet())));
            }
        });
    }

    private void queryPermissions()
    {
        RemoteResponse<ServerZone> response = serverController.getClient().sendRequestAndWait(new RemoteRequest<>(RemoteMessageID.QUERY_PERMISSIONS, null),
                ServerZone.class);
        if (response == null || !response.success)
        {
            serverController.log("Error getting permissions: " + (response == null ? "no response" : response.message));
            return;
        }

        TreeItem<Zone> selectedZone = zoneTree.getSelectionModel().getSelectedItem();
        UserIdent selectedPlayer = playerList.getSelectionModel().getSelectedItem();
        String selectedGroup = groupList.getSelectionModel().getSelectedItem();

        // Build / update view
        ServerZone serverZone = response.data;
        Set<UserIdent> players = new HashSet<UserIdent>();
        Set<String> groups = new HashSet<String>();

        TreeItem<Zone> root = new TreeItem<Zone>(serverZone);
        zoneTree.setRoot(root);
        root.setExpanded(true);
        players.addAll(serverZone.playerPermissions.keySet());
        groups.addAll(serverZone.groupPermissions.keySet());
        if (selectedZone == null || (selectedZone != null && serverZone.id == selectedZone.getValue().id))
            zoneTree.getSelectionModel().select(root);
        for (WorldZone worldZone : serverZone.worldZones.values())
        {
            TreeItem<Zone> worldItem = new TreeItem<Zone>(worldZone);
            root.getChildren().add(worldItem);
            worldItem.setExpanded(true);
            players.addAll(worldZone.playerPermissions.keySet());
            groups.addAll(worldZone.groupPermissions.keySet());
            if (selectedZone != null && worldZone.id == selectedZone.getValue().id)
                zoneTree.getSelectionModel().select(worldItem);
            for (AreaZone areaZone : worldZone.areaZones)
            {
                TreeItem<Zone> areaItem = new TreeItem<Zone>(areaZone);
                worldItem.getChildren().add(areaItem);
                players.addAll(areaZone.playerPermissions.keySet());
                groups.addAll(areaZone.groupPermissions.keySet());
                if (selectedZone != null && areaZone.id == selectedZone.getValue().id)
                    zoneTree.getSelectionModel().select(areaItem);
            }
        }

        playerList.getItems().clear();
        for (UserIdent player : players)
        {
            playerList.getItems().add(player);
            if (player.equals(selectedPlayer))
                playerList.getSelectionModel().select(player);
        }
        groupList.getItems().clear();
        for (String group : groups)
            groupList.getItems().add(group);
        groupList.getSelectionModel().select(selectedGroup);
        if (selectedGroup == null && selectedPlayer == null)
            groupList.getSelectionModel().select("_ALL_");
    }

    @Override
    public boolean handleResponse(JsonRemoteResponse response)
    {
        return false;
    }

    @FXML
    public void addPermission(ActionEvent event)
    {
        // try
        // {
        // FXMLLoader fxmlLoader = new FXMLLoader(ClassLoader.getSystemResource("dialog/permissionValue.fxml"));
        // Parent root = fxmlLoader.load();
        // Scene scene = new Scene(root);
        // // Scene scene = new Scene(new Group(new Text(0, 20, "Hello World!")));
        //
        // Stage dialog2 = new Stage();
        // dialog2.initStyle(StageStyle.UTILITY);
        // dialog2.setScene(scene);
        // dialog2.showAndWait();
        // }
        // catch (IOException e)
        // {
        // e.printStackTrace();
        // }

        final ChoiceDialog<String> dialog = new ChoiceDialog<String>("true", "true", "false");
        @SuppressWarnings("unchecked")
        final ComboBox<String> cb = ((ComboBox<String>) ((GridPane) dialog.getDialogPane().getContent()).getChildren().get(1));
        new AutoCompleteLongestMatchListener<String>(cb);
        dialog.setTitle("Enter a value for the permission");
        dialog.setHeaderText(permissionKey.getEditor().getText());
        dialog.setContentText("Value:");
        cb.setEditable(true);
        cb.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event)
            {
                if (event.getCode() == KeyCode.ENTER)
                {
                    dialog.setResult(cb.getEditor().getText());
                    dialog.close();
                }
            }
        });
        // cb.getSelectionModel().select(0);

        Entry<String,String> item = permissionTable.getSelectionModel().getSelectedItem();
        if ((item != null) && item.getKey().equals(permissionKey.getEditor().getText()))
            cb.getEditor().setText(item.getValue());

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent())
        {
            // serverController.log(permissionKey.getEditor().getText() + " = " + result.get());
            addPermission(permissionKey.getEditor().getText(), result.get());
        }
    }

    @FXML
    public void deletePermission()
    {
        deletePermission(permissionKey.getEditor().getText());
    }

    private void addPermission(String permission, String value)
    {
        TreeItem<Zone> zone = zoneTree.getSelectionModel().getSelectedItem();
        if (zone == null)
            return;

        SetPermissionRequest request;
        if (selectedGroup != null)
            request = new SetPermissionRequest(zone.getValue().id, selectedGroup, permission, value);
        else if (selectedPlayer != null)
            request = new SetPermissionRequest(zone.getValue().id, selectedPlayer, permission, value);
        else
            return;

        RemoteResponse<Object> response = serverController.getClient().sendRequestAndWait(new RemoteRequest<>(SetPermissionRequest.ID, request), Object.class);
        if (response == null || !response.success)
        {
            serverController.log("Error setting permission: " + (response == null ? "no response" : response.message));
            return;
        }
        serverController.log("Set permission: " + permission + " = " + value);
        queryPermissions();
    }

    private void deletePermission(String permission)
    {
        TreeItem<Zone> zone = zoneTree.getSelectionModel().getSelectedItem();
        if (zone == null)
            return;

        SetPermissionRequest request;
        if (selectedGroup != null)
            request = new SetPermissionRequest(zone.getValue().id, selectedGroup, permission, null);
        else if (selectedPlayer != null)
            request = new SetPermissionRequest(zone.getValue().id, selectedPlayer, permission, null);
        else
            return;

        RemoteResponse<Object> response = serverController.getClient().sendRequestAndWait(new RemoteRequest<>(SetPermissionRequest.ID, request), Object.class);
        if (response == null || !response.success)
        {
            serverController.log("Error setting permissions: " + (response == null ? "no response" : response.message));
            return;
        }
        serverController.log("Deleted permission: " + permission);
        queryPermissions();
    }

}
