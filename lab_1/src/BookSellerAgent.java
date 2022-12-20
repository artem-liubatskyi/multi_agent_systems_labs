import behaviours.seller.OfferRequestsServer;
import behaviours.seller.PurchaseOrdersServer;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.Hashtable;

public class BookSellerAgent extends Agent {
    private final Hashtable catalogue = new Hashtable();

    protected void setup() {
        var args = ((String) getArguments()[0]).split(",");

        for (String bookName : args) {
            catalogue.put(bookName.trim(), 10);
        }

        register();

        // Add the behaviour serving queries from buyer agents
        addBehaviour(new BookSellerOfferRequestsServer());

        // Add the behaviour serving purchase orders from buyer agents
        addBehaviour(new BookSellerPurchaseOrdersServer());
    }

    // Register the book-selling service in the yellow pages
    private void register() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("book-selling");
        sd.setName(getAID().getName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Printout a dismissal message
        System.out.println("Seller-agent " + getAID().getName() + " terminating.");
    }

    private Integer getBookPrice(String title) {
        return (Integer) catalogue.get(title);
    }

    private class BookSellerOfferRequestsServer extends OfferRequestsServer {
        public Integer getBookPriceByTitle(String title) {
            return getBookPrice(title);
        }
    }

    private class BookSellerPurchaseOrdersServer extends PurchaseOrdersServer {
        public Integer getBookPriceByTitle(String title) {
            return getBookPrice(title);
        }
    }


}

