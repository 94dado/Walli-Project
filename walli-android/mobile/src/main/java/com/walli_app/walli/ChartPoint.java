package com.walli_app.walli;

/**
 * Created by dado on 06/07/2016.
 */
public class ChartPoint {
    private String label;
    private float value;

    public ChartPoint(String label, float value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public float getValue() {
        return value;
    }
}
