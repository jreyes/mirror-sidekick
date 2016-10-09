package com.vaporwarecorp.mirror.sidekick.service;

import android.app.Service;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.vaporwarecorp.mirror.sidekick.R;
import com.vaporwarecorp.mirror.sidekick.event.TransmitterEvent;
import com.vaporwarecorp.mirror.sidekick.manager.ArtikCloudManager;
import com.vaporwarecorp.mirror.sidekick.manager.TransmitterNotificationManager;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import timber.log.Timber;

import static com.vaporwarecorp.mirror.sidekick.event.TransmitterEvent.*;

public class TransmitterService extends Service {
// ------------------------------ FIELDS ------------------------------

    public static final String ACCESS_TOKEN = "ACCESS_TOKEN";

    private String accessToken;
    private ArtikCloudManager artikCloudManager;
    private BeaconTransmitter beaconTransmitter;
    private TransmitterNotificationManager transmitterNotificationManager;

// --------------------- GETTER / SETTER METHODS ---------------------

    public String getAccessToken() {
        return accessToken;
    }

// -------------------------- OTHER METHODS --------------------------

    public void cancelIntent() {
        EventBus.getDefault().post(new TransmitterEvent(INTENT_CANCEL));
    }

    public String getDeviceId() {
        return getString(R.string.artik_device_id);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        initializeTransmitter();
        transmitterNotificationManager = new TransmitterNotificationManager(this);
        artikCloudManager = new ArtikCloudManager(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        transmitterNotificationManager.stopNotification();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(TransmitterEvent event) {
        switch (event.getType()) {
            case START_TRANSMITTING:
                Timber.d("START_TRANSMITTING");
                if (!beaconTransmitter.isStarted()) {
                    startAdvertising();
                }
                break;
            case STOP_TRANSMITTING:
                Timber.d("STOP_TRANSMITTING");
                if (beaconTransmitter.isStarted()) {
                    stopAdvertising();
                }
                break;
            case TRANSMITTER_STATUS:
                if (beaconTransmitter.isStarted()) {
                    Timber.d("TRANSMITTER_STATUS - TRANSMITTER_STARTED");
                    EventBus.getDefault().post(new TransmitterEvent(TRANSMITTER_STARTED));
                } else {
                    Timber.d("TRANSMITTER_STATUS - TRANSMITTER_STOPPED");
                    EventBus.getDefault().post(new TransmitterEvent(TRANSMITTER_STOPPED));
                }
                break;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getStringExtra(ACCESS_TOKEN) != null) {
            accessToken = intent.getStringExtra(ACCESS_TOKEN);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void startAdvertising() {
        beaconTransmitter.startAdvertising();
        transmitterNotificationManager.startNotification();
        artikCloudManager.startMessaging();
        EventBus.getDefault().post(new TransmitterEvent(TRANSMITTER_STARTED));
    }

    public void stopAdvertising() {
        beaconTransmitter.stopAdvertising();
        transmitterNotificationManager.stopNotification();
        artikCloudManager.stopMessaging();
        EventBus.getDefault().post(new TransmitterEvent(TRANSMITTER_STOPPED));
    }

    private void initializeTransmitter() {
        Beacon beacon = new Beacon.Builder()
                .setId1(getString(R.string.artik_beacon_id))
                .setId2("0")
                .setId3("1")
                .setManufacturer(0x004C)
                .setTxPower(-59)
                .build();

        BeaconParser beaconParser = new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");
        beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        beaconTransmitter.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        beaconTransmitter.setAdvertiseTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW);
        beaconTransmitter.setBeacon(beacon);
    }
}
