package utils;

import models.AgentCreateParam;

public class AgentArgumentsBuilder {
    public static String build(AgentCreateParam[] agents) {
        String[] result = new String[agents.length];

        for (int i = 0; i < agents.length; i++) {
            for (int agentsCount = 0; agentsCount < agents[i].count; agentsCount++) {
                var agentName = agents[i].name;
                result[i] = (agentName  + agents[i].count) + ":" + agentName;
                if (agents[i].params != null) {
                    result[i] += "(" + agents[i].params + ")";
                }
            }
        }

        return String.join(";", result);
    }
}
