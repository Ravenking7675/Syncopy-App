package com.avinash.syncopyproject.Model;

public class PcUser {

    String connectedTo;
    String pcName;
    String pcType;
    String uuid;

    public PcUser() {
    }

    public PcUser(String connectedTo, String pcName, String pcType, String uuid) {
        this.connectedTo = connectedTo;
        this.pcName = pcName;
        this.pcType = pcType;
        this.uuid = uuid;
    }

    public String getConnectedTo() {
        return connectedTo;
    }

    public void setConnectedTo(String connectedTo) {
        this.connectedTo = connectedTo;
    }

    public String getPcName() {
        return pcName;
    }

    public void setPcName(String pcName) {
        this.pcName = pcName;
    }

    public String getPcType() {
        return pcType;
    }

    public void setPcType(String pcType) {
        this.pcType = pcType;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
