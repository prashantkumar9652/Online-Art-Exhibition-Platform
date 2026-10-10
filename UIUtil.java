package ui;

import model.Artwork;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.function.Function;

// Saare panels ke common UI helpers
public class UIUtil {

    public static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
    }

    public static JTable table(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(24);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setReorderingAllowed(false);
        return t;
    }

    public static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg);
    }

    public static void error(Component parent, Exception e) {
        JOptionPane.showMessageDialog(parent, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    // Image load karta hai; na mile to gradient placeholder deta hai
    public static ImageIcon image(String path, int w, int h) {
        if (path != null && new File(path).exists()) {
            ImageIcon raw = new ImageIcon(path);
            if (raw.getIconWidth() > 0)
                return new ImageIcon(raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        }
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setPaint(new GradientPaint(0, 0, new Color(120, 140, 220), w, h, new Color(220, 140, 180)));
        g.fillRect(0, 0, w, h);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        String t = "No Image";
        g.drawString(t, (w - g.getFontMetrics().stringWidth(t)) / 2, h / 2);
        g.dispose();
        return new ImageIcon(img);
    }

    public static void showImage(Component parent, Artwork a) {
        JLabel l = new JLabel(image(a.getImagePath(), 480, 360));
        JOptionPane.showMessageDialog(parent, l, a.getTitle(), JOptionPane.PLAIN_MESSAGE);
    }

    public static JPanel artworkCard(Artwork a, JComponent... actions) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        card.add(new JLabel(image(a.getImagePath(), 220, 160)), BorderLayout.NORTH);

        JPanel info = new JPanel(new GridLayout(0, 1, 0, 2));
        JLabel title = new JLabel(a.getTitle());
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        info.add(title);
        info.add(new JLabel("by " + a.getArtistName()));
        info.add(new JLabel(String.format("Rs. %.2f", a.getPrice())));
        info.add(new JLabel("Status: " + a.getStatus()));
        card.add(info, BorderLayout.CENTER);

        if (actions.length > 0) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
            for (JComponent c : actions) p.add(c);
            card.add(p, BorderLayout.SOUTH);
        }
        return card;
    }

    // Artworks ka 3-column grid (scrollable). actions = har card ke neeche ke buttons
    public static JComponent cardGrid(List<Artwork> list, Function<Artwork, JComponent[]> actions) {
        if (list.isEmpty()) return new JLabel("Yahan abhi koi artwork nahi hai.", SwingConstants.CENTER);
        JPanel grid = new JPanel(new GridLayout(0, 3, 12, 12));
        grid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        for (Artwork a : list)
            grid.add(artworkCard(a, actions == null ? new JComponent[0] : actions.apply(a)));
        int pad = (3 - list.size() % 3) % 3;
        for (int i = 0; i < pad; i++) grid.add(new JPanel());
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(grid, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(wrap);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    public static JPanel row(Component... comps) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        for (Component c : comps) p.add(c);
        return p;
    }
}
