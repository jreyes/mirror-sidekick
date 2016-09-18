package com.vaporwarecorp.mirror.sidekick.ui;

import android.app.Activity;
import android.os.Bundle;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.github.jorgecastillo.FillableLoader;
import com.github.jorgecastillo.State;
import com.github.jorgecastillo.listener.OnStateChangeListener;
import com.vaporwarecorp.mirror.sidekick.R;
import com.vaporwarecorp.mirror.sidekick.event.TransmitterEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.vaporwarecorp.mirror.sidekick.event.TransmitterEvent.INTENT_CANCEL;

public class ShareActivity extends Activity implements OnStateChangeListener {
// ------------------------------ FIELDS ------------------------------

    private static final String PATH = "M219.1,33.1 C205.5,35.4,193.5,39.2,182,44.9\n" +
            "C151.9,60.1,139.5,70.4,121.3,95.6\n" +
            "C120.5,96.6,119,99.3,114.3,108 C108.5,118.7,101.6,140.1,99.6,153.3\n" +
            "C98.5,160.3,97.8,171.4,97.6,182 L97.5,192.5 L94.8,192.8\n" +
            "C93.4,193,91.1,194.1,89.7,195.4 C87.3,197.6,87.3,197.7,87.5,215.6\n" +
            "C87.9,239.6,89.6,256,93.2,268.4 C93.8,270.7,94.4,273,94.6,273.7\n" +
            "C95.4,278,100,290,104,298.5 C115.6,323.3,129.2,340,150.5,355.8\n" +
            "C159.9,362.8,177.5,371.9,186.5,374.6 C189.8,375.5,195.8,377.4,199.9,378.7\n" +
            "C213.3,383,230.7,386.4,238.3,386.2 C239.2,386.1,240,386.2,240,386.4\n" +
            "C240,386.6,238.3,388.5,236.2,390.6 C232.9,394.1,229.5,402.4,230.1,405.6\n" +
            "C230.5,408,229.5,408.5,223.7,408.6 C190.8,409,157.2,417.7,151.6,427.1\n" +
            "C150.3,429.3,149.7,433,149.3,439.8 L148.9,449.5 L152.4,451.8\n" +
            "C155.6,454,167.4,458.5,172.3,459.4 C173.5,459.6,178.3,460.5,183,461.4\n" +
            "C187.7,462.3,193.6,463.2,196.3,463.5 C198.9,463.8,202.9,464.2,205.3,464.4\n" +
            "C223.5,466.3,266.4,467,282,465.7 C297.9,464.3,300.5,464.1,305,463.5\n" +
            "C326.2,460.8,344.4,455.8,350.2,451 L353.4,448.3 L353.1,439.5\n" +
            "C353,434.7,352.5,429.8,352,428.6 C350,423.3,339.4,417.2,327.3,414.4\n" +
            "C318.2,412.3,317.5,412.1,311.7,411.5 C308.9,411.2,306.3,410.7,305.9,410.5\n" +
            "C304.9,409.8,288.6,408.7,279.2,408.6 C271.5,408.5,270.9,408.4,271.1,406.5\n" +
            "C271.8,400.9,266.7,390.2,262.3,387.9 C259.1,386.2,259.4,385.5,263.8,385.2\n" +
            "C274.1,384.5,297.9,377.4,311.9,370.9 C343.1,356.4,373.2,326.8,390.7,293.3\n" +
            "C399.5,276.4,407.5,252.7,409.4,237.5 C409.8,234.7,410.2,231.4,410.5,230\n" +
            "C412.1,221.3,412.7,195.8,411.6,184.9 C410.8,175.9,406,170.9,399.3,171.7\n" +
            "C397.5,171.9,396,171.7,395.9,171.3 C395.1,163.1,387.8,142.2,380.9,128\n" +
            "C361.6,88.5,331.7,59.9,292,42.9 C277.1,36.5,270.4,34.6,256,32.4\n" +
            "C247.5,31.1,229.4,31.4,219.1,33.1 Z M110,247.2\n" +
            "C113.1,255.9,123.1,276.1,128.2,284.2 C133.3,292.3,146.1,308,153.9,315.7\n" +
            "C169,330.7,187.4,342.7,209.5,352 C227.1,359.3,255.5,363.1,275,360.7\n" +
            "C283.1,359.7,315.4,350.7,317.4,348.9 C318,348.4,319,348,319.8,348\n" +
            "C321.9,348,319.7,349.8,310.5,355.6 C294.4,365.9,276.5,373.1,261,375.6\n" +
            "C259.1,375.9,257.3,376.3,257,376.5 C255.4,377.5,240.9,378.4,228.5,378.3\n" +
            "C218.8,378.3,212.7,377.8,208.8,376.6 C172.4,366.4,141.4,340,123.7,304.5\n" +
            "C118.2,293.3,111.8,274.7,109.5,263 C109.2,261.6,107.3,248.7,106.6,243.2\n" +
            "C106,239,107.7,241,110,247.2 Z";

    @Bind(R.id.loader)
    FillableLoader loader;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface OnStateChangeListener ---------------------

    @Override
    public void onStateChange(int state) {
        switch (state) {
            case State.FINISHED:
                launchPendingIntent();
                stopAnimation();
                break;
        }
    }

// -------------------------- OTHER METHODS --------------------------

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(TransmitterEvent event) {
        switch (event.getType()) {
            case INTENT_CANCEL:
                stopAnimation();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAnimation();
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

    private void launchPendingIntent() {
        startActivity(getIntent().getParcelableExtra("pendingIntent"));
    }

    private void startAnimation() {
        loader.reset();
        loader.setSvgPath(PATH);
        loader.setOnStateChangeListener(this);
        loader.setOnClickListener(v -> stopAnimation());
        loader.postDelayed(() -> loader.start(), 250);
    }

    private void stopAnimation() {
        finish();
    }
}
