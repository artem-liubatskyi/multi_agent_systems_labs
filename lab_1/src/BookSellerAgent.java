import jade.core.Agent;

import java.util.Hashtable;

public class BookSellerAgent extends Agent {
    private Hashtable catalogue;

    protected void setup() {
        catalogue = new Hashtable();
        System.out.println("Hello! Seller-agent " + getAID().getName() + " is ready.");
    }

    protected void takeDown() {
        System.out.println("Seller - agent " + getAID().getName() + "terminating.");
    }
}

