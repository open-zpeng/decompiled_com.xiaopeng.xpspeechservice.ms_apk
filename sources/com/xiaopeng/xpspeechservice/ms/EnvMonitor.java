package com.xiaopeng.xpspeechservice.ms;

import android.car.Car;
import android.car.CarNotConnectedException;
import android.car.hardware.CarPropertyValue;
import android.car.hardware.power.CarPowerManager;
import android.car.hardware.vcu.CarVcuManager;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import com.xiaopeng.xpspeechservice.ms.bean.CarSpeed;
import com.xiaopeng.xpspeechservice.ms.bean.NetworkState;
import com.xiaopeng.xpspeechservice.ms.bean.PowerState;
import com.xiaopeng.xpspeechservice.utils.LogUtils;
import java.util.Arrays;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
/* loaded from: classes.dex */
public class EnvMonitor {
    private static final int MSG_UPDATE_CAR_SPEED = 101;
    private static final String TAG = "EnvMonitor";
    private static final int mCarSpeedUpdateInterval = 300;
    private Car mCar;
    private final ServiceConnection mCarConnectionCb;
    private CarPowerManager mCarPowerManager;
    private final CarPowerManager.CarPowerStateListener mCarPowerStateListener;
    private int mCarSpeed;
    private final CarVcuManager.CarVcuEventCallback mCarVcuEventCallback;
    private CarVcuManager mCarVcuManager;
    private EventBus mEventBus;
    private EventHandler mEventHandler;
    private HandlerThread mEventThread;
    private boolean mIsPowerOn;

