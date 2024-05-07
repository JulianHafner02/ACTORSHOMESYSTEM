package at.fhv.sysarch.lab2.homeautomation.misc;

public class Temperature {

    private double value;

    private String unit;

    public Temperature(double value, String unit) {
        this.value = value;
        this.unit = unit;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }


}
