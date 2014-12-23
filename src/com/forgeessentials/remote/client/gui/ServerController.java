package com.forgeessentials.remote.client.gui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

import com.forgeessentials.remote.client.RemoteClient;
import com.forgeessentials.remote.client.RemoteRequest;
import com.forgeessentials.remote.client.RemoteRequest.PushRequestData;
import com.forgeessentials.remote.client.RemoteResponse;
import com.forgeessentials.remote.client.RequestAuth;
import com.forgeessentials.remote.client.data.PushChatHandler;
import com.forgeessentials.remote.client.gui.MainController.ServerTab;
import com.forgeessentials.remote.client.gui.model.Server;
import com.google.gson.JsonElement;

public class ServerController implements Initializable, Runnable {

    public static final int TIMEOUT = 30 * 1000;

    @FXML
    BorderPane root;

    @FXML
    ListView<String> log;

    @FXML
    TabPane features;

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
            if (!server.usernameProperty().isEmpty().get())
                auth = new RequestAuth(server.usernameProperty().get(), server.passkeyProperty().get());
            else
                auth = null;
            new Thread(this).start();
            queryCapabilities();
            pushChat(true);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
    }

    private void log(final String string)
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

    @Override
    public void run()
    {
        while (!client.isClosed())
        {
            RemoteResponse<JsonElement> response = client.getNextResponse(0);
            if (response != null)
            {
                if (response.id == null)
                    handleUnknownMessage(response);
                else
                {
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
                        handleUnknownMessage(response);
                        break;
                    }
                }
            }
        }
    }

    public void handleUnknownMessage(RemoteResponse<JsonElement> response)
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

    private void queryCapabilities()
    {
        RemoteRequest<Object> request = new RemoteRequest<Object>("query_remote_capabilities", auth, null);
        RemoteResponse<JsonElement> response = client.sendRequestAndWait(request, TIMEOUT);
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
        log(response.data.toString());
    }

    private void pushChat(boolean enable)
    {
        RemoteRequest<PushRequestData> request = new RemoteRequest<PushRequestData>("push_chat", auth, new PushRequestData(enable));
        RemoteResponse<JsonElement> response = client.sendRequestAndWait(request, TIMEOUT);
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

    public void close()
    {
        client.close();
    }

}
