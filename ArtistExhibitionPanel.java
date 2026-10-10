package ui;

import dao.ArtworkDAO;
import dao.ExhibitionDAO;
import dao.NotificationDAO;
import model.Artwork;
import model.Exhibition;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Artist: exhibitions mein apna approved artwork submit karna
public class ArtistExhibitionPanel extends JPanel implements Refreshable {
    private final User artist;
    private final ExhibitionDAO exDAO = new ExhibitionDAO();
    private final ArtworkDAO artworkDAO = new ArtworkDAO();
    private final DefaultTableModel exModel = UIUtil.model("ID", "Exhibition", "Start", "End", "Status");
    private final JTable exTable = UIUtil.table(exModel);
    private final DefaultTableModel subModel = UIUtil.model("Exhibition", "Artwork", "Approval Status");
    private final JComboBox<Artwork> artBox = new JComboBox<>();
    private List<Exhibition> exhibitions = new ArrayList<>();

    public ArtistExhibitionPanel(User artist) {
        this.artist = artist;
        setLayout(new BorderLayout(0, 8));

        JButton submit = new JButton("Submit Artwork to Selected Exhibition");
        JPanel top = new JPanel(new BorderLayout());
        top.add(new JLabel("  Upcoming / Live exhibitions (sirf APPROVED artwork submit hota hai)"), BorderLayout.NORTH);
        top.add(new JScrollPane(exTable), BorderLayout.CENTER);
        top.add(UIUtil.row(new JLabel("My artwork:"), artBox, submit), BorderLayout.SOUTH);
        top.setPreferredSize(new Dimension(800, 260));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(new JLabel("  My participation / submissions"), BorderLayout.NORTH);
        bottom.add(new JScrollPane(UIUtil.table(subModel)), BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(bottom, BorderLayout.CENTER);
        submit.addActionListener(e -> submit());
        refresh();
    }

    @Override
    public void refresh() {
        try {
            exhibitions = exDAO.getAll();
            exModel.setRowCount(0);
            for (Exhibition e : exhibitions)
                exModel.addRow(new Object[]{e.getId(), e.getTitle(), e.getStartTime().format(AdminContentPanel.FMT),
                        e.getEndTime().format(AdminContentPanel.FMT), e.getStatus()});
            artBox.removeAllItems();
            for (Artwork a : artworkDAO.getByArtist(artist.getId()))
                if ("APPROVED".equals(a.getStatus())) artBox.addItem(a);
            subModel.setRowCount(0);
            for (Object[] r : exDAO.getSubmissionsByArtist(artist.getId()))
                subModel.addRow(new Object[]{r[2], r[3], r[5]});
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }

    private void submit() {
        int row = exTable.getSelectedRow();
        Artwork art = (Artwork) artBox.getSelectedItem();
        if (row < 0 || art == null) {
            UIUtil.info(this, "Exhibition select karo, aur ek approved artwork bhi hona chahiye.");
            return;
        }
        Exhibition ex = exhibitions.get(row);
        if ("ENDED".equals(ex.getStatus())) {
            UIUtil.info(this, "Ye exhibition khatam ho chuki hai.");
            return;
        }
        try {
            if (exDAO.submitArtwork(ex.getId(), art.getId())) {
                new NotificationDAO().add(artist.getName() + " submitted '" + art.getTitle() + "' to '" + ex.getTitle() + "'");
                UIUtil.info(this, "Participation submit ho gayi! Admin approval ka wait karo.");
            } else {
                UIUtil.info(this, "Ye artwork is exhibition mein pehle se submit hai.");
            }
            refresh();
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }
}
