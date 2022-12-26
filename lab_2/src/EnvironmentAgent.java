import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import models.Labyrinth;
import utils.Constants;

public class EnvironmentAgent extends Agent {
    private Labyrinth Labyrinth = new Labyrinth();
    
    protected void setup() {
        this.register();
        addBehaviour(new EnvironmentStateRequestHandler());
        addBehaviour(new SpeleologistActionHandler());
    }

    private void register() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(Constants.ENVIRONMENT_AGENT_TYPE);
        sd.setName(Constants.ENVIRONMENT_AGENT_TYPE);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private class SpeleologistActionHandler extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt =MessageTemplate.and(MessageTemplate.MatchConversationId(Constants.ENVIRONMENT_ACTION_CONVERSATION_ID),
                    MessageTemplate.MatchPerformative(ACLMessage.CFP));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                ACLMessage message = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                message.setConversationId(Constants.ENVIRONMENT_ACTION_CONVERSATION_ID);
                message.setContent("OK");
                message.setReplyWith("accept" + System.currentTimeMillis());
                myAgent.send(message);
            } else{
                block();
            }
        }
    }

    private class EnvironmentStateRequestHandler extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId(Constants.ENVIRONMENT_STATE_REQUEST_CONVERSATION_ID),
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                sendEnvironmentStateToNavigator();
            } else {
                block();
            }
        }

        private void sendEnvironmentStateToNavigator() {
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.setContent(String.valueOf(Labyrinth.GetHeroRoomState()));
            message.setConversationId(Constants.ENVIRONMENT_STATE_REQUEST_CONVERSATION_ID);
            message.setReplyWith("inform" + System.currentTimeMillis());
            myAgent.send(message);
        }
    }
}
