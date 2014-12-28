package com.forgeessentials.remote.client.gui.features;

import com.forgeessentials.remote.client.RemoteResponse.JsonRemoteResponse;


public class PermissionsController extends FeatureController {

    @Override
    public void init()
    {
        System.out.println("Init permissions feature");
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
