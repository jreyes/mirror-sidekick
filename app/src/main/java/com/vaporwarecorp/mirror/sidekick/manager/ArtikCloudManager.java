package com.vaporwarecorp.mirror.sidekick.manager;

import android.content.Intent;
import android.net.Uri;
import cloud.artik.model.Acknowledgement;
import cloud.artik.model.ActionOut;
import cloud.artik.model.MessageOut;
import cloud.artik.model.WebSocketError;
import cloud.artik.websocket.ArtikCloudWebSocketCallback;
import cloud.artik.websocket.FirehoseWebSocket;
import com.vaporwarecorp.mirror.sidekick.service.TransmitterService;
import com.vaporwarecorp.mirror.sidekick.ui.ShareActivity;
import timber.log.Timber;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import static android.content.Intent.*;

public class ArtikCloudManager {
// ------------------------------ FIELDS ------------------------------

    private FirehoseWebSocket mFirehoseWS;
    private TransmitterService transmitterService;

// --------------------------- CONSTRUCTORS ---------------------------

    public ArtikCloudManager(TransmitterService transmitterService) {
        this.transmitterService = transmitterService;
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * Creates a websocket /live connection
     */
    public void startMessaging() {
        createFirehoseWebsocket();
        try {
            mFirehoseWS.connect();
        } catch (IOException e) {
            Timber.e(e, e.getMessage());
        }
    }

    /**
     * Closes a websocket /live connection
     */
    public void stopMessaging() {
        if (mFirehoseWS != null) {
            new Thread(() -> {
                try {
                    mFirehoseWS.close();
                    mFirehoseWS = null;
                } catch (IOException e) {
                    Timber.e(e, e.getMessage());
                }
            }).start();
        }
    }

    private void createFirehoseWebsocket() {
        try {
            final String accessToken = transmitterService.getAccessToken();
            final String deviceId = transmitterService.getDeviceId();
            mFirehoseWS = new FirehoseWebSocket(accessToken, deviceId, null, null, null, new ArtikCloudWebSocketCallback() {
                @Override
                public void onOpen(int i, String s) {
                    Timber.d("FirehoseWebSocket: onOpen()");
                }

                @Override
                public void onMessage(MessageOut messageOut) {
                    final Map<String, Object> data = messageOut.getData();
                    if (data == null) {
                        return;
                    }

                    final String action = String.valueOf(data.get("ACTION"));
                    if (Intent.ACTION_SHUTDOWN.equals(action)) {
                        stopPendingIntent();
                    } else {
                        data.remove("ACTION");
                        startPendingIntent(action, data);
                    }
                }

                @Override
                public void onAction(ActionOut actionOut) {
                }

                @Override
                public void onAck(Acknowledgement acknowledgement) {
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Timber.w("mFirehoseWS is closed. code: %s; reason: %s", code, reason);
                }

                @Override
                public void onError(WebSocketError ex) {
                    Timber.e("mFirehoseWS error: %s", ex.toString());
                    transmitterService.stopAdvertising();
                }

                @Override
                public void onPing(long timestamp) {
                }
            });
        } catch (URISyntaxException | IOException e) {
            Timber.e(e, e.getMessage());
        }
    }

    private Intent getViewIntent(Map<String, Object> data) {
        final String url = String.valueOf(data.get("URL"));
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (data.get("CLASS_NAME_KEY") != null) {
            final String classNameKey = String.valueOf(data.get("CLASS_NAME_KEY"));
            final String classNameValue = String.valueOf(data.get("CLASS_NAME_VALUE"));
            intent.setClassName(classNameKey, classNameValue);
        }
        intent.putExtra("autoplay", true);
        return intent;
    }

    private void startPendingIntent(String action, Map<String, Object> data) {
        Intent intent;
        switch (action) {
            case Intent.ACTION_VIEW:
                intent = getViewIntent(data);
                break;
            default:
                Timber.e("no valid action found : %s", data.toString());
                return;
        }

        final Intent shareIntent = new Intent(transmitterService, ShareActivity.class);
        shareIntent.setFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP | FLAG_ACTIVITY_NEW_TASK);
        shareIntent.putExtra("pendingIntent", intent);
        transmitterService.startActivity(shareIntent);
    }

    private void stopPendingIntent() {
        transmitterService.cancelIntent();
    }
}
