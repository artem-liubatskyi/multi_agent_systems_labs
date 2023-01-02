import java.util.Arrays;
import java.util.stream.Collectors;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import models.Labyrinth;
import models.SpeleologistActions;
import utils.Constants;

public class EnvironmentAgent extends Agent {
    private Labyrinth Labyrinth = new Labyrinth();
    private AID Speleologist;

    protected void setup() {
        this.register();
        addBehaviour(new SpeleologistFinder());
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

    private class SpeleologistFinder extends Behaviour {
        public void action() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(Constants.SPELEOLOGIST_AGENT_TYPE);
            template.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                if (result != null && result.length > 0) {
                    Speleologist = result[0].getName();
                } else {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        }

        public boolean done() {
            return Speleologist != null;
        }
    }

    private class SpeleologistActionHandler extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchConversationId(Constants.ENVIRONMENT_ACTION_CONVERSATION_ID),
                    MessageTemplate.MatchPerformative(ACLMessage.CFP));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                var action = SpeleologistActions.valueOf(msg.getContent());
                Labyrinth.ResolveHeroAction(action);

                ACLMessage message = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                message.addReceiver(Speleologist);
                message.setConversationId(Constants.ENVIRONMENT_ACTION_CONVERSATION_ID);
                message.setContent("OK");
                message.setReplyWith("accept" + System.currentTimeMillis());
                myAgent.send(message);
            } else {
                block();
            }
        }
    }

    private class EnvironmentStateRequestHandler extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchConversationId(Constants.ENVIRONMENT_STATE_REQUEST_CONVERSATION_ID),
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                sendEnvironmentStateToSpeleologist();
            } else {
                block();
            }
        }

        private void sendEnvironmentStateToSpeleologist() {
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.addReceiver(Speleologist);
            var roomState =Labyrinth.GetHeroRoomState();
            String state = Arrays.stream(Labyrinth.GetHeroRoomState().toArray())
                    .map(String::valueOf)
                    .collect(Collectors.joining("."));
            message.setContent(state);
            message.setConversationId(Constants.ENVIRONMENT_STATE_REQUEST_CONVERSATION_ID);
            message.setReplyWith("INFORM" + System.currentTimeMillis());
            myAgent.send(message);
        }
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println(getAID().getName() + " terminating.");
    }
}
