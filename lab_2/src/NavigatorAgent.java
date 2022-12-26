import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import models.SpeleologistActions;
import utils.Constants;

public class NavigatorAgent extends Agent {

    protected void setup() {
        this.register();
        addBehaviour(new RequestsServer());
    }

    private void register() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(Constants.NAVIGATOR_AGENT_TYPE);
        sd.setName(Constants.NAVIGATOR_AGENT_TYPE);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    protected void takeDown() {

    }

    private class RequestsServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt =MessageTemplate.and(MessageTemplate.MatchConversationId(Constants.ENVIRONMENT_STATE_NOTIFICATION_CONVERSATION_ID),
                    MessageTemplate.MatchPerformative(ACLMessage.CFP));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                ACLMessage message = new ACLMessage(ACLMessage.CFP);
                message.setConversationId(Constants.ENVIRONMENT_STATE_NOTIFICATION_CONVERSATION_ID);
                message.setContent(String.valueOf(SpeleologistActions.GrabGold));
                message.setReplyWith("cfp" + System.currentTimeMillis());
                myAgent.send(message);
            } else {
                block();
            }
        }
    }
}
