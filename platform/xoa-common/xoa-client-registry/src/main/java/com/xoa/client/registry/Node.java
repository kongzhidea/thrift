package com.xoa.client.registry;


import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private String host;

    private int port;

    private boolean disabled;

    public Node() {
    }


    public Node(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getIdentity() {
        return host + ":" + port;
    }

    public static Node getNodeFromIdentity(String identity) {
        try {
            String[] conts = StringUtils.split(identity, ":");
            String host = conts[0];
            String port = conts[1];
            Node node = new Node(host, Integer.valueOf(port));
            return node;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Node> getNodeFromIdentity(List<String> identities) {
        List<Node> nodes = new ArrayList<Node>();
        for (String identify : identities) {
            Node node = getNodeFromIdentity(identify);
            if (node != null) {
                nodes.add(node);
            }
        }
        return nodes;
    }

    public static boolean inNodes(List<String> nodeList, String identity) {
        if (identity == null || "".equals(identity)) {
            return false;
        }
        for (String n : nodeList) {
            if (n.equals(identity)) {
                return true;
            }
        }
        return false;
    }
}
