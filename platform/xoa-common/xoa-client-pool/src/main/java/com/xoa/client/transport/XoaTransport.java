/**
 * @ XoaTransport.java Create on 2011-12-22 下午4:18:17
 */
package com.xoa.client.transport;

import com.xoa.client.registry.Node;
import org.apache.thrift.transport.TTransport;


public class XoaTransport {

    private TTransport transport;
    private Node node;
    private boolean disabled = false;

    public TTransport getTransport() {
        return transport;
    }

    public void setTransport(TTransport transport) {
        this.transport = transport;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
  
