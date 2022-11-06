public class Main {
    public static void main(String[] args) {
        String[] agentNames = {"BookBuyerAgent", "BookSellerAgent"};
        String[] jadeArgs = {"-gui", "-agents", buildAgentsArgument(agentNames)};
        jade.Boot.main(jadeArgs);
    }

    private static String buildAgentsArgument(String[] agentNames) {
        String[] result = new String[agentNames.length];

        for (int i = 0; i < agentNames.length; i++) {
            var agentName = agentNames[i];
            result[i] = agentName + ":" + agentName;
        }

        return String.join(";", result);
    }
}