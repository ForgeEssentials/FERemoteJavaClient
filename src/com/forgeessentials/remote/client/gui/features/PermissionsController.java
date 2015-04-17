package com.forgeessentials.remote.client.gui.features;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

import com.forgeessentials.remote.client.RemoteRequest;
import com.forgeessentials.remote.client.RemoteResponse;
import com.forgeessentials.remote.client.RemoteResponse.JsonRemoteResponse;
import com.forgeessentials.remote.client.data.PermissionList;
import com.forgeessentials.remote.client.data.UserIdent;
import com.forgeessentials.remote.client.network.QueryPermissionsHandler;
import com.forgeessentials.remote.client.network.QueryPermissionsHandler.AreaZone;
import com.forgeessentials.remote.client.network.QueryPermissionsHandler.ServerZone;
import com.forgeessentials.remote.client.network.QueryPermissionsHandler.WorldZone;
import com.forgeessentials.remote.client.network.QueryPermissionsHandler.Zone;

public class PermissionsController extends FeatureController {

    @FXML
    protected TreeView<Zone> zoneTree;

    @FXML
    protected ListView<UserIdent> playerList;

    @FXML
    protected ListView<String> groupList;

    @FXML
    protected ComboBox<String> permissionName;

    @FXML
    protected TableView<Map.Entry<String, String>> permissionTable;

    @FXML
    protected TableColumn<Map.Entry<String, String>, String> permissionValue;

    @FXML
    protected TableColumn<Map.Entry<String, String>, String> permissionKey;

    protected ServerZone serverZone;

    protected String selectedGroup;

    protected UserIdent selectedPlayer;
    
    @Override
    public void init()
    {
        tab.setText("Permissions");

        permissionKey.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Entry<String,String>,String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<Entry<String, String>, String> value)
            {
                return new SimpleStringProperty(value.getValue().getKey());
            }
        });
        permissionValue.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Entry<String,String>,String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<Entry<String, String>, String> value)
            {
                return new SimpleStringProperty(value.getValue().getValue());
            }
        });

        zoneTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<Zone>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<Zone>> paramObservableValue, TreeItem<Zone> paramT1, TreeItem<Zone> paramT2)
            {
                updatePermissionList();
            }
        });
        playerList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<UserIdent>() {
            @Override
            public void changed(ObservableValue<? extends UserIdent> paramObservableValue, UserIdent oldValue, UserIdent newValue)
            {
                selectedGroup = null;
                selectedPlayer = newValue;
                updatePermissionList();
            }
        });
        groupList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> paramObservableValue, String oldValue, String newValue)
            {
                selectedGroup = newValue;
                selectedPlayer = null;
                updatePermissionList();
            }
        });

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
        RemoteResponse<QueryPermissionsHandler.Response> response = serverController.getClient().sendRequestAndWait(
                new RemoteRequest<>(QueryPermissionsHandler.ID, null), QueryPermissionsHandler.Response.class);
        if (response == null || !response.success)
        {
            serverController.log("Error getting permissions: " + (response == null ? "no response" : response.message));
            return;
        }

        serverZone = response.data;

        // Build views
        Set<UserIdent> players = new HashSet<UserIdent>();
        Set<String> groups = new HashSet<String>();
        
        TreeItem<Zone> root = new TreeItem<Zone>(serverZone);
        root.setExpanded(true);
        players.addAll(serverZone.playerPermissions.keySet());
        groups.addAll(serverZone.groupPermissions.keySet());
        for (WorldZone worldZone : serverZone.worldZones.values())
        {
            TreeItem<Zone> worldItem = new TreeItem<Zone>(worldZone);
            worldItem.setExpanded(true);
            players.addAll(worldZone.playerPermissions.keySet());
            groups.addAll(worldZone.groupPermissions.keySet());
            for (AreaZone areaZone : worldZone.areaZones)
            {
                worldItem.getChildren().add(new TreeItem<Zone>(areaZone));
                players.addAll(areaZone.playerPermissions.keySet());
                groups.addAll(areaZone.groupPermissions.keySet());
            }
            root.getChildren().add(worldItem);
        }
        zoneTree.setRoot(root);

        playerList.getItems().clear();
        for (UserIdent player : players)
            playerList.getItems().add(player);
        groupList.getItems().clear();
        for (String group : groups)
            groupList.getItems().add(group);
    }

    @Override
    public void stop()
    {
    }

    @Override
    public boolean handleResponse(JsonRemoteResponse response)
    {
        return false;
    }

}
