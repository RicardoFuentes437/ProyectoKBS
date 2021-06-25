package test.src.Project1;

import jade.core.Agent;
import net.sf.clipsrules.jni.*;
import jade.core.behaviours.*;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.UnreadableException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.*;

public class SellerAgent extends Agent {
    private String targetTable;
    private AID[] sellerAgents;
    private String store;
    Environment clips;

    protected void setup() {
        clips = new Environment();
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            store = (String) args[0];
        }
        // Register the product-selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("product-selling");
        sd.setName("JADE-"+store+"-selling");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        inicializar();
        addBehaviour(new OfferRequestsServer());
        addBehaviour(new PurchaseOrdersServer());
    }


    public void conectarDB(String s){

        try{
            DriverManager.registerDriver (new oracle.jdbc.OracleDriver());
            Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1522:orcl", "ricardof", "12345");
            Statement stmt=conn.createStatement();
            ResultSet rs=stmt.executeQuery(s);
            if(s.startsWith("select *")){
                while(rs.next()) {
                    clips.assertString("(product (part-number " +rs.getInt(1)+ ") (name \"" +rs.getString(2)+ "\") (category " +rs.getString(3)+") (price " +rs.getInt(4)+ "))");
                }
            }
            conn.close();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void inicializar(){
        clips.clear();
        clips.load("C:\\Users\\68765\\clips\\clips_jni_051\\test\\src\\Project1\\Rules\\templates.clp");
        clips.load("C:\\Users\\68765\\clips\\clips_jni_051\\test\\src\\Project1\\Rules\\rules.clp");
        clips.eval("(reset)");
        conectarDB("select * from "+store);
    }

    public int getData(String s, String data) {
        int p = 0;
        try {
            DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
            Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1522:orcl", "ricardof", "12345");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(s);
            Object[] obj = new Object[5];
            while (rs.next()) {
                obj[0] = rs.getInt(1);
                obj[1] = rs.getString(2);
                obj[2] = rs.getString(3);
                obj[3] = rs.getInt(4);
                obj[4] = rs.getInt(5);
            }
            conn.close();
            if (data == "price") {
                p = (int) obj[3];
            }else if(data == "part"){
                p = (int) obj[0];
            }else if(data == "stock"){
                p= (int) obj[4];
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return p;
    }


    protected void takeDown() {

        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Seller-agent "+getAID().getName()+" terminating.");
    }

    public void Supplier(final String table, final String pName){
        Object[] args = {table, pName};
        if (args != null && args.length > 0) {
            targetTable = (String) args[0];
            String productName = (String) args[1];
            addBehaviour(new OneShotBehaviour() {
                public void action() {
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("product-supply-"+productName);
                    template.addServices(sd);
                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, template);
                        System.out.println("Found the following supplier agents:");
                        sellerAgents = new AID[result.length];
                        for (int i = 0; i < result.length; ++i) {
                            sellerAgents[i] = result[i].getName();
                            System.out.println(sellerAgents[i].getName());
                        }
                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    }

                    myAgent.addBehaviour(new RequestStock());
                }
            });
        }
    }

    private class RequestStock extends Behaviour {
        private AID supplier;
        private int repliesCnt = 0;
        private MessageTemplate mt;
        private int step = 0;

        public void action() {
            switch (step) {
                case 0:

                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < sellerAgents.length; ++i) {
                        cfp.addReceiver(sellerAgents[i]);
                    }
                    cfp.setContent(targetTable);
                    cfp.setConversationId("product-supply");
                    cfp.setReplyWith("cfp"+System.currentTimeMillis());
                    myAgent.send(cfp);

                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("product-supply"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:

                    ACLMessage reply = myAgent.receive(mt);
                    String product;
                    if (reply != null) {

                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            product = reply.getContent();
                            System.out.println(product+" successfully re-stocked "+reply.getSender().getName());
                        }
                        step = 2;
                    }
                    else {
                        block();
                    }
                    break;
            }
        }

        public boolean done() {
            return (step == 2);
        }
    }

    /**
     Inner class OfferRequestsServer.
     This is the behaviour used by Book-seller agents to serve incoming requests
     for offer from buyer agents.
     If the requested book is in the local catalogue the seller agent replies
     with a PROPOSE message specifying the price. Otherwise a REFUSE message is
     sent back.
     */
    private class OfferRequestsServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // CFP Message received. Process it
                Object[] contenido = new Object[8];
                try{
                    contenido = (Object[]) msg.getContentObject();
                }catch (UnreadableException e){
                    e.printStackTrace();
                }

                Integer part_number = getData("select * from "+store+" where name=\'"+contenido[0]+"\'", "part");

                clips.assertString("(customer (customer-id "+contenido[7]+") (name \""+contenido[2]+"\") (address \""+contenido[3]+"\") (phone "+contenido[4]+") (credit-card \""+contenido[6]+"\"))");
                clips.assertString("(order (order-number "+contenido[8]+") (customer-id "+contenido[7]+") (payment-method "+contenido[5]+"))");
                clips.assertString("(line-item (order-number "+contenido[8]+") (part-number "+part_number+") (customer-id "+contenido[7]+"))");

                Integer price = getData("select * from "+store+" where name=\'"+contenido[0]+"\'", "price");

                ACLMessage reply = msg.createReply();

                if (price != null) {
                    // The requested productis available for sale. Reply with the price
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(price.intValue()));
                }
                else {
                    // The requested book is NOT available for sale.
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }  // End of inner class OfferRequestsServer

    /**
     Inner class PurchaseOrdersServer.
     This is the behaviour used by Book-seller agents to serve incoming
     offer acceptances (i.e. purchase orders) from buyer agents.
     The seller agent removes the purchased book from its catalogue
     and replies with an INFORM message to notify the buyer that the
     purchase has been sucesfully completed.
     */
    private class PurchaseOrdersServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // ACCEPT_PROPOSAL Message received. Process it
                String title = msg.getContent();

                Integer price = getData("select * from "+store+" where name=\'"+title+"\'", "price");

                ACLMessage reply = msg.createReply();

                if (price != null) {
                    reply.setPerformative(ACLMessage.INFORM);
                    conectarDB("update "+store+" set stock=stock-1 where name=\'"+title+"\'");

                    System.out.println(title+" sold to agent "+msg.getSender().getName());
                    clips.eval("(facts)");
                    clips.run();
                    int stock = getData("select * from "+store+" where name=\'"+title+"\'", "stock");
                    if(stock == 0 || stock < 0){
                        Supplier(store, title);
                    }
                }
                else {
                    // The requested book has been sold to another buyer in the meanwhile .
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
                clips.clear();
                inicializar();
            }
            else {
                block();
            }
        }
    }  // End of inner class OfferRequestsServer
}