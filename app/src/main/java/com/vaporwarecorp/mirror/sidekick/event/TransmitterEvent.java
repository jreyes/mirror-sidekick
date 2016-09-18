package com.vaporwarecorp.mirror.sidekick.event;

public class TransmitterEvent {
// ------------------------------ FIELDS ------------------------------

    public static final int INTENT_CANCEL = 5;
    public static final int START_TRANSMITTING = 0;
    public static final int STOP_TRANSMITTING = 1;
    public static final int TRANSMITTER_STARTED = 2;
    public static final int TRANSMITTER_STATUS = 4;
    public static final int TRANSMITTER_STOPPED = 3;

    private final int type;

// --------------------------- CONSTRUCTORS ---------------------------

    public TransmitterEvent(int type) {
        this.type = type;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public int getType() {
        return type;
    }
}
