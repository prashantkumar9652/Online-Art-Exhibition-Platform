package ui;

import dao.ExhibitionDAO;
import model.Exhibition;
import model.User;
import util.CountdownThread;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Enthusiast: exhibition join karna + live countdown (THREAD)
public class EnthusiastExhibitionPanel extends JPanel implements Refreshable {
    private final User user;
    private final ExhibitionDAO dao = new ExhibitionDAO();
    private final DefaultTableModel model =
            UIUtil.model("ID", "Exhibition", "Start", "End", "Status", "Participants", "Joined?");
    private final JTable table = UIUtil.table(model);
    private final JLabel countdownLabel = new JLabel("Exhibition select karo, countdown yahan chalega");
    private List<Exhibition> exhibitions = new ArrayList<>();
    private CountdownThread countdown;

    public EnthusiastExhibitionPanel(User user) {
        this.user = user;
        setLayout(new BorderLayout());
        JButton join = new JButton("Join Selected");
        JButton view = new JButton("View Artworks");
        countdownLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        JPanel top = new JPanel(new BorderLayout());
        top.add(UIUtil.row(join, view), BorderLayout.NORTH);
        top.add(UIUtil.row(countdownLabel), BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        join.addActionListener(e -> join());
        view.addActionListener(e -> viewArtworks());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0)
                startCountdown(exhibitions.get(table.getSelectedRow()));
        });
        refresh();
    }

    @Override
    public void refresh() {
        try {
            exhibitions = dao.getAll();
            model.setRowCount(0);
            for (Exhibition e : exhibitions)
                model.addRow(new Object[]{e.getId(), e.getTitle(), e.getStartTime().format(AdminContentPanel.FMT),
                        e.getEndTime().format(AdminContentPanel.FMT), e.getStatus(),
                        dao.participantCount(e.getId()), dao.isJoined(e.getId(), user.getId()) ? "Yes" : "No"});
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }

    // Purana thread band karke naye exhibition ke liye naya thread
    private void startCountdown(Exhibition ex) {
        stopCountdown();
        countdown = new CountdownThread(ex, countdownLabel);
        countdown.start();
    }

    private void stopCountdown() {
        if (countdown != null) countdown.interrupt();
    }

    @Override
    public void removeNotify() {
        stopCountdown();
        super.removeNotify();
    }

    private Exhibition selected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtil.info(this, "Pehle ek exhibition select karo.");
            return null;
        }
        return exhibitions.get(row);
    }

    private void join() {
        Exhibition ex = selected();
        if (ex == null) return;
        if ("ENDED".equals(ex.getStatus())) {
            UIUtil.info(this, "Ye exhibition khatam ho chuki hai.");
            return;
        }
        try {
            UIUtil.info(this, dao.join(ex.getId(), user.getId())
                    ? "Participation successful! Tum '" + ex.getTitle() + "' mein join ho gaye."
                    : "Tum pehle se is exhibition mein ho.");
            refresh();
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }

    private void viewArtworks() {
        Exhibition ex = selected();
        if (ex == null) return;
        try {
            JComponent grid = UIUtil.cardGrid(dao.getApprovedArtworks(ex.getId()), null);
            grid.setPreferredSize(new Dimension(780, 460));
            JOptionPane.showMessageDialog(this, grid, ex.getTitle() + " - Artworks", JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }
}
