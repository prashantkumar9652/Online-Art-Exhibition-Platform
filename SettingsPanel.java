package ui;

import dao.SettingsDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.Map;

// Admin: system-wide settings (key/value)
public class SettingsPanel extends JPanel implements Refreshable {
    private final SettingsDAO dao = new SettingsDAO();
    private final DefaultTableModel model = UIUtil.model("Setting", "Value");
    private final JTable table = UIUtil.table(model);

    public SettingsPanel() {
        setLayout(new BorderLayout());
        JButton addBtn = new JButton("Add / Update Setting");
        JButton delBtn = new JButton("Delete Selected");
        JLabel hint = new JLabel("  tax_percent = purchase par tax %, max_artwork_price = artist upload limit");
        add(UIUtil.row(addBtn, delBtn, hint), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        addBtn.addActionListener(e -> save());
        delBtn.addActionListener(e -> delete());
        refresh();
    }

    @Override
    public void refresh() {
        try {
            model.setRowCount(0);
            for (Map.Entry<String, String> e : dao.getAll().entrySet())
                model.addRow(new Object[]{e.getKey(), e.getValue()});
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }

    private void save() {
        int row = table.getSelectedRow();
        JTextField key = new JTextField(row >= 0 ? (String) model.getValueAt(row, 0) : "", 15);
        JTextField value = new JTextField(row >= 0 ? (String) model.getValueAt(row, 1) : "", 15);
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        p.add(new JLabel("Setting name:")); p.add(key);
        p.add(new JLabel("Value:")); p.add(value);
        if (JOptionPane.showConfirmDialog(this, p, "Setting", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                != JOptionPane.OK_OPTION) return;
        String k = key.getText().trim(), v = value.getText().trim();
        if (k.isEmpty() || v.isEmpty()) {
            UIUtil.info(this, "Name aur value dono chahiye.");
            return;
        }
        try {
            dao.set(k, v);
            UIUtil.info(this, "Setting save ho gayi.");
            refresh();
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }

    private void delete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtil.info(this, "Pehle ek setting select karo.");
            return;
        }
        try {
            dao.delete((String) model.getValueAt(row, 0));
            refresh();
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }
}
