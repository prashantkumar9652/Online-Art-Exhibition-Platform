package ui;

import dao.OrderDAO;
import model.Order;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

// Artist: sales tracking
public class ArtistSalesPanel extends JPanel implements Refreshable {
    private final User artist;
    private final OrderDAO dao = new OrderDAO();
    private final DefaultTableModel model = UIUtil.model("Order ID", "Artwork", "Buyer", "Amount (Rs.)", "Payment", "Date");
    private final JLabel total = new JLabel(" ");

    public ArtistSalesPanel(User artist) {
        this.artist = artist;
        setLayout(new BorderLayout());
        total.setFont(new Font("SansSerif", Font.BOLD, 14));
        add(UIUtil.row(total), BorderLayout.NORTH);
        add(new JScrollPane(UIUtil.table(model)), BorderLayout.CENTER);
        refresh();
    }

    @Override
    public void refresh() {
        try {
            model.setRowCount(0);
            double sum = 0;
            for (Order o : dao.getByArtist(artist.getId())) {
                model.addRow(new Object[]{o.getId(), o.getArtworkTitle(), o.getBuyerName(),
                        o.getPayableAmount(), o.getPaymentMethod(), o.getDate()});
                sum += o.getPayableAmount();
            }
            total.setText(String.format("Total sales: %d artwork(s)   |   Revenue: Rs. %.2f", model.getRowCount(), sum));
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }
}
