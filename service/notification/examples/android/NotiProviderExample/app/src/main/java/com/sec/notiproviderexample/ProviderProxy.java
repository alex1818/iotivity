/*
//******************************************************************
//
// Copyright 2016 Samsung Electronics All Rights Reserved.
//
//-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
//-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
 */

package com.sec.notiproviderexample;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.iotivity.base.ModeType;
import org.iotivity.base.OcPlatform;
import org.iotivity.base.OcResourceHandle;
import org.iotivity.base.PlatformConfig;
import org.iotivity.base.QualityOfService;
import org.iotivity.base.ServiceType;
import org.iotivity.service.ns.provider.*;
import org.iotivity.service.ns.common.*;


import java.util.HashMap;

public class ProviderProxy
        implements ProviderService.OnSubscriptionListener, ProviderService.OnSyncInfoListener{

    private static final String TAG = "NS_PROVIDER_PROXY";

    private Context mContext = null;
    private OcResourceHandle mResourceHandle;   //resource handle
    private ProviderService ioTNotification = null;
    private HashMap<String, Integer> msgMap;

    private Handler mHandler = null;

    private static final int MESSAGE_SUBSCRIPTION = 1;
    private static final int MESSAGE_SYNC = 2;

    private static final int SYNC_READ = 0;
    private static final int SYNC_DISMISS = 1;
    private static final int SYNC_UNREAD = 2;

    public ProviderProxy(Context context) {
        Log.i(TAG, "Create providerProxy Instance");

        this.msgMap = new HashMap<>();
        this.mContext = context;
        ioTNotification =  ProviderService.getInstance();
    }

    public void setHandler(Handler handler)
    {
        this.mHandler = handler;
    }

    private void configurePlatform() {

        PlatformConfig platformConfig = new PlatformConfig(
                mContext,
                ServiceType.IN_PROC,
                ModeType.CLIENT_SERVER,
                "0.0.0.0", // By setting to "0.0.0.0", it binds to all available interfaces
                0,         // Uses randomly available port
                QualityOfService.LOW
        );

        Log.i(TAG, "Configuring platform.");
        OcPlatform.Configure(platformConfig);
        try {
            OcPlatform.stopPresence(); // Initialize OcPlatform
        } catch(Exception e) {
            Log.e(TAG, "Exception: stopping presence when configuration step: " + e);
        }
        Log.i(TAG, "Configuration done Successfully");
    }

    public void Start(boolean policy)
    {
        Log.i(TAG, "Start ProviderService -IN");
        configurePlatform();
        try{
            int result =  ioTNotification.Start(policy, this, this);
            Log.i(TAG, "Notification Start: " + result);
        }
        catch(Exception e){

        }

        Log.i(TAG, "Start ProviderService - OUT");
    }

    public void Stop() {
        Log.i(TAG, "Stop ProviderService - IN");
        try {
            OcPlatform.stopPresence();
        } catch (Exception e) {
            Log.e(TAG, "Exception: stopping presence when terminating NS server: " + e);
        }
        try{
            int result =  ioTNotification.Stop();
            Log.i(TAG, "Notification Stop: " + result);
        }
        catch(Exception e){

        }

        Log.i(TAG, "Stop ProviderService - OUT");
    }

    public void SendMessage(Message notiMessage) {
        Log.i(TAG, "SendMessage ProviderService - IN");

        try{
            int result =  ioTNotification.SendMessage(notiMessage);
            Log.i(TAG, "Notification Send Message: " + result);
        }
        catch(Exception e){

        }

        Log.i(TAG, "SendMessage ProviderService - OUT");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, "Notification sent", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void SendSyncInfo(long messageId, SyncInfo.SyncType syncType) {
        Log.i(TAG, "SendSyncInfo ProviderService - IN");
        if(msgMap.containsKey(messageId)) {
            if(msgMap.get(messageId) == SYNC_UNREAD)
            {
                try{
                    ioTNotification.SendSyncInfo(messageId,syncType );
                    Log.i(TAG, "Notification Sync " );
                }
                catch(Exception e) {

                }
                Log.i(TAG, "SendSyncInfo ProviderService - OUT");
                msgMap.put("ID: "+messageId, SYNC_READ);
            }
        }
    }

    public void EnableRemoteService(String servAdd) {
        Log.i(TAG, "EnableRemoteService ProviderService - IN");
        try{
            int result = ioTNotification.EnableRemoteService(servAdd);
            Log.i(TAG, "Notification EnableRemoteService: "+ result );
        }
        catch(Exception e) {

        }
        Log.i(TAG, "EnableRemoteService ProviderService - OUT");
    }

    public void DisableRemoteService(String servAdd) {
        Log.i(TAG, "DisableRemoteService ProviderService - IN");
        try{
            int result = ioTNotification.DisableRemoteService(servAdd);
            Log.i(TAG, "Notification DisableRemoteService: "+ result );
        }
        catch(Exception e) {

        }
        Log.i(TAG, "DisableRemoteService ProviderService - OUT");
    }

    public void AcceptSubscription(Consumer consumer, boolean accepted)
    {
        Log.i(TAG,"AcceptSubscription ProviderService - IN");
        try{
            int result = consumer.AcceptSubscription(consumer, accepted);
            Log.i(TAG, "Notification AcceptSubscription: "+result );
        }
        catch(Exception e) {

        }
        Log.i(TAG, "AcceptSubscription ProviderService - OUT");
    }

    @Override
    public void onConsumerSubscribed(Consumer consumer) {
        Log.i(TAG, "onConsumerSubscribed - IN");
        AcceptSubscription(consumer, true);
        Log.i(TAG, "onConsumerSubscribed - OUT");
    }

    @Override
    public void onMessageSynchronized(SyncInfo syncInfo) {
        Log.i(TAG, "Received SyncInfo with messageID: "+syncInfo.getMessageId());
    }

    public HashMap<String, Integer> getMsgMap() {
        return msgMap;
    }
}
