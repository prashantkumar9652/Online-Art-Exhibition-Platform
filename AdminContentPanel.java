package ui;

import dao.ArtworkDAO;
import dao.ExhibitionDAO;
import dao.NotificationDAO;
import model.Artwork;
import model.Exhibition;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

// Admin: exhibition content management (artwork approvals, exhibition submissions, exhibitions)
public class AdminContentPanel extends JPanel implements Refreshable {
    static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ArtworkDAO artworkDAO = new ArtworkDAO();
    private final ExhibitionDAO exDAO = new ExhibitionDAO();
    private final NotificationDAO notifDAO = new NotificationDAO();

    // Tab 1: artwork approvals
    private final DefaultTableModel artModel = UIUtil.model("ID", "Title", "Artist", "Price", "Status");
    private final JTable artTable = UIUtil.table(artModel);
    private final JComboBox<String> artFilter = new JComboBox<>(new String[]{"PENDING", "APPROVED", "REJECTED", "SOLD"});
    private List<Artwork> shownArtworks = new ArrayList<>();

    // Tab 2: exhibition submissions
    private final DefaultTableModel subModel = UIUtil.model("Exh ID", "Art ID", "Exhibition", "Artwork", "Artist", "Status");
    private final JTable subTable = UIUtil.table(subModel);
    private final JComboBox<String> subFilter = new JComboBox<>(new String[]{"PENDING", "APPROVED", "REJECTED"});

    // Tab 3: exhibitions
    private final DefaultTableModel exModel = UIUtil.model("ID", "Title", "Start", "End", "Status", "Participants");
    private final JTable exTable = UIUtil.table(exModel);
    private final JTextField titleF = new JTextField(14);
    private final JTextField descF = new JTextField(18);
    private final JTextField startF = new JTextField(LocalDateTime.now().plusDays(1).withMinute(0).format(FMT), 12);
    private final JTextField endF = new JTextField(LocalDateTime.now().plusDays(3).withMinute(0).format(FMT), 12);

