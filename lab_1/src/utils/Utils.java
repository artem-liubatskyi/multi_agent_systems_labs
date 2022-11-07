package utils;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class Utils {
    public static DFAgentDescription[] getAgentsByType(String type, Agent agent) {
        try {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(type);
            template.addServices(sd);
            return DFService.search(agent, template);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        return new DFAgentDescription[0];
    }
}
