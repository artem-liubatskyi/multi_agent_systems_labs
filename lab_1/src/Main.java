import models.AgentCreateParam;
import utils.AgentArgumentsBuilder;

public class Main {
    public static void main(String[] args) {
        AgentCreateParam[] agentNames = {
                new AgentCreateParam("BookBuyerAgent", "The-Lord-of-the-rings",1),
                new AgentCreateParam("BookSellerAgent", "The-Lord-of-the-rings, Harry-Potter",2)
        };

        String[] jadeArgs = {"-gui", "-agents", AgentArgumentsBuilder.build(agentNames)};
        jade.Boot.main(jadeArgs);
    }


}