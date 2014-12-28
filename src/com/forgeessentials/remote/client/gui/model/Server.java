package com.forgeessentials.remote.client.gui.model;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;

import com.google.gson.annotations.Expose;

/**
 * Server data class
 */
public class Server {

    private StringProperty name;

    private StringProperty address;

    private IntegerProperty port;

    private StringProperty username;

    private StringProperty passkey;

    private BooleanProperty ssl;

    public final List<Server> subServers = new ArrayList<Server>();

    @Expose(serialize = false, deserialize = false)
    public final ListProperty<Server> subServersProperty = new SimpleListProperty<Server>(FXCollections.observableArrayList(subServers));

    public Server(Server server)
    {
        this.name = server.name == null ? null : new SimpleStringProperty(server.name.get());
        this.address = server.address == null ? null : new SimpleStringProperty(server.address.get());
        this.port = server.port == null ? null : new SimpleIntegerProperty(server.port.get());
        this.username = server.username == null ? null : new SimpleStringProperty(server.username.get());
        this.passkey = server.passkey == null ? null : new SimpleStringProperty(server.passkey.get());
        this.ssl = server.ssl == null ? null : new SimpleBooleanProperty(server.ssl.get());
        for (Server s : server.subServers)
            subServers.add(new Server(s));
    }

    public Server(String name)
    {
        this.name = new SimpleStringProperty(name);
    }

    public Server(String name, String address, int port, String username, String passkey)
    {
        this.name = new SimpleStringProperty(name);
        this.address = new SimpleStringProperty(address);
        this.port = new SimpleIntegerProperty(port);
        this.username = new SimpleStringProperty(username);
        this.passkey = new SimpleStringProperty(passkey);
        this.ssl = new SimpleBooleanProperty(false);
    }

    public StringProperty nameProperty()
    {
        if (name == null)
            name = new SimpleStringProperty();
        return name;
    }

    public StringProperty addressProperty()
    {
        if (address == null && !isFolder())
            address = new SimpleStringProperty();
        return address;
    }

    public IntegerProperty portProperty()
    {
        if (port == null)
        {
            if (!isFolder())
                port = new SimpleIntegerProperty();
            else
                port = null;
        }
        return port;
    }

    public StringProperty usernameProperty()
    {
        if (username == null && !isFolder())
            username = new SimpleStringProperty();
        return username;
    }

    public StringProperty passkeyProperty()
    {
        if (passkey == null && !isFolder())
            passkey = new SimpleStringProperty();
        return passkey;
    }

    public BooleanProperty sslProperty()
    {
        if (ssl == null && !isFolder())
            ssl = new SimpleBooleanProperty(false);
        return ssl;
    }

    public boolean isFolder()
    {
        return address == null || address.get() == null;
    }

}
