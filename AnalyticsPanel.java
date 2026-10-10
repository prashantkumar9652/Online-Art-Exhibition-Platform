package ui;

import dao.ExhibitionDAO;
import dao.FeedbackDAO;
import dao.OrderDAO;
import dao.UserDAO;
import util.DB;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

// Admin: content analytics (graphs)
public class AnalyticsPanel extends JPanel implements Refreshable {
    private final JLabel summary = new JLabel(" ");
    private final JPanel charts = new JPanel(new GridLayout(2, 2, 10, 10));

    public AnalyticsPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        summary.setFont(new Font("SansSerif", Font.BOLD, 14));
        add(summary, BorderLayout.NORTH);
        add(charts, BorderLayout.CENTER);
        refresh();
    }

    @Override
    public void refresh() {
        try {
            OrderDAO orders = new OrderDAO();
            summary.setText(String.format("Users: %d   |   Artworks: %d   |   Orders: %d   |   Revenue: Rs. %.2f",
                    DB.count("SELECT COUNT(*) FROM users"), DB.count("SELECT COUNT(*) FROM artworks"),
                    DB.count("SELECT COUNT(*) FROM orders"), orders.totalRevenue()));
            charts.removeAll();
            charts.add(new BarChart("Users by Role", new UserDAO().countByRole()));
            charts.add(new BarChart("Participants per Exhibition", new ExhibitionDAO().participantStats()));
            charts.add(new BarChart("Revenue per Artist (Rs.)", orders.revenueByArtist()));
            charts.add(new BarChart("Feedback Received per Artist", new FeedbackDAO().interactionsByReceiver()));
            charts.revalidate();
            charts.repaint();
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }
}