    private EnvMonitor() {
        this.mIsPowerOn = true;
        this.mCarSpeed = 0;
        this.mCarConnectionCb = new ServiceConnection() { // from class: com.xiaopeng.xpspeechservice.ms.EnvMonitor.2
            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName name, IBinder service) {
                LogUtils.d(EnvMonitor.TAG, "onServiceConnected() name = %s service = %s", name, service);
                try {
                    EnvMonitor.this.mCarPowerManager = (CarPowerManager) EnvMonitor.this.mCar.getCarManager("power");
                    EnvMonitor.this.mCarPowerManager.setListener(EnvMonitor.this.mCarPowerStateListener);
                    EnvMonitor.this.mCarVcuManager = (CarVcuManager) EnvMonitor.this.mCar.getCarManager("xp_vcu");
                    List<Integer> vcuPropList = Arrays.asList(557847045);
                    EnvMonitor.this.mCarVcuManager.registerPropCallback(vcuPropList, EnvMonitor.this.mCarVcuEventCallback);
                } catch (CarNotConnectedException e) {
                    LogUtils.e(EnvMonitor.TAG, "get car manager fail", (Throwable) e);
                }
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName name) {
                LogUtils.d(EnvMonitor.TAG, "onServiceDisconnected() name = %s", name);
            }
        };
        this.mCarPowerStateListener = new CarPowerManager.CarPowerStateListener() { // from class: com.xiaopeng.xpspeechservice.ms.EnvMonitor.3
            public void onStateChanged(int state) {
                if (state != 6) {
                    if (state == 7) {
                        EnvMonitor.this.onPowerStateChanged(false);
                        return;
                    } else if (state != 9) {
                        return;
                    }
                }
                EnvMonitor.this.onPowerStateChanged(true);
            }
        };
        this.mCarVcuEventCallback = new CarVcuManager.CarVcuEventCallback() { // from class: com.xiaopeng.xpspeechservice.ms.EnvMonitor.4
            public void onChangeEvent(CarPropertyValue carPropertyValue) {
                if (carPropertyValue.getPropertyId() == 557847045) {
                    int gearLevel = ((Integer) carPropertyValue.getValue()).intValue();
                    if (gearLevel == 1 || gearLevel == 2) {
                        EnvMonitor.this.mEventHandler.sendEmptyMessage(EnvMonitor.MSG_UPDATE_CAR_SPEED);
                        return;
                    }
                    EnvMonitor.this.mEventHandler.removeMessages(EnvMonitor.MSG_UPDATE_CAR_SPEED);
                    EnvMonitor.this.mCarSpeed = 0;
                    EnvMonitor.this.mEventBus.postSticky(new CarSpeed(EnvMonitor.this.mCarSpeed));
                }
            }

            public void onErrorEvent(int propertyId, int errorCode) {
            }
        };
        this.mEventThread = new HandlerThread(TAG);
        this.mEventThread.start();
        this.mEventHandler = new EventHandler(this.mEventThread.getLooper());
        this.mEventBus = EventBus.getDefault();
        registerConnectivityManagerCB();
        registerCarApi();
    }

    /* loaded from: classes.dex */
    private static class SingleHolder {
        private static EnvMonitor instance = new EnvMonitor();

        private SingleHolder() {
        }
    }

    public static EnvMonitor getInstance() {
        return SingleHolder.instance;
    }

    private void registerConnectivityManagerCB() {
        ConnectivityManager cm = (ConnectivityManager) SpeechApp.getContext().getSystemService("connectivity");
        NetworkRequest.Builder request = new NetworkRequest.Builder();
        request.addTransportType(0);
        request.addTransportType(1);
        request.addTransportType(3);
        cm.requestNetwork(request.build(), new ConnectivityManager.NetworkCallback() { // from class: com.xiaopeng.xpspeechservice.ms.EnvMonitor.1
            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onAvailable(Network network) {
                LogUtils.i(EnvMonitor.TAG, "network available: " + network.toString());
                EnvMonitor.this.mEventBus.postSticky(new NetworkState(0));
            }

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            }

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
            }

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onLosing(Network network, int maxMsToLive) {
                LogUtils.i(EnvMonitor.TAG, "network losing: " + network.toString() + " maxMstoLive: " + maxMsToLive);
                EnvMonitor.this.mEventBus.postSticky(new NetworkState(1));
            }

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onLost(Network network) {
                LogUtils.i(EnvMonitor.TAG, "network lost: " + network.toString());
                EnvMonitor.this.mEventBus.postSticky(new NetworkState(2));
            }
        });
    }

    private void registerCarApi() {
        this.mCar = Car.createCar(SpeechApp.getContext(), this.mCarConnectionCb, this.mEventHandler);
        this.mCar.connect();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onPowerStateChanged(boolean isOn) {
        if (this.mIsPowerOn != isOn) {
            this.mIsPowerOn = isOn;
            if (this.mIsPowerOn) {
                this.mEventBus.post(new PowerState(1));
            } else {
                this.mEventBus.post(new PowerState(0));
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == EnvMonitor.MSG_UPDATE_CAR_SPEED) {
                try {
                    int speed = Math.round(EnvMonitor.this.mCarVcuManager.getRawCarSpeed());
                    if (speed != EnvMonitor.this.mCarSpeed) {
                        EnvMonitor.this.mCarSpeed = speed;
                        EnvMonitor.this.mEventBus.postSticky(new CarSpeed(EnvMonitor.this.mCarSpeed));
                    }
                } catch (Exception e) {
                    LogUtils.e(EnvMonitor.TAG, "get car speech fail");
                }
                EnvMonitor.this.mEventHandler.sendEmptyMessageDelayed(EnvMonitor.MSG_UPDATE_CAR_SPEED, 300L);
            }
        }
    }

    public void onDestroy() {
        try {
            if (this.mCarPowerManager != null) {
                this.mCarPowerManager.clearListener();
            }
            if (this.mCarVcuManager != null) {
                this.mCarVcuManager.unregisterCallback(this.mCarVcuEventCallback);
            }
            this.mCar.disconnect();
        } catch (Exception e) {
        }
        this.mEventHandler.removeCallbacksAndMessages(null);
        this.mEventThread.quit();
    }
}
