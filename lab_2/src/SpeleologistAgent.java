import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import models.LabyrinthCellDescriptor;
import models.SpeleologistActions;
import models.SpeleologistCycleSteps;
import utils.Constants;

public class SpeleologistAgent extends Agent {
    private AID Environment;
    private AID Navigator;

    protected void setup() {
        this.register();
        addBehaviour(new EnvironmentFinder());
        addBehaviour(new NavigatorFinder());
    }

    private void register() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(Constants.SPELEOLOGIST_AGENT_TYPE);
        sd.setName(Constants.SPELEOLOGIST_AGENT_TYPE);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private class EnvironmentFinder extends Behaviour {
        public void action() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(Constants.ENVIRONMENT_AGENT_TYPE);
            template.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                if (result != null && result.length > 0) {
                    Environment = result[0].getName();
                    myAgent.addBehaviour(new RequestPerformer());
                    System.out.println("Environment agent registered");
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
            return Environment != null;
        }
    }

    private class NavigatorFinder extends Behaviour {
        public void action() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(Constants.NAVIGATOR_AGENT_TYPE);
            template.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                if (result != null && result.length > 0) {
                    Navigator = result[0].getName();
                    System.out.println("Navigator agent registered");
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
            return Navigator != null;
        }
    }

    public class RequestPerformer extends Behaviour {
        private MessageTemplate environmentStateReplyTemplate;
        private MessageTemplate navigatorReplyTemplate;
        private MessageTemplate actionReplyTemplate;
        private LabyrinthCellDescriptor environmentState;
        private SpeleologistActions action;
        private SpeleologistCycleSteps step = SpeleologistCycleSteps.RequestEnvironmentState;
        public void action() {

            switch (step) {
                case RequestEnvironmentState:
                    System.out.println("Requesting environment state");
                    environmentStateReplyTemplate = requestEnvironmentState();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    step = SpeleologistCycleSteps.ReceiveEnvironmentStateResponse;
                    break;
                case ReceiveEnvironmentStateResponse:
                    ACLMessage environmentReply = myAgent.receive(environmentStateReplyTemplate);
                    if (environmentReply != null) {
                        if (environmentReply.getPerformative() == ACLMessage.INFORM) {
                            environmentState = LabyrinthCellDescriptor.valueOf(environmentReply.getContent());
                            System.out.println(String.format("Environment state received: %state",environmentState));
                        }
                        step = SpeleologistCycleSteps.NotifyNavigatorAboutEnvironmentState;
                    } else {
                        block();
                    }
                    break;
                case NotifyNavigatorAboutEnvironmentState:
                    navigatorReplyTemplate = sendEnvironmentStateToNavigator(environmentState);
                    System.out.println("Sending environment state to navigator");
                    step = SpeleologistCycleSteps.ReceiveActionFromNavigator;
                    break;
                case ReceiveActionFromNavigator:
                    ACLMessage navigatorReply = myAgent.receive(navigatorReplyTemplate);
                    if (navigatorReply != null) {
                        if (navigatorReply.getPerformative() == ACLMessage.PROPOSE) {
                            action = SpeleologistActions.valueOf(navigatorReply.getContent());
                            System.out.println(String.format("Received next action from navigator: %state", action));
                        }
                        step = SpeleologistCycleSteps.SendActionToEnvironment;
                    } else {
                        block();
                    }
                    break;
                case SendActionToEnvironment:
                    actionReplyTemplate = sendAction(action);
                    System.out.println("Sending action to Environment");
                    step = SpeleologistCycleSteps.ReceiveActionAcceptance;
                    break;
                case ReceiveActionAcceptance:
                    ACLMessage actionAcceptanceReply = myAgent.receive(actionReplyTemplate);
                    if (actionAcceptanceReply != null) {
                        if (actionAcceptanceReply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                            System.out.println("Received action acceptance confirmation from environment");
                        }
                        step = SpeleologistCycleSteps.RequestEnvironmentState;
                    } else {
                        block();
                    }
                    break;
            }
        }

        private  MessageTemplate requestEnvironmentState() {
            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            message.addReceiver(Environment);
            message.setConversationId(Constants.ENVIRONMENT_STATE_REQUEST_CONVERSATION_ID);
            message.setReplyWith("request" + System.currentTimeMillis());
            myAgent.send(message);

            return  MessageTemplate.and(MessageTemplate.MatchConversationId(Constants.ENVIRONMENT_STATE_REQUEST_CONVERSATION_ID),
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        }

        private MessageTemplate sendEnvironmentStateToNavigator(LabyrinthCellDescriptor state) {
            ACLMessage message = new ACLMessage(ACLMessage.CFP);
            message.addReceiver(Navigator);
            message.setContent(String.valueOf(state));
            message.setConversationId(Constants.ENVIRONMENT_STATE_NOTIFICATION_CONVERSATION_ID);
            message.setReplyWith("cfp" + System.currentTimeMillis());
            myAgent.send(message);

            return  MessageTemplate.and(MessageTemplate.MatchConversationId(Constants.ENVIRONMENT_STATE_NOTIFICATION_CONVERSATION_ID),
                    MessageTemplate.MatchInReplyTo(message.getReplyWith()));
        }

        private MessageTemplate sendAction(SpeleologistActions action) {
            ACLMessage message = new ACLMessage(ACLMessage.CFP);
            message.addReceiver(Environment);
            message.setContent(String.valueOf(action));
            message.setConversationId(Constants.ENVIRONMENT_ACTION_CONVERSATION_ID);
            message.setReplyWith("cfp" + System.currentTimeMillis());
            myAgent.send(message);

            return  MessageTemplate.and(MessageTemplate.MatchConversationId(Constants.ENVIRONMENT_ACTION_CONVERSATION_ID),
                    MessageTemplate.MatchInReplyTo(message.getReplyWith()));
        }

        public boolean done() {
            return environmentState == LabyrinthCellDescriptor.Wumpus || environmentState == LabyrinthCellDescriptor.Glitch;
        }
    }

}
