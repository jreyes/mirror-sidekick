package com.vaporwarecorp.mirror.sidekick.manager;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import com.vaporwarecorp.mirror.sidekick.R;
import com.vaporwarecorp.mirror.sidekick.service.TransmitterService;
import com.vaporwarecorp.mirror.sidekick.ui.MainActivity;
import timber.log.Timber;

public class TransmitterNotificationManager extends BroadcastReceiver {
// ------------------------------ FIELDS ------------------------------

    private static final String ACTION_STOP_TRANSMITTING = "STOP_TRANSMITTING";
    private static final int NOTIFICATION_ID = 412;
    private static final int REQUEST_CODE = 100;

    private final int notificationColor;
    private final NotificationManagerCompat notificationManager;
    private boolean started;
    private final PendingIntent stopTransmitterIntent;
    private final TransmitterService service;

// --------------------------- CONSTRUCTORS ---------------------------

    public TransmitterNotificationManager(TransmitterService transmitterService) {
        service = transmitterService;

        notificationColor = ActivityCompat.getColor(service, R.color.colorPrimary);
        notificationManager = NotificationManagerCompat.from(service);

        stopTransmitterIntent = PendingIntent.getBroadcast(service, REQUEST_CODE,
                new Intent(ACTION_STOP_TRANSMITTING).setPackage(service.getPackageName()),
                PendingIntent.FLAG_CANCEL_CURRENT);

        notificationManager.cancelAll();
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        switch (action) {
            case ACTION_STOP_TRANSMITTING:
                service.stopAdvertising();
                break;
            default:
                Timber.w("Unknown intent ignored. Action=%s", action);
        }
    }

    public void startNotification() {
        if (!started) {
            // The notification must be updated after setting started to true
            Notification notification = createNotification();
            if (notification != null) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_STOP_TRANSMITTING);

                service.registerReceiver(this, filter);
                service.startForeground(NOTIFICATION_ID, notification);

                started = true;
            }
        }
    }

    public void stopNotification() {
        if (started) {
            started = false;
            try {
                notificationManager.cancel(NOTIFICATION_ID);
                service.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                // ignore if the receiver is not registered.
            }
            service.stopForeground(true);
        }
    }

    private PendingIntent createContentIntent() {
        Intent intent = new Intent(service, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(service, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(service)
                .addAction(new NotificationCompat.Action(
                        R.drawable.ic_pause_white_24dp,
                        service.getString(R.string.stop_transmitting),
                        stopTransmitterIntent
                ))
                .setStyle(new NotificationCompat.MediaStyle().setShowActionsInCompactView(0))
                .setColor(notificationColor)
                .setSmallIcon(R.drawable.ic_tap_and_play_white_24dp)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(createContentIntent())
                .setContentTitle(service.getString(R.string.transmitter_on))
                .build();
    }
}
