package ui;

import dao.FeedbackDAO;
import model.Feedback;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Artist: art enthusiasts ka feedback dekhna aur reply karna
public class ArtistFeedbackPanel extends JPanel implements Refreshable {
    private final User artist;
    private final FeedbackDAO dao = new FeedbackDAO();
    private final DefaultTableModel model = UIUtil.model("From", "Artwork", "Message", "Time");
    private final JTable table = UIUtil.table(model);
    private List<Feedback> received = new ArrayList<>();

    public ArtistFeedbackPanel(User artist) {
        this.artist = artist;
        setLayout(new BorderLayout());
        JButton reply = new JButton("Reply to Selected");
        add(UIUtil.row(reply), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        table.getColumnModel().getColumn(2).setPreferredWidth(400);
        reply.addActionListener(e -> reply());
        refresh();
    }

    @Override
    public void refresh() {
        try {
            received = dao.getReceived(artist.getId());
            model.setRowCount(0);
            for (Feedback f : received)
                model.addRow(new Object[]{f.getSenderName(), f.getArtworkTitle(), f.getMessage(), f.getSentAt()});
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }

    private void reply() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtil.info(this, "Pehle ek feedback select karo.");
            return;
        }
        Feedback f = received.get(row);
        String msg = JOptionPane.showInputDialog(this, "Reply to " + f.getSenderName() + ":");
        if (msg == null || msg.trim().isEmpty()) return;
        Integer artworkId = f.getArtworkId() == 0 ? null : Integer.valueOf(f.getArtworkId());
        try {
            dao.add(artist.getId(), f.getSenderId(), artworkId, msg.trim());
            UIUtil.info(this, "Reply bhej diya.");
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }
}
