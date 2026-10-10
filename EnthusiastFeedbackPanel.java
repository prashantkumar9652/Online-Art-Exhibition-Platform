package ui;

import dao.ArtworkDAO;
import dao.FeedbackDAO;
import model.Artwork;
import model.Feedback;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

// Enthusiast: artwork par feedback dena aur artists ke replies dekhna
public class EnthusiastFeedbackPanel extends JPanel implements Refreshable {
    private final User user;
    private final ArtworkDAO artworkDAO = new ArtworkDAO();
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();
    private final JComboBox<Artwork> artBox = new JComboBox<>();
    private final JTextArea msgA = new JTextArea(3, 40);
    private final DefaultTableModel model = UIUtil.model("Artist", "About Artwork", "Reply", "Time");

    public EnthusiastFeedbackPanel(User user) {
        this.user = user;
        setLayout(new BorderLayout(0, 8));

        JPanel form = new JPanel(new BorderLayout(6, 6));
        form.setBorder(BorderFactory.createTitledBorder("Give Feedback"));
        JButton send = new JButton("Submit Feedback");
        form.add(UIUtil.row(new JLabel("Artwork:"), artBox), BorderLayout.NORTH);
        form.add(new JScrollPane(msgA), BorderLayout.CENTER);
        form.add(UIUtil.row(send), BorderLayout.SOUTH);

        JPanel replies = new JPanel(new BorderLayout());
        replies.setBorder(BorderFactory.createTitledBorder("Replies from Artists"));
        replies.add(new JScrollPane(UIUtil.table(model)), BorderLayout.CENTER);

        add(form, BorderLayout.NORTH);
        add(replies, BorderLayout.CENTER);
        send.addActionListener(e -> send());
        refresh();
    }

    @Override
    public void refresh() {
        try {
            artBox.removeAllItems();
            for (Artwork a : artworkDAO.getAll())
                if ("APPROVED".equals(a.getStatus()) || "SOLD".equals(a.getStatus())) artBox.addItem(a);
            model.setRowCount(0);
            for (Feedback f : feedbackDAO.getReceived(user.getId()))
                model.addRow(new Object[]{f.getSenderName(), f.getArtworkTitle(), f.getMessage(), f.getSentAt()});
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }

    private void send() {
        Artwork a = (Artwork) artBox.getSelectedItem();
        String msg = msgA.getText().trim();
        if (a == null || msg.isEmpty()) {
            UIUtil.info(this, "Artwork select karo aur feedback likho.");
            return;
        }
        try {
            feedbackDAO.add(user.getId(), a.getArtistId(), a.getId(), msg);
            msgA.setText("");
            UIUtil.info(this, "Feedback artist ko bhej diya. Thank you!");
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }
}
