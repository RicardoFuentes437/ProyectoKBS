package test.src.Project1;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.*;

public class SupplierAgent extends Agent {

    private String targetProduct;

    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            targetProduct = (String) args[0];
        }
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("product-supply-"+targetProduct);
        sd.setName("JADE-product-supplier");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new OfferRequestsServer());
    }

    protected void takeDown() {

        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        System.out.println("supplier-agent "+getAID().getName()+" terminating.");
    }

    public void conectarDB(String s){

        try{
            DriverManager.registerDriver (new oracle.jdbc.OracleDriver());
            Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1522:orcl", "ricardof", "12345");
            Statement stmt=conn.createStatement();
            ResultSet rs=stmt.executeQuery(s);
            conn.close();
        } catch (SQLException e)
        {
            e.printStackTrace();
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
                String table = msg.getContent();
                conectarDB("update "+table+" set stock=2 where name=\'"+targetProduct+"\'");
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(targetProduct);
                System.out.println("Product "+targetProduct+" re-stocked "+msg.getSender().getName());
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }

}