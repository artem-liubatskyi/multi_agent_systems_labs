public class Main {
    public static void main(String[] args) {
        AgentParam[] agentNames = {new AgentParam("BookBuyerAgent", "The-Lord-of-the-rings"), new AgentParam("BookSellerAgent", "The-Lord-of-the-rings, Harry-Potter")};

        String[] jadeArgs = {"-gui", "-agents", buildAgentsArgument(agentNames)};
        jade.Boot.main(jadeArgs);
    }

    private static String buildAgentsArgument(AgentParam[] agents) {
        String[] result = new String[agents.length];

        for (int i = 0; i < agents.length; i++) {
            var agentName = agents[i].name;
            result[i] = agentName + ":" + agentName;
            if (agents[i].params != null) {
                result[i] += "(" + agents[i].params + ")";
            }
        }

        return String.join(";", result);
    }
}

class AgentParam {
    String name;
    String params;
    Integer count = 1;

    AgentParam(String name, String params) {
        this.name = name;
        this.params = params;
    }
}