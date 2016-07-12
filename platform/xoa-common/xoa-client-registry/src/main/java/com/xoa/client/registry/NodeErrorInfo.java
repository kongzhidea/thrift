package com.xoa.client.registry;

public class NodeErrorInfo {
    private String serviceId;
    private int count;
    private Node node;

    public NodeErrorInfo(String serviceId, Node node, int count) {
        this.serviceId = serviceId;
        this.count = count;
        this.node = node;
    }

    public void addCount(int delta) {
        count += delta;
    }

    public String getServiceId() {
        return serviceId;
    }

    public Node getNode() {
        return node;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}