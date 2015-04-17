package com.forgeessentials.remote.client.gui;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Region;

import com.forgeessentials.remote.client.RemoteClient;
import com.forgeessentials.remote.client.RemoteRequest;
import com.forgeessentials.remote.client.RemoteRequest.PushRequestData;
import com.forgeessentials.remote.client.RemoteResponse;
import com.forgeessentials.remote.client.RemoteResponse.JsonRemoteResponse;
import com.forgeessentials.remote.client.RequestAuth;
import com.forgeessentials.remote.client.gui.MainController.ServerTab;
import com.forgeessentials.remote.client.gui.features.FeatureController;
import com.forgeessentials.remote.client.gui.model.Server;
import com.forgeessentials.remote.client.network.PushChatHandler;

public class ServerController implements Runnable {

    public static final int TIMEOUT = 30 * 1000;

    @FXML
    protected Region root;

    @FXML
    protected ListView<String> log;

    @FXML
    protected TabPane features;

    private RemoteClient client;

    private RequestAuth auth;

    private Server server;

    private ServerTab serverTab;

    /* ------------------------------------------------------------ */

    public boolean init(Server server)
    {
        this.server = server;
        try
        {
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
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
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
            case PushChatHandler.ID:
            {
                RemoteResponse<PushChatHandler.Response> r = client.transformResponse(response, PushChatHandler.Response.class);
                log(String.format("Chat (%s): %s", r.data.username, r.data.message));
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
        if (response.success)
        {
            if (response.message == null)
                response.message = "success";
            if (response.data == null)
                log(String.format("EAT Response %s:#%d (%s)", response.id, response.rid, response.message));
            else
                log(String.format("EAT Response %s:#%d (%s): %s", response.id, response.rid, response.message, response.data.toString()));
        }
        else
        {
            if (response.message == null)
                response.message = "failure";
            if (response.data == null)
                log(String.format("EAT Response %s:#%d (%s)", response.id, response.rid, response.message));
            else
                log(String.format("EAT Response %s:#%d (%s): %s", response.id, response.rid, response.message, response.data.toString()));
        }
    }

    /* ------------------------------------------------------------ */

    private void queryCapabilities()
    {
        RemoteRequest<Object> request = new RemoteRequest<Object>("query_remote_capabilities", auth, null);
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
        RemoteRequest<PushRequestData> request = new RemoteRequest<PushRequestData>("push_chat", auth, new PushRequestData(enable));
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
