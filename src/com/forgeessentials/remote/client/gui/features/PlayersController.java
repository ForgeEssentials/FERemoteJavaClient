package com.forgeessentials.remote.client.gui.features;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import com.forgeessentials.remote.client.RemoteRequest;
import com.forgeessentials.remote.client.RemoteResponse;
import com.forgeessentials.remote.client.RemoteResponse.JsonRemoteResponse;
import com.forgeessentials.remote.client.data.DataFloatLocation;
import com.forgeessentials.remote.client.network.QueryPlayerHandler;
import com.forgeessentials.remote.client.network.QueryPlayerHandler.PlayerInfoResponse;

public class PlayersController extends FeatureController {

    @FXML
    protected TableView<PlayerInfo> playersTable;

    @FXML
    protected TableColumn<PlayerInfo, String> colName;

    @FXML
    protected TableColumn<PlayerInfo, String> colHealth;

    @FXML
    protected TableColumn<PlayerInfo, String> colArmor;

    @FXML
    protected TableColumn<PlayerInfo, String> colHunger;

    @FXML
    protected TableColumn<PlayerInfo, String> colSaturation;

    @FXML
    protected TableColumn<PlayerInfo, String> colLocation;

    @FXML
    protected CheckBox flagDetails;

    @FXML
    protected CheckBox flagLocation;

    protected Timer updateTimer;

    @Override
    public void init()
    {
        tab.setText("Players");
        
        colName.setCellValueFactory(new PropertyValueFactory<PlayerInfo, String>("name"));
        colHealth.setCellValueFactory(new PropertyValueFactory<PlayerInfo, String>("health"));
        colArmor.setCellValueFactory(new PropertyValueFactory<PlayerInfo, String>("armor"));
        colHunger.setCellValueFactory(new PropertyValueFactory<PlayerInfo, String>("hunger"));
        colSaturation.setCellValueFactory(new PropertyValueFactory<PlayerInfo, String>("saturation"));
        colLocation.setCellValueFactory(new PropertyValueFactory<PlayerInfo, String>("location"));

        updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run()
            {
                queryPlayers();
            }
        }, 0, 1 * 1000);
    }

    @Override
    public void stop()
    {
        updateTimer.cancel();
    }

    @Override
    public boolean handleResponse(JsonRemoteResponse response)
    {
        return false;
    }

    /* ------------------------------------------------------------ */

    private void queryPlayers()
    {
        List<String> flags = new ArrayList<>();
        if (flagDetails.isSelected())
            flags.add("detail");
        if (flagLocation.isSelected())
            flags.add("location");

        final RemoteRequest<QueryPlayerHandler.Request> request = new RemoteRequest<>(QueryPlayerHandler.ID, new QueryPlayerHandler.Request(null, flags));
        final RemoteResponse<QueryPlayerHandler.Response> response = serverController.getClient()
                .sendRequestAndWait(request, QueryPlayerHandler.Response.class);
        if (response == null)
        {
            serverController.log("Error: no response");
            return;
        }
        if (!response.success)
        {
            serverController.log("Error: " + response.message);
            return;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                playersTable.getItems().clear();
                for (PlayerInfoResponse pi : response.data.players)
                {
                    PlayerInfo playerInfo = new PlayerInfo(pi.name);
                    if (pi.data.containsKey("health"))
                        playerInfo.health = new SimpleFloatProperty(pi.data.get("health").getAsFloat());
                    if (pi.data.containsKey("armor"))
                        playerInfo.armor = new SimpleIntegerProperty(pi.data.get("armor").getAsInt());
                    if (pi.data.containsKey("hunger"))
                        playerInfo.hunger = new SimpleIntegerProperty(pi.data.get("hunger").getAsInt());
                    if (pi.data.containsKey("saturation"))
                        playerInfo.saturation = new SimpleFloatProperty(pi.data.get("saturation").getAsFloat());
                    if (pi.data.containsKey("location"))
                    {
                        DataFloatLocation loc = serverController.getClient().getGson().fromJson(pi.data.get("location"), DataFloatLocation.class);
                        playerInfo.location = new SimpleStringProperty(loc.toString());
                    }
                    playersTable.getItems().add(playerInfo);
                }
            }
        });
    }

    public static int getInteger(Object val)
    {
        if (val instanceof Integer)
            return (int) val;
        else if (val instanceof Float)
            return (int) (float) val;
        else if (val instanceof Double)
            return (int) (double) val;
        else if (val instanceof String)
            return Integer.parseInt((String) val);
        throw new IllegalArgumentException();
    }

    public static float getFloat(Object val)
    {
        if (val instanceof Integer)
            return (float) val;
        else if (val instanceof Float)
            return (float) val;
        else if (val instanceof Double)
            return (float) (double) val;
        else if (val instanceof String)
            return Float.parseFloat((String) val);
        throw new IllegalArgumentException();
    }

    @FXML
    public void onFlagsChanged()
    {
        queryPlayers();
    }

    public static class PlayerInfo {

        StringProperty name;

        FloatProperty health;

        IntegerProperty armor;

        IntegerProperty hunger;

        FloatProperty saturation;

        StringProperty location;

        public PlayerInfo(String name)
        {
            this.name = new SimpleStringProperty(name);
        }

        public StringProperty nameProperty()
        {
            return name;
        }

        public FloatProperty healthProperty()
        {
            return health;
        }

        public IntegerProperty armorProperty()
        {
            return armor;
        }

        public IntegerProperty hungerProperty()
        {
            return hunger;
        }

        public FloatProperty saturationProperty()
        {
            return saturation;
        }

        public StringProperty locationProperty()
        {
            return location;
        }

    }

}
