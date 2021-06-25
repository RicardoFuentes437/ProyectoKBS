package test.src.Project1;

import jade.core.AID;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class CustomerGui extends JFrame {
    private CustomerAgent myAgent;
    private JTextField titleField, priceField, nameField, addressField, phoneField, cardField, methodField, idField, orderField;

    CustomerGui(CustomerAgent a) {
        super(a.getLocalName());

        myAgent = a;

        JPanel p = new JPanel();
        p.setLayout(new GridLayout(10, 10));
        p.add(new JLabel("Product:"));
        titleField = new JTextField(15);
        p.add(titleField);
        p.add(new JLabel("Price:"));
        priceField = new JTextField(15);
        p.add(priceField);
        p.add(new JLabel("Customer Name:"));
        nameField = new JTextField(15);
        p.add(nameField);
        p.add(new JLabel("Customer Id:"));
        idField = new JTextField(15);
        p.add(idField);
        p.add(new JLabel("Order Id:"));
        orderField = new JTextField(15);
        p.add(orderField);
        p.add(new JLabel("Address:"));
        addressField = new JTextField(15);
        p.add(addressField);
        p.add(new JLabel("Phone Number:"));
        phoneField = new JTextField(15);
        p.add(phoneField);
        p.add(new JLabel("Payment Method:"));
        methodField = new JTextField(15);
        p.add(methodField);
        p.add(new JLabel("Credit Card:"));
        cardField = new JTextField(15);
        p.add(cardField);
        getContentPane().add(p, BorderLayout.CENTER);

        JButton addButton = new JButton("Buy");
        addButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    String title = titleField.getText().trim();
                    String price = priceField.getText().trim();
                    String customer = nameField.getText().trim();
                    String address = addressField.getText().trim();
                    String phone = phoneField.getText().trim();
                    String method = methodField.getText().trim();
                    String card = cardField.getText().trim();
                    String id = idField.getText().trim();
                    String order = orderField.getText().trim();
                    myAgent.definePurchase(title, Integer.parseInt(price), customer, address, phone, method, card, id, order, a);
                    titleField.setText("");
                    priceField.setText("");
                    nameField.setText("");
                    addressField.setText("");
                    phoneField.setText("");
                    methodField.setText("");
                    cardField.setText("");
                    idField.setText("");
                    orderField.setText("");
                }
                catch (Exception e) {
                    JOptionPane.showMessageDialog(CustomerGui.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } );
        p = new JPanel();
        p.add(addButton);
        getContentPane().add(p, BorderLayout.SOUTH);

        // Make the agent terminate when the user closes
        // the GUI using the button on the upper right corner
        addWindowListener(new	WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                myAgent.doDelete();
            }
        } );

        setResizable(false);
    }

    public void showGui() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int)screenSize.getWidth() / 2;
        int centerY = (int)screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.setVisible(true);
    }
}
