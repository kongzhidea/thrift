package com.xoa.client.registry;

import java.util.List;


public class Service {

    private String serviceId;

    private List<Node> nodes;

    private List<String> alarmEmails;// 报警邮件

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<String> getAlarmEmails() {
        return alarmEmails;
    }

    public void setAlarmEmails(List<String> alarmEmails) {
        this.alarmEmails = alarmEmails;
    }
}
