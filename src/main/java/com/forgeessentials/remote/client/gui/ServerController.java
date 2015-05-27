package com.forgeessentials.remote.client.gui;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Region;

import com.forgeessentials.remote.RemoteMessageID;
import com.forgeessentials.remote.client.RemoteClient;
import com.forgeessentials.remote.client.RemoteRequest;
import com.forgeessentials.remote.client.RemoteRequest.PushRequestData;
import com.forgeessentials.remote.client.RemoteResponse;
import com.forgeessentials.remote.client.RemoteResponse.JsonRemoteResponse;
import com.forgeessentials.remote.client.RequestAuth;
import com.forgeessentials.remote.client.gui.MainController.ServerTab;
import com.forgeessentials.remote.client.gui.features.FeatureController;
import com.forgeessentials.remote.client.gui.model.Server;
import com.forgeessentials.remote.network.ChatResponse;

import javafx.scene.control.TextField;

public class ServerController implements Runnable
{

    public static final int TIMEOUT = 30 * 1000;

    @FXML
    protected Region root;

    @FXML
    protected ListView<String> log;

    @FXML
    protected TabPane features;

    @FXML
    protected TextField input;

    private RemoteClient client;

    private RequestAuth auth;

    private Server server;

    private ServerTab serverTab;

    /* ------------------------------------------------------------ */

    public void init(Server server) throws UnknownHostException, IOException
    {
        this.server = server;
        client = new RemoteClient(server.addressProperty().get(), server.portProperty().get());
        if (!server.usernameProperty().get().isEmpty())
            auth = new RequestAuth(server.usernameProperty().get(), server.passkeyProperty().get());
        else
            auth = null;
        new Thread(this).start();
        queryCapabilities();
        pushChat(true);

        // Initialize controllers for feature-tabs
        for (Tab tab : features.getTabs())
            if (tab.getContent().getUserData() instanceof FeatureController)
            {
                FeatureController controller = (FeatureController) tab.getContent().getUserData();
                controller.setParent(this, tab);
                controller.init();
            }
        features.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> paramObservableValue, Tab oldValue, Tab newValue)
            {
                if (oldValue != null)
                    ((FeatureController) oldValue.getContent().getUserData()).deactivate();
                if (newValue != null)
                    ((FeatureController) newValue.getContent().getUserData()).activate();
            }
        });
        ((FeatureController) features.getSelectionModel().getSelectedItem().getContent().getUserData()).activate();
    }

    public void stop()
    {
        // Stop controllers for feature-tabs
        for (Tab tab : features.getTabs())
            if (tab.getContent().getUserData() instanceof FeatureController)
                ((FeatureController) tab.getContent().getUserData()).stop();
        client.close();
    }

    public void log(final String string)
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                while (log.getItems().size() > 500)
                    log.getItems().remove(0);
                log.getItems().add(string);
                log.scrollTo(log.getItems().size());
            }
        });
    }

    /* ------------------------------------------------------------ */
    /* Message loop */

    @Override
    public void run()
    {
        while (!client.isClosed())
        {
            JsonRemoteResponse response = client.getNextResponse(0);
            if (response != null)
                handleResponse(response);
        }
    }

    protected void handleResponse(JsonRemoteResponse response)
    {
        boolean handled = false;
        for (Tab tab : features.getTabs())
            if (tab.getContent().getUserData() instanceof FeatureController)
                if (((FeatureController) tab.getContent().getUserData()).handleResponse(response))
                {
                    handled = true;
                    break;
                }
        if (!handled)
        {
            if (response.id == null)
            {
                handleUnknownResponse(response);
                return;
            }
            switch (response.id)
            {
            case ChatResponse.ID:
            {
                RemoteResponse<ChatResponse> r = client.transformResponse(response, ChatResponse.class);
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                if (r.data.sender == null)
                    log(String.format("[%s] %s", timeFormat.format(r.data.timestamp), r.data.message));
                else
                    log(String.format("[%s] %s: %s", timeFormat.format(r.data.timestamp), r.data.sender, r.data.message));
                break;
            }
            case "shutdown":
            {
                log("Server shutdown");
                client.close();
                break;
            }
            default:
                handleUnknownResponse(response);
                break;
            }
        }
    }

    public void handleUnknownResponse(JsonRemoteResponse response)
    {
        if (response.id == null)
            response.id = "";
        if (response.message == null)
            response.message = response.success ? "success" : "failure";
        if (response.data == null)
            log(String.format("%s #%d: %s", response.id, response.rid, response.message));
        else
            log(String.format("%s #%d: %s: %s", response.id, response.rid, response.message, response.data.toString()));
    }

    /* ------------------------------------------------------------ */

    private void queryCapabilities()
    {
        RemoteRequest<Object> request = new RemoteRequest<Object>(RemoteMessageID.QUERY_REMOTE_CAPABILITIES, auth, null);
        JsonRemoteResponse response = client.sendRequestAndWait(request, TIMEOUT);
        if (response == null)
        {
            log("Error: no response");
            return;
        }
        if (!response.success)
        {
            log("Error: " + response.message);
            return;
        }
        log("Capabilities: " + response.data.toString());
    }

    private void pushChat(boolean enable)
    {
        RemoteRequest<PushRequestData> request = new RemoteRequest<PushRequestData>(RemoteMessageID.PUSH_CHAT, auth, new PushRequestData(enable));
        JsonRemoteResponse response = client.sendRequestAndWait(request, TIMEOUT);
        if (response == null)
        {
            log("Error: no response");
            return;
        }
        if (!response.success)
        {
            log("Error: " + response.message);
            return;
        }
        log(response.data != null ? response.data.toString() : response.message);
    }

    /* ------------------------------------------------------------ */

    @FXML
    public void sendInput()
    {
        String line = input.getText();
        input.setText("");
        if (line.isEmpty())
            return;

        if (line.startsWith("/"))
        {
            RemoteRequest<String> request = new RemoteRequest<String>(RemoteMessageID.COMMAND, auth, line.substring(1));
            client.sendRequestSafe(request);
        }
        else
        {
            RemoteRequest<String> request = new RemoteRequest<String>(RemoteMessageID.CHAT, auth, line);
            client.sendRequestSafe(request);
        }
    }

    /* ------------------------------------------------------------ */

    public Server getServer()
    {
        return server;
    }

    public RemoteClient getClient()
    {
        return client;
    }

    public void setTab(ServerTab serverTab)
    {
        this.serverTab = serverTab;
    }

    public ServerTab getTab()
    {
        return serverTab;
    }

}
