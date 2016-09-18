package com.vaporwarecorp.mirror.sidekick.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.vaporwarecorp.mirror.sidekick.R;
import com.vaporwarecorp.mirror.sidekick.event.TransmitterEvent;
import com.vaporwarecorp.mirror.sidekick.manager.ArtikOAuthManager;
import com.vaporwarecorp.mirror.sidekick.service.TransmitterService;
import com.vaporwarecorp.mirror.sidekick.util.PermissionUtil;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.BleNotAvailableException;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import java.util.List;

import static com.vaporwarecorp.mirror.sidekick.event.TransmitterEvent.*;

public class MainActivity extends Activity {
// ------------------------------ FIELDS ------------------------------

    @Bind(R.id.pulse_ring)
    ImageView pulsingRing;
    @Bind(R.id.start_scan_button)
    ImageButton startButton;
    @Bind(R.id.scan_circle)
    ImageView startButtonOuterCircle;
    @Bind(R.id.stop_scan_button)
    ImageButton stopButton;

    private String accessToken;
    private ArtikOAuthManager artikOAuthManager;
    private boolean authenticationChecked;
    private BeaconManager beaconManager;
    private boolean isTransmitting;
    private boolean permissionsChecked;
    private boolean transmitterServiceChecked;

// -------------------------- OTHER METHODS --------------------------

    @Override
    public void onDestroy() {
        ButterKnife.unbind(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(TransmitterEvent event) {
        switch (event.getType()) {
            case TRANSMITTER_STARTED:
                if (!isTransmitting) {
                    isTransmitting = true;
                    startAnimation();
                }
                break;
            case TRANSMITTER_STOPPED:
                if (isTransmitting) {
                    isTransmitting = false;
                    stopAnimation();
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        tryToStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @OnClick({R.id.start_scan_button, R.id.stop_scan_button, R.id.scan_circle})
    void onScanButtonClick() {
        toggleTransmitting();
    }

    private void checkAuthentication() {
        // create OAuth manager if it doesn't exist
        if (artikOAuthManager == null) {
            artikOAuthManager = ArtikOAuthManager.newInstance(this);
        }
        // get authorization
        artikOAuthManager.authorizeImplicitly()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Timber.d("onCompleted");
                        tryToStart();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "Error trying to authenticate");
                    }

                    @Override
                    public void onNext(String s) {
                        accessToken = s;
                        authenticationChecked = true;
                    }
                });
    }

    private void checkPermissions() {
        final List<String> neededPermissions = PermissionUtil.checkPermissions(this);
        if (neededPermissions.isEmpty()) {
            permissionsChecked = true;
            tryToStart();
        } else {
            PermissionUtil.requestPermissions(this, neededPermissions);
        }
    }

    private void checkTransmitterService() {
        if (beaconManager == null) {
            // Getting instance of beacon manager.
            beaconManager = BeaconManager.getInstanceForApplication(this);
        }
        if (!isTransmitterServiceRunning()) {
            final Intent intent = new Intent(this, TransmitterService.class);
            intent.putExtra(TransmitterService.ACCESS_TOKEN, accessToken);
            startService(intent);
        }
        transmitterServiceChecked = true;
        tryToStart();
    }

    private boolean isTransmitterServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (TransmitterService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void notifyTransmittingNotSupported() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.transmitting_not_supported))
                .setMessage(getString(R.string.transmitting_not_supported_message))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void pulseAnimation() {
        AnimationSet set = new AnimationSet(false);
        set.addAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_pulse));
        pulsingRing.startAnimation(set);
    }

    private void requestBluetooth() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.bluetooth_not_enabled))
                .setMessage(getString(R.string.please_enable_bluetooth))
                .setPositiveButton(R.string.settings, (dialog, which) -> {
                    // Initializing intent to go to bluetooth settings.
                    Intent bltSettingsIntent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                    startActivity(bltSettingsIntent);
                })
                .show();
    }

    private void startAnimation() {
        startButtonOuterCircle.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_zoom_in));
        startButton.setImageResource(R.drawable.ic_circle);
        stopButton.setVisibility(View.VISIBLE);
        pulseAnimation();
    }

    private void startTransmitting() {
        try {
            if (!beaconManager.checkAvailability()) {
                requestBluetooth();
            } else {
                if (!(BeaconTransmitter.checkTransmissionSupported(this) == BeaconTransmitter.SUPPORTED)) {
                    notifyTransmittingNotSupported();
                } else if (!isTransmitting) {
                    EventBus.getDefault().post(new TransmitterEvent(START_TRANSMITTING));
                }
            }
        } catch (BleNotAvailableException bleNotAvailableException) {
            notifyTransmittingNotSupported();
        }
    }

    private void stopAnimation() {
        startButtonOuterCircle.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_zoom_out));
        startButton.setImageResource(R.drawable.ic_button_transmit);
        stopButton.setVisibility(View.INVISIBLE);
        pulsingRing.clearAnimation();
    }

    private void stopTransmitting() {
        if (isTransmitting) {
            EventBus.getDefault().post(new TransmitterEvent(STOP_TRANSMITTING));
        }
    }

    private void toggleTransmitting() {
        if (!isTransmitting) startTransmitting();
        else stopTransmitting();
    }

    private void tryToStart() {
        if (!permissionsChecked) {
            checkPermissions();
            return;
        }
        if (!authenticationChecked) {
            checkAuthentication();
            return;
        }
        if (!transmitterServiceChecked) {
            checkTransmitterService();
        }
    }

    private void updateUI() {
        EventBus.getDefault().post(new TransmitterEvent(TRANSMITTER_STATUS));
    }
}
