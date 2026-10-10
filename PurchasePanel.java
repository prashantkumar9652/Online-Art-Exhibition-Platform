package ui;

import dao.ArtworkDAO;
import dao.OrderDAO;
import dao.SettingsDAO;
import exception.ArtworkAlreadySoldException;
import model.Artwork;
import model.Cart;
import model.Order;
import model.Payable;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Enthusiast: cart, checkout (transaction), transaction history
public class PurchasePanel extends JPanel implements Refreshable {
    private final User user;
    private final Cart cart;
    private final OrderDAO orderDAO = new OrderDAO();
    private final ArtworkDAO artworkDAO = new ArtworkDAO();
    private final DefaultTableModel cartModel = UIUtil.model("ID", "Title", "Artist", "Price (Rs.)");
    private final JTable cartTable = UIUtil.table(cartModel);
    private final DefaultTableModel histModel = UIUtil.model("Order ID", "Artwork", "Amount (Rs.)", "Payment", "Date");
    private final JComboBox<String> payBox =
            new JComboBox<>(new String[]{"UPI", "Credit/Debit Card", "Net Banking", "Cash on Delivery"});
    private final JLabel totalLabel = new JLabel(" ");

    public PurchasePanel(User user, Cart cart) {
        this.user = user;
        this.cart = cart;
        setLayout(new GridLayout(2, 1, 0, 8));

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createTitledBorder("My Cart"));
        JButton remove = new JButton("Remove Selected");
        JButton checkout = new JButton("Pay & Checkout");
        JPanel bar = UIUtil.row(remove, new JLabel("Payment:"), payBox, checkout, totalLabel);
        top.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        top.add(bar, BorderLayout.SOUTH);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createTitledBorder("Transaction History"));
        bottom.add(new JScrollPane(UIUtil.table(histModel)), BorderLayout.CENTER);

        add(top);
        add(bottom);
        remove.addActionListener(e -> {
            int row = cartTable.getSelectedRow();
            if (row < 0) {
                UIUtil.info(this, "Pehle cart se ek item select karo.");
                return;
            }
            cart.remove((int) cartModel.getValueAt(row, 0));
            refresh();
        });
        checkout.addActionListener(e -> checkout());
        refresh();
    }

    @Override
    public void refresh() {
        cartModel.setRowCount(0);
        for (Artwork a : cart.getItems())
            cartModel.addRow(new Object[]{a.getId(), a.getTitle(), a.getArtistName(), a.getPrice()});
        totalLabel.setText(String.format("   Subtotal: Rs. %.2f (+ tax at checkout)", cart.total()));
        try {
            histModel.setRowCount(0);
            for (Order o : orderDAO.getByUser(user.getId()))
                histModel.addRow(new Object[]{o.getId(), o.getArtworkTitle(), o.getPayableAmount(),
                        o.getPaymentMethod(), o.getDate()});
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }

    private void checkout() {
        if (cart.isEmpty()) {
            UIUtil.info(this, "Cart khali hai. Gallery se artwork add karo.");
            return;
        }
        try {
            List<Order> receipts = orderDAO.purchase(user.getId(), new ArrayList<>(cart.getItems()),
                    (String) payBox.getSelectedItem());
            showReceipt(receipts);
            cart.clear();
            refresh();
        } catch (ArtworkAlreadySoldException e) {
            UIUtil.info(this, "Purchase cancel (koi paisa nahi kata): " + e.getMessage());
            dropUnavailableItems();
            refresh();
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }

    private void dropUnavailableItems() {
        try {
            for (Artwork a : new ArrayList<>(cart.getItems())) {
                Artwork fresh = artworkDAO.getById(a.getId());
                if (fresh == null || !"APPROVED".equals(fresh.getStatus())) cart.remove(a.getId());
            }
        } catch (SQLException ignored) {
        }
    }

    private void showReceipt(List<Order> receipts) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("===== PURCHASE RECEIPT =====\n");
        sb.append("Customer : ").append(user.getName()).append("\n");
        sb.append("Site     : ").append(new SettingsDAO().get("site_name", "Art Exhibition")).append("\n\n");
        double total = 0;
        for (Order o : receipts) {
            Payable p = o; // interface ke through amount/method
            sb.append(String.format("%-28s Rs. %10.2f%n", o.getArtworkTitle(), p.getPayableAmount()));
            total += p.getPayableAmount();
        }
        sb.append("\nPayment  : ").append(receipts.get(0).getPaymentMethod());
        sb.append(String.format("%nTOTAL    : Rs. %.2f (tax included)%n", total));
        sb.append("\nThank you! Artwork ab 'My Collection' mein hai.");
        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Receipt", JOptionPane.INFORMATION_MESSAGE);
    }
}
