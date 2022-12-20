package utils;

import models.AgentCreateParam;

import java.util.Stack;
import java.util.UUID;

public class AgentArgumentsBuilder {
    public static String build(AgentCreateParam[] agents) {
        Stack<String> result = new Stack();

        for (int i = 0; i < agents.length; i++) {
            for (int agentsCount = 0; agentsCount < agents[i].count; agentsCount++) {
                result.push(buildAgentParamChunk(agents[i]));
            }
        }

        return String.join(";", result);
    }

    private static String buildAgentParamChunk(AgentCreateParam agent){
        var agentName = agent.name;
        var param = (agentName + "-" + UUID.randomUUID()) + ":" + agentName;
        if (agent.params != null) {
            param += "(" + agent.params + ")";
        }
        return param;
    }
}
