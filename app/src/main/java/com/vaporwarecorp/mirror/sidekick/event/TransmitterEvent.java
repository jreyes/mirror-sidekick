package com.vaporwarecorp.mirror.sidekick.event;

public class TransmitterEvent {
// ------------------------------ FIELDS ------------------------------

    public static final int INTENT_CANCEL = 5;
    public static final int START_TRANSMITTING = 0;
    public static final int STOP_TRANSMITTING = 1;
    public static final int TRANSMITTER_STARTED = 2;
    public static final int TRANSMITTER_STATUS = 4;
    public static final int TRANSMITTER_STOPPED = 3;

    private final String accessToken;
    private final int type;

// --------------------------- CONSTRUCTORS ---------------------------

    public TransmitterEvent(int type) {
        this(type, null);
    }

    public TransmitterEvent(int type, String accessToken) {
        this.type = type;
        this.accessToken = accessToken;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public String getAccessToken() {
        return accessToken;
    }

    public int getType() {
        return type;
    }
}
