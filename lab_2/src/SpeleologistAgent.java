import java.util.*;
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

    private Map<LabyrinthCellDescriptor, List<String>> StatePhrasesMap = new HashMap<>();

    protected void setup() {
        this.register();
        StatePhrasesMap.put(LabyrinthCellDescriptor.Breeze, new ArrayList<>() {
            {
                add("I feel breeze here");
                add("There is a breeze");
                add("It’s a cool breeze here");
            }
        });
        StatePhrasesMap.put(LabyrinthCellDescriptor.Stench, new ArrayList<>() {
            {
                add("I feel stench here");
                add("There is a stench");
                add("It’s a real stench here");
            }
        });
        StatePhrasesMap.put(LabyrinthCellDescriptor.Glitch, new ArrayList<>() {
            {
                add("I see glitch here");
                add("There is a glitch");
            }
        });

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
        private String environmentState;
        private SpeleologistActions action;
        private SpeleologistCycleSteps step = SpeleologistCycleSteps.RequestEnvironmentState;

        public void action() {
            switch (step) {
                case RequestEnvironmentState:
                    System.out.println("Requesting environment state");
                    environmentStateReplyTemplate = requestEnvironmentState();
                    step = SpeleologistCycleSteps.ReceiveEnvironmentStateResponse;
                    break;
                case ReceiveEnvironmentStateResponse:
                    ACLMessage environmentReply = myAgent.receive(environmentStateReplyTemplate);
                    if (environmentReply != null) {
                        if (environmentReply.getPerformative() == ACLMessage.INFORM) {
                            environmentState = environmentReply.getContent();
                            System.out.println(String.format("Environment state received: %s", environmentState));
                            step = SpeleologistCycleSteps.NotifyNavigatorAboutEnvironmentState;
                        }
                    } else {
                        block();
                    }
                    break;
                case NotifyNavigatorAboutEnvironmentState:
                    navigatorReplyTemplate = sendEnvironmentStateToNavigator(environmentState);
                    step = SpeleologistCycleSteps.ReceiveActionFromNavigator;
                    break;
                case ReceiveActionFromNavigator:
                    ACLMessage navigatorReply = myAgent.receive(navigatorReplyTemplate);
                    if (navigatorReply != null) {
                        if (navigatorReply.getPerformative() == ACLMessage.PROPOSE) {
                            action = SpeleologistActions.valueOf(navigatorReply.getContent());
                            System.out.println(String.format("Received next action from navigator: %s", action));
                            step = SpeleologistCycleSteps.SendActionToEnvironment;
                        }
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
                            System.out.println("Received action acceptance confirmation from environment \n\n");
                        }
                        step = SpeleologistCycleSteps.RequestEnvironmentState;
                    } else {
                        block();
                    }
                    break;
            }
        }

        private MessageTemplate requestEnvironmentState() {
            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            message.addReceiver(Environment);
            message.setConversationId(Constants.ENVIRONMENT_STATE_REQUEST_CONVERSATION_ID);
            message.setReplyWith("request" + System.currentTimeMillis());
            myAgent.send(message);

            return MessageTemplate.and(
                    MessageTemplate.MatchConversationId(Constants.ENVIRONMENT_STATE_REQUEST_CONVERSATION_ID),
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        }

        private MessageTemplate sendEnvironmentStateToNavigator(String state) {
            var parsedStateChunks = ParseEnvironmentState(state);
            var statePhrase = String.join(".",Arrays.stream(parsedStateChunks).map(x-> StatePhrasesMap.get(x).get(0)).toArray(String[]::new));
            System.out.println( String.format("Sending environment state to navigator %s", statePhrase));
            ACLMessage message = new ACLMessage(ACLMessage.CFP);
            message.addReceiver(Navigator);
            message.setContent(statePhrase);
            message.setConversationId(Constants.ENVIRONMENT_STATE_NOTIFICATION_CONVERSATION_ID);
            message.setReplyWith("cfp" + System.currentTimeMillis());
            myAgent.send(message);

            return MessageTemplate.and(
                    MessageTemplate.MatchConversationId(Constants.ENVIRONMENT_STATE_NOTIFICATION_CONVERSATION_ID),
                    MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
        }

        private MessageTemplate sendAction(SpeleologistActions action) {
            ACLMessage message = new ACLMessage(ACLMessage.CFP);
            message.addReceiver(Environment);
            message.setContent(String.valueOf(action));
            message.setConversationId(Constants.ENVIRONMENT_ACTION_CONVERSATION_ID);
            message.setReplyWith("cfp" + System.currentTimeMillis());
            myAgent.send(message);

            return MessageTemplate.and(
                    MessageTemplate.MatchConversationId(Constants.ENVIRONMENT_ACTION_CONVERSATION_ID),
                    MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL));
        }

        private LabyrinthCellDescriptor[] ParseEnvironmentState(String state) {
            return state.length()> 0 ? Arrays.stream(state.split("[.]")).map(LabyrinthCellDescriptor::valueOf).toArray(LabyrinthCellDescriptor[]::new) : new LabyrinthCellDescriptor[0];
        }

        public boolean done() {
            return action == SpeleologistActions.GrabGold;
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
