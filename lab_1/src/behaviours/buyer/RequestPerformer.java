package behaviours.buyer;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.Constants;

enum BookPurchaseRequestSteps {
    SendBookOfferRequest, // Send the cfp to all sellers
    ReceiveBookOfferResponse,// Receive all proposals/refusals from seller agents
    SendPurchaseRequest,// Send the purchase order to the seller that provided the best offer
    ReceivePurchaseResponse,// Receive the purchase order reply
    Terminate// Purchase successful. We can terminate
}

public abstract class RequestPerformer extends Behaviour {
    private AID bestSeller; // The agent who provides the best offer
    private int bestPrice;  // The best offered price
    private int repliesCnt = 0; // The counter of replies from seller agents
    private MessageTemplate mt; // The template to receive replies
    private BookPurchaseRequestSteps step = BookPurchaseRequestSteps.SendBookOfferRequest;
    protected abstract AID[] getSellerAgents();
    protected abstract String getTargetBookTitle();

    public void action() {
        switch (step) {
            case SendBookOfferRequest:
                // Send the cfp to all sellers
                ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                for (int i = 0; i < getSellerAgents().length; ++i) {
                    cfp.addReceiver(getSellerAgents()[i]);
                }
                cfp.setContent(getTargetBookTitle());
                cfp.setConversationId(Constants.BOOK_TRADE_CONVERSATION_ID);
                cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
                myAgent.send(cfp);
                // Prepare the template to get proposals
                mt = MessageTemplate.and(MessageTemplate.MatchConversationId(Constants.BOOK_TRADE_CONVERSATION_ID),
                        MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                step = BookPurchaseRequestSteps.ReceiveBookOfferResponse;
                break;
            case ReceiveBookOfferResponse:
                // Receive all proposals/refusals from seller agents
                ACLMessage reply = myAgent.receive(mt);
                if (reply != null) {
                    // Reply received
                    if (reply.getPerformative() == ACLMessage.PROPOSE) {
                        // This is an offer
                        int price = Integer.parseInt(reply.getContent());
                        if (bestSeller == null || price < bestPrice) {
                            // This is the best offer at present
                            bestPrice = price;
                            bestSeller = reply.getSender();
                        }
                    }
                    repliesCnt++;
                    if (repliesCnt >= getSellerAgents().length) {
                        // We received all replies
                        step = BookPurchaseRequestSteps.SendPurchaseRequest;
                    }
                } else {
                    block();
                }
                break;
            case SendPurchaseRequest:
                // Send the purchase order to the seller that provided the best offer
                ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                order.addReceiver(bestSeller);
                order.setContent(getTargetBookTitle());
                order.setConversationId(Constants.BOOK_TRADE_CONVERSATION_ID);
                order.setReplyWith("order" + System.currentTimeMillis());
                myAgent.send(order);
                // Prepare the template to get the purchase order reply
                mt = MessageTemplate.and(MessageTemplate.MatchConversationId(Constants.BOOK_TRADE_CONVERSATION_ID),
                        MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                step = BookPurchaseRequestSteps.ReceivePurchaseResponse;
                break;
            case ReceivePurchaseResponse:
                // Receive the purchase order reply
                reply = myAgent.receive(mt);
                if (reply != null) {
                    // Purchase order reply received
                    if (reply.getPerformative() == ACLMessage.INFORM) {
                        // Purchase successful. We can terminate
                        System.out.println(getTargetBookTitle() + " successfully purchased from agent " + reply.getSender().getName());
                        System.out.println("Price = " + bestPrice);
                        myAgent.doDelete();
                    } else {
                        System.out.println("Attempt failed: requested book already sold.");
                    }

                    step = BookPurchaseRequestSteps.Terminate;
                } else {
                    block();
                }
                break;
        }
    }

    public boolean done() {
        if (step == BookPurchaseRequestSteps.SendPurchaseRequest && bestSeller == null) {
            System.out.println("Attempt failed: " + getTargetBookTitle() + " not available for sale");
        }
        return ((step == BookPurchaseRequestSteps.SendPurchaseRequest && bestSeller == null) || step == BookPurchaseRequestSteps.Terminate);
    }
}
