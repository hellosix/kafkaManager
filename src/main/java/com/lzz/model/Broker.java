package com.lzz.model;

/**
 * Created by gl49 on 2018/1/19.
 */
public class Broker {
    private String host;
    private String port;
    private String jmx_port;

    public Broker(){

    }

    public Broker(String host, String port, String jmx_port){
        this.host = host;
        this.port = port;
        this.jmx_port = jmx_port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getJmx_port() {
        return jmx_port;
    }

    public void setJmx_port(String jmx_port) {
        this.jmx_port = jmx_port;
    }
}