    public AdminContentPanel() {
        setLayout(new BorderLayout());
        JTabbedPane inner = new JTabbedPane();
        inner.addTab("Artwork Approvals", buildArtworkTab());
        inner.addTab("Exhibition Submissions", buildSubmissionTab());
        inner.addTab("Exhibitions", buildExhibitionTab());
        add(inner, BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildArtworkTab() {
        JPanel p = new JPanel(new BorderLayout());
        JButton approve = new JButton("Approve");
        JButton reject = new JButton("Reject");
        JButton view = new JButton("View Image");
        p.add(UIUtil.row(new JLabel("Show:"), artFilter, approve, reject, view), BorderLayout.NORTH);
        p.add(new JScrollPane(artTable), BorderLayout.CENTER);
        artFilter.addActionListener(e -> loadArtworks());
        approve.addActionListener(e -> setArtworkStatus("APPROVED"));
        reject.addActionListener(e -> setArtworkStatus("REJECTED"));
        view.addActionListener(e -> {
            Artwork a = selectedArtwork();
            if (a != null) UIUtil.showImage(this, a);
        });
        return p;
    }

    private JPanel buildSubmissionTab() {
        JPanel p = new JPanel(new BorderLayout());
        JButton approve = new JButton("Approve");
        JButton reject = new JButton("Reject");
        p.add(UIUtil.row(new JLabel("Show:"), subFilter, approve, reject), BorderLayout.NORTH);
        p.add(new JScrollPane(subTable), BorderLayout.CENTER);
        subFilter.addActionListener(e -> loadSubmissions());
        approve.addActionListener(e -> setSubmissionStatus("APPROVED"));
        reject.addActionListener(e -> setSubmissionStatus("REJECTED"));
        return p;
    }

    private JPanel buildExhibitionTab() {
        JPanel p = new JPanel(new BorderLayout());
        JButton create = new JButton("Create");
        JButton delete = new JButton("Delete Selected");
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        form.add(new JLabel("Title:")); form.add(titleF);
        form.add(new JLabel("Description:")); form.add(descF);
        form.add(new JLabel("Start (yyyy-MM-dd HH:mm):")); form.add(startF);
        form.add(new JLabel("End:")); form.add(endF);
        form.add(create); form.add(delete);
        p.add(form, BorderLayout.NORTH);
        p.add(new JScrollPane(exTable), BorderLayout.CENTER);
        create.addActionListener(e -> createExhibition());
        delete.addActionListener(e -> deleteExhibition());
        return p;
    }

    @Override
    public void refresh() {
        loadArtworks();
        loadSubmissions();
        loadExhibitions();
    }

    private void loadArtworks() {
        try {
            shownArtworks = artworkDAO.getByStatus((String) artFilter.getSelectedItem());
            artModel.setRowCount(0);
            for (Artwork a : shownArtworks)
                artModel.addRow(new Object[]{a.getId(), a.getTitle(), a.getArtistName(), a.getPrice(), a.getStatus()});
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }

    private void loadSubmissions() {
        try {
            subModel.setRowCount(0);
            for (Object[] row : exDAO.getSubmissions((String) subFilter.getSelectedItem())) subModel.addRow(row);
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }

    private void loadExhibitions() {
        try {
            exModel.setRowCount(0);
            for (Exhibition e : exDAO.getAll())
                exModel.addRow(new Object[]{e.getId(), e.getTitle(), e.getStartTime().format(FMT),
                        e.getEndTime().format(FMT), e.getStatus(), exDAO.participantCount(e.getId())});
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }

    private Artwork selectedArtwork() {
        int row = artTable.getSelectedRow();
        if (row < 0) {
            UIUtil.info(this, "Pehle ek artwork select karo.");
            return null;
        }
        return shownArtworks.get(row);
    }

    private void setArtworkStatus(String status) {
        Artwork a = selectedArtwork();
        if (a == null) return;
        if ("SOLD".equals(a.getStatus())) {
            UIUtil.info(this, "Sold artwork ka status change nahi ho sakta.");
            return;
        }
        try {
            artworkDAO.updateStatus(a.getId(), status);
            notifDAO.add("Artwork '" + a.getTitle() + "' has been " + status.toLowerCase());
            loadArtworks();
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }

    private void setSubmissionStatus(String status) {
        int row = subTable.getSelectedRow();
        if (row < 0) {
            UIUtil.info(this, "Pehle ek submission select karo.");
            return;
        }
        try {
            exDAO.setSubmissionStatus((int) subModel.getValueAt(row, 0), (int) subModel.getValueAt(row, 1), status);
            notifDAO.add("'" + subModel.getValueAt(row, 3) + "' " + status.toLowerCase()
                    + " for exhibition '" + subModel.getValueAt(row, 2) + "'");
            loadSubmissions();
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }

    private void createExhibition() {
        String title = titleF.getText().trim();
        if (title.isEmpty()) {
            UIUtil.info(this, "Exhibition ka title daalo.");
            return;
        }
        try {
            LocalDateTime start = LocalDateTime.parse(startF.getText().trim(), FMT);
            LocalDateTime end = LocalDateTime.parse(endF.getText().trim(), FMT);
            if (!end.isAfter(start)) {
                UIUtil.info(this, "End time start ke baad ka hona chahiye.");
                return;
            }
            exDAO.add(new Exhibition(0, title, descF.getText().trim(), start, end));
            notifDAO.add("New exhibition announced: " + title);
            titleF.setText("");
            descF.setText("");
            loadExhibitions();
        } catch (DateTimeParseException e) {
            UIUtil.info(this, "Date format galat hai. Aise likho: 2026-10-05 10:00");
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }

    private void deleteExhibition() {
        int row = exTable.getSelectedRow();
        if (row < 0) {
            UIUtil.info(this, "Pehle ek exhibition select karo.");
            return;
        }
        if (!UIUtil.confirm(this, "Ye exhibition delete karni hai?")) return;
        try {
            exDAO.delete((int) exModel.getValueAt(row, 0));
            loadExhibitions();
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }
}
