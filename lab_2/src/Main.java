import models.AgentCreateParam;
import utils.AgentArgumentsBuilder;

public class Main {
    public static void main(String[] args) {
        AgentCreateParam[] agentNames = {
                new AgentCreateParam("EnvironmentAgent", null, 1),
                new AgentCreateParam("NavigatorAgent", null, 1),
                new AgentCreateParam("SpeleologistAgent", null, 1),
        };
        String[] jadeArgs = {"-gui", "-agents", AgentArgumentsBuilder.build(agentNames)};
        jade.Boot.main(jadeArgs);
    }


}