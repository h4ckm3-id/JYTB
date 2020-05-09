/*
 *     JYTBot, YouTube viewer bot for educational purposes
 *     Copyright (C) 2019  Mark Tripoli (triippztech.com)
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.triippztech.app.models;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.triippztech.app.dao.HttpRequestSender;
import com.triippztech.app.dao.RequestSender;
import com.triippztech.app.http.Request;
import com.triippztech.app.http.Response;
import com.triippztech.app.utils.Log;
import com.triippztech.app.utils.UrlUtil;
import org.openqa.selenium.Proxy;
// import com.triippztech.app.services.BotWorker;

import java.io.IOException;
import java.util.*;

public class Proxies {
    private final String protoType = "https";

    private PubProxies proxies;
    private Set<Datum> usedProxies;
    private String workerName;
    private String workerColor;
    private String apiKey;
    private String proxyType;
    private Datum currentProxyModel;
    private Proxy currentProxy;

    private RequestSender sender;
    private Gson gson;
    private FreeProxies Freeproxies;

    public Proxies(String workerName, String apiKey, String workerColor) {
        this.workerName = workerName;
        this.apiKey = apiKey;
        this.workerColor = workerColor;
        this.usedProxies = new HashSet<>();


        this.sender = new HttpRequestSender();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.generateProxies();
    }

    // constructor for free proxy.
    public Proxies(String workerName, String workerColor) {
        this.workerName = workerName;
        this.workerColor = workerColor;
        this.usedProxies = new HashSet<>();


        this.sender = new HttpRequestSender();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.generateFreeProxies();
    }

    @SuppressWarnings("Duplicates")
    public void generateFreeProxies()
    {
        String url = "https://raw.githubusercontent.com/scidam/proxy-list/master/proxy.json";
        this.setProxyType("FREE");

        Request request = new Request(url);
            try {
                Response response = sender.sendRequest(request);
                Freeproxies = gson.fromJson(response.getBody(), FreeProxies.class);
            } catch (JsonSyntaxException | IOException e) {
                Log.WERROR(workerName, workerColor, e.getMessage());
                generateFreeProxies();
    }
    // Set the first
    this.loadNewProxy();
}
    @SuppressWarnings("Duplicates")
    public void generateProxies()

    {
        HashMap<String, String> params = new HashMap<>();
        params.put("api", apiKey);
        params.put("limit", "20");
        params.put("https", "true");
        params.put("format", "json");

        this.setProxyType("PAID");

        Request request = new Request(UrlUtil.buildUrlQuery(Constants.SearchEndpoint, params));

        try {
            Response response = sender.sendRequest(request);
            proxies = gson.fromJson(response.getBody(), PubProxies.class);
        } catch (JsonSyntaxException | IOException e) {
            Log.WERROR(workerName, workerColor, e.getMessage());
            generateProxies();
        }

        // Set the first
        this.loadNewProxy();
    }

    public void rotateProxies()
    {
        Log.WWARN(workerName, workerColor,"Rotating proxies");

    //    String px = this.getProxyType();
        this.usedProxies.add(this.currentProxyModel);

        // Load a new one
        this.loadNewProxy();
    }

    private void refreshProxies()
    {
        this.proxies = null;
        String px = this.getProxyType();

        if ( px == "FREE" ) {
            Log.WWARN(workerName, workerColor,"Refreshing new Free Proxy ...");
            this.generateFreeProxies();
        } else {
            Log.WWARN(workerName, workerColor,"Refreshing new Paid Proxy ...");
            this.generateProxies();
        }
    }

    private void loadNewProxy() {
        Log.WWARN(workerName, workerColor,"Load new proxies");

        this.usedProxies.add(this.getCurrentProxyModel());
        String px = this.getProxyType();

        if ( px == "PAID" ) {
            Datum proxy = randomProxy();
            if (isUsed(proxy)) {
                try {
                    Log.WWARN(workerName, workerColor, "proxy already used");
                    Thread.sleep(3000);
                    Log.WWARN(workerName, workerColor, "Refreshing Paid Proxy list...");
                    this.refreshProxies();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                this.setCurrentProxyModel(proxy);
                this.setCurrentProxy(proxy);
            }
        } else {
            Datum proxy = randomFreeProxy();
            if (isUsed(proxy)) {
                try {
                    Log.WWARN(workerName, workerColor, "proxy already used");
                    Thread.sleep(3000);
                    Log.WWARN(workerName, workerColor, "Refreshing Free Proxy list...");
                    this.refreshProxies();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                this.setCurrentProxyModel(proxy);
                this.setCurrentProxy(proxy);
            }
        }

    }


    public void setProxyType(String proxyType) {
        this.proxyType = proxyType;
    }
    public String getProxyType() {
        return this.proxyType;
    }

    private Boolean isUsed(Datum proxy)
    {
        return usedProxies.contains(proxy);
    }

    private Datum randomProxy() {
        return proxies.getData().get(new Random().nextInt(proxies.getData().size()));
    }

    private Datum randomFreeProxy() {
        return Freeproxies.getData().get(new Random().nextInt(Freeproxies.getData().size()));
    }

    public List<Datum> getProxies() {
        return proxies.getData();
    }
    public List<Datum> getFreeProxies() {
        return Freeproxies.getData();
    }

    public Set<Datum> getUsedProxies() {
        return usedProxies;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Datum getCurrentProxyModel() {
        return currentProxyModel;
    }

    public void setCurrentProxyModel(Datum currentProxyModel) {
        this.currentProxyModel = currentProxyModel;
    }

    public Proxy getCurrentProxy() {
        return currentProxy;
    }

    public void setCurrentProxy(Datum currentProxy) {
        Proxy proxy = new Proxy();
        proxy.setHttpProxy(currentProxy.getIpPort());
        proxy.setSslProxy(currentProxy.getIpPort());
    }
}
