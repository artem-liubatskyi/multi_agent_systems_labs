import behaviours.buyer.RequestPerformer;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import utils.Utils;

public class BookBuyerAgent extends Agent {

    private String targetBookTitle;
    private AID[] sellerAgents;

    protected void setup() {
        System.out.println("Hallo! Buyer-agent " + getAID().getName() + " is ready.");

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            targetBookTitle = (String) args[0];
            System.out.println("Target book is " + targetBookTitle);

            addBehaviour(new TickerBehaviour(this, 10000) {
                protected void onTick() {
                    System.out.println("Trying to buy " + targetBookTitle);

                    var result = Utils.getAgentsByType("book-selling", myAgent);
                    System.out.println("Found the following seller agents:");
                    sellerAgents = new AID[result.length];
                    for (int i = 0; i < result.length; ++i) {
                        sellerAgents[i] = result[i].getName();
                        System.out.println(sellerAgents[i].getName());
                    }

                    myAgent.addBehaviour(new BookBuyerRequestPerformer());
                }
            });
        } else {
            System.out.println("No target book title specified");
            doDelete();
        }
    }

    private class BookBuyerRequestPerformer extends RequestPerformer {

        protected AID[] getSellerAgents() {
            return sellerAgents;
        }

        protected String getTargetBookTitle() {
            return targetBookTitle;
        }
    }
}