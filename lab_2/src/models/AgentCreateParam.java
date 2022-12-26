package models;

public class AgentCreateParam {
    public String name;
    public String params;
    public Integer count = 1;

    public AgentCreateParam(String name, String params, Integer count) {
        this.name = name;
        this.params = params;
        this.count = count;
    }
}
