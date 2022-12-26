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
import models.SpeleologistActions;
import utils.Constants;

public class NavigatorAgent extends Agent {
    private AID Speleologist;

    protected void setup() {
        this.register();
        addBehaviour(new RequestsServer());
        addBehaviour(new SpeleologistFinder());
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

    private class RequestsServer extends CyclicBehaviour {
        private int index = 0;
        private SpeleologistActions[] actions = new SpeleologistActions[] {
                SpeleologistActions.TurnRight,
                SpeleologistActions.Move,
                SpeleologistActions.TurnLeft,
                SpeleologistActions.Move,
                SpeleologistActions.Move,
                SpeleologistActions.GrabGold
        };

        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchConversationId(Constants.ENVIRONMENT_STATE_NOTIFICATION_CONVERSATION_ID),
                    MessageTemplate.MatchPerformative(ACLMessage.CFP));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                ACLMessage message = new ACLMessage(ACLMessage.PROPOSE);
                message.addReceiver(Speleologist);
                message.setConversationId(Constants.ENVIRONMENT_STATE_NOTIFICATION_CONVERSATION_ID);
                message.setContent(String.valueOf(defineAction(null)));
                message.setReplyWith("PROPOSE" + System.currentTimeMillis());
                index++;
                myAgent.send(message);
            } else {
                block();
            }
        }

        private SpeleologistActions defineAction(String state) {
            return actions[index];
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
