package ds.young.turbo.common;

import java.util.List;

public class Configuration {
    private String procotol;
    private String name;
    private int port;
    private String role;
    private String serialize;
    private String address;
    private String stragety;
    private List<RegisterInfoProvider> providerList;
    private List<RegisterInfoConsumer> consumerList;


    private static Configuration configuration = new Configuration();

    private Configuration(){};

    public static Configuration getInstance(){
        return configuration;
    }

    public String getProcotol() {
        return procotol;
    }

    public void setProcotol(String procotol) {
        this.procotol = procotol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getSerialize() {
        return serialize;
    }

    public void setSerialize(String serialize) {
        this.serialize = serialize;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStragety() {
        return stragety;
    }

    public void setStragety(String stragety) {
        this.stragety = stragety;
    }

    public List<RegisterInfoProvider> getProviderList() {
        return providerList;
    }

    public void setProviderList(List<RegisterInfoProvider> providerList) {
        this.providerList = providerList;
    }

    public List<RegisterInfoConsumer> getConsumerList() {
        return consumerList;
    }

    public void setConsumerList(List<RegisterInfoConsumer> consumerList) {
        this.consumerList = consumerList;
    }
}
