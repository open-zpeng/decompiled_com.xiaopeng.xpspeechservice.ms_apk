package com.xiaopeng.lib.framework.moduleinterface.aiassistantmodule.sensor;

import java.lang.reflect.Field;
/* loaded from: classes.dex */
public abstract class BaseSensor {
    private ISensorCallback mSensorCallback;
    private ISensorListener mSensorListener;

    public abstract void initField();

    public abstract String sensorName();

    public abstract void startSensing();

    public abstract void stopSensing();

    protected BaseSensor() {
        initField();
    }

    public void setSensorListener(ISensorListener sensorListener) {
        this.mSensorListener = sensorListener;
    }

    public void setSensorCallback(ISensorCallback sensorCallback) {
        this.mSensorCallback = sensorCallback;
    }

    public ISensorCallback getSensorCallback() {
        return this.mSensorCallback;
    }

    public ISensorListener getSensorListener() {
        return this.mSensorListener;
    }

    public void doExtra(String extra) {
    }

    public void refreshField(String field) {
    }

    protected void sensing(String field, String value) {
        if (getSensorListener() != null) {
            getSensorListener().onSensorChange(sensorName(), field, value);
        }
    }

    protected void sensing(String field, Object value) {
        sensing(field, String.valueOf(value));
    }

    public String onRequestSensorValue(String fieldName) {
        String value = null;
        try {
            Field field = getFieldByClasss(fieldName, this);
            refreshField(fieldName);
            field.setAccessible(true);
            value = String.valueOf(field.get(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (getSensorCallback() != null) {
            getSensorCallback().onSensorReponse(sensorName(), fieldName, value);
        }
        return value;
    }

    private Field getFieldByClasss(String fieldName, Object object) {
        Field field = null;
        for (Class<?> clazz = object.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                field = clazz.getDeclaredField(fieldName);
            } catch (Exception e) {
            }
        }
        return field;
    }
}
