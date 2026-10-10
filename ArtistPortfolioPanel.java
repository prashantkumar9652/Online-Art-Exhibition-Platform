package ui;

import dao.ArtworkDAO;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

// Artist: apne artworks ka visual portfolio (status ke saath)
public class ArtistPortfolioPanel extends JPanel implements Refreshable {
    private final User artist;
    private final ArtworkDAO dao = new ArtworkDAO();
    private JComponent content;

    public ArtistPortfolioPanel(User artist) {
        this.artist = artist;
        setLayout(new BorderLayout());
        refresh();
    }

    @Override
    public void refresh() {
        if (content != null) remove(content);
        try {
            content = UIUtil.cardGrid(dao.getByArtist(artist.getId()), null);
        } catch (SQLException e) {
            content = new JLabel("Error: " + e.getMessage());
        }
        add(content, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}
