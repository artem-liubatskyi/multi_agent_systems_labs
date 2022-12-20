package behaviours.seller;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public abstract class PurchaseOrdersServer extends CyclicBehaviour {
    public abstract Integer getBookPriceByTitle(String title);

    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
        ACLMessage msg = myAgent.receive(mt);
        if (msg != null) {
            // ACCEPT_PROPOSAL Message received. Process it
            String title = msg.getContent();
            ACLMessage reply = msg.createReply();

            Integer price = getBookPriceByTitle(title);
            if (price != null) {
                reply.setPerformative(ACLMessage.INFORM);
                System.out.println(title+" sold to agent "+msg.getSender().getName());
            }
            else {
                // The requested book has been sold to another buyer in the meanwhile .
                reply.setPerformative(ACLMessage.FAILURE);
                reply.setContent("not-available");
            }
            myAgent.send(reply);
        }
        else {
            block();
        }
    }
}
