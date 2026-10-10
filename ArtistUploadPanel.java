package ui;

import dao.ArtworkDAO;
import dao.NotificationDAO;
import dao.SettingsDAO;
import model.Artwork;
import model.User;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Artist: artwork upload + manage (edit/delete)
public class ArtistUploadPanel extends JPanel implements Refreshable {
    private final User artist;
    private final ArtworkDAO dao = new ArtworkDAO();
    private final JTextField titleF = new JTextField(20);
    private final JTextArea descA = new JTextArea(3, 20);
    private final JTextField priceF = new JTextField(10);
    private final JLabel imageLabel = new JLabel("No image selected");
    private File chosenImage;
    private final DefaultTableModel model = UIUtil.model("ID", "Title", "Price", "Status");
    private final JTable table = UIUtil.table(model);
    private List<Artwork> mine = new ArrayList<>();

    public ArtistUploadPanel(User artist) {
        this.artist = artist;
        setLayout(new BorderLayout(0, 8));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Artwork Details"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0; form.add(new JLabel("Title:"), c);
        c.gridx = 1; form.add(titleF, c);
        c.gridx = 0; c.gridy = 1; form.add(new JLabel("Description:"), c);
        c.gridx = 1; form.add(new JScrollPane(descA), c);
        c.gridx = 0; c.gridy = 2; form.add(new JLabel("Price (Rs.):"), c);
        c.gridx = 1; form.add(priceF, c);
        JButton browse = new JButton("Choose Image...");
        c.gridx = 0; c.gridy = 3; form.add(browse, c);
        c.gridx = 1; form.add(imageLabel, c);

        JButton upload = new JButton("Upload New");
        JButton update = new JButton("Update Selected");
        JButton delete = new JButton("Delete Selected");
        JButton clear = new JButton("Clear Form");
        c.gridx = 0; c.gridy = 4; c.gridwidth = 2;
        form.add(UIUtil.row(upload, update, delete, clear), c);

        add(form, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        browse.addActionListener(e -> chooseImage());
        upload.addActionListener(e -> upload());
        update.addActionListener(e -> updateSelected());
        delete.addActionListener(e -> deleteSelected());
        clear.addActionListener(e -> clearForm());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) fillForm(mine.get(table.getSelectedRow()));
        });
        refresh();
    }

    @Override
    public void refresh() {
        try {
            mine = dao.getByArtist(artist.getId());
            model.setRowCount(0);
            for (Artwork a : mine) model.addRow(new Object[]{a.getId(), a.getTitle(), a.getPrice(), a.getStatus()});
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }

    private void chooseImage() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Images (jpg, png, gif)", "jpg", "jpeg", "png", "gif"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            chosenImage = fc.getSelectedFile();
            imageLabel.setText(chosenImage.getName());
        }
    }

    // Chosen image ko project ke images/ folder mein copy karta hai
    private String copyImage() throws IOException {
        if (chosenImage == null) return null;
        File dir = new File("images");
        dir.mkdirs();
        File dest = new File(dir, System.currentTimeMillis() + "_" + chosenImage.getName());
        Files.copy(chosenImage.toPath(), dest.toPath());
        return dest.getPath();
    }

    private double readPrice() throws SQLException {
        double price = Double.parseDouble(priceF.getText().trim());
        double max = Double.parseDouble(new SettingsDAO().get("max_artwork_price", "1000000"));
        if (price <= 0 || price > max) throw new NumberFormatException("range");
        return price;
    }

    private void upload() {
        String title = titleF.getText().trim();
        if (title.isEmpty()) {
            UIUtil.info(this, "Title daalo.");
            return;
        }
        try {
            double price = readPrice();
            dao.add(new Artwork(0, artist.getId(), title, descA.getText().trim(), copyImage(), price, "PENDING"));
            new NotificationDAO().add("New artwork '" + title + "' submitted by " + artist.getName() + " for approval");
            UIUtil.info(this, "Artwork upload ho gaya! Admin approval ke baad gallery mein dikhega.");
            clearForm();
            refresh();
        } catch (NumberFormatException e) {
            UIUtil.info(this, "Price sahi daalo (0 se zyada, aur admin ki max limit ke andar).");
        } catch (SQLException | IOException e) {
            UIUtil.error(this, e);
        }
    }

    private void updateSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtil.info(this, "Pehle table se artwork select karo.");
            return;
        }
        Artwork old = mine.get(row);
        if ("SOLD".equals(old.getStatus())) {
            UIUtil.info(this, "Sold artwork edit nahi ho sakta.");
            return;
        }
        try {
            double price = readPrice();
            String img = chosenImage != null ? copyImage() : old.getImagePath();
            dao.update(new Artwork(old.getId(), artist.getId(), titleF.getText().trim(),
                    descA.getText().trim(), img, price, old.getStatus()));
            UIUtil.info(this, "Artwork update ho gaya.");
            refresh();
        } catch (NumberFormatException e) {
            UIUtil.info(this, "Price sahi daalo.");
        } catch (SQLException | IOException e) {
            UIUtil.error(this, e);
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtil.info(this, "Pehle table se artwork select karo.");
            return;
        }
        Artwork a = mine.get(row);
        if ("SOLD".equals(a.getStatus())) {
            UIUtil.info(this, "Sold artwork delete nahi ho sakta.");
            return;
        }
        if (!UIUtil.confirm(this, "'" + a.getTitle() + "' delete karna hai?")) return;
        try {
            dao.delete(a.getId());
            clearForm();
            refresh();
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }

    private void fillForm(Artwork a) {
        titleF.setText(a.getTitle());
        descA.setText(a.getDescription());
        priceF.setText(String.valueOf(a.getPrice()));
        chosenImage = null;
        imageLabel.setText(a.getImagePath() == null ? "No image" : "Current: " + new File(a.getImagePath()).getName());
    }

    private void clearForm() {
        titleF.setText("");
        descA.setText("");
        priceF.setText("");
        chosenImage = null;
        imageLabel.setText("No image selected");
        table.clearSelection();
    }
}
