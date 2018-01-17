package sample.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceConfiguration {
    private String driver;
    private String modalias;

    public String getModalias() {
        return modalias;
    }

    public void setModalias(String modalias) {
        this.modalias = modalias;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }
}
