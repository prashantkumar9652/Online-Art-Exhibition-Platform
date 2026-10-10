package ui;

import dao.NotificationDAO;
import model.Notification;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

// Admin: system-wide notifications bhejna aur manage karna
public class AdminNotificationPanel extends JPanel implements Refreshable {
    private final NotificationDAO dao = new NotificationDAO();
    private final DefaultTableModel model = UIUtil.model("ID", "Message", "Time");
    private final JTable table = UIUtil.table(model);
    private final JTextField msgField = new JTextField(40);

    public AdminNotificationPanel() {
        setLayout(new BorderLayout());
        JButton send = new JButton("Send to Everyone");
        JButton delete = new JButton("Delete Selected");
        add(UIUtil.row(new JLabel("Message:"), msgField, send, delete), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        table.getColumnModel().getColumn(0).setMaxWidth(60);

        send.addActionListener(e -> {
            String m = msgField.getText().trim();
            if (m.isEmpty()) {
                UIUtil.info(this, "Message likho.");
                return;
            }
            try {
                dao.add(m);
                msgField.setText("");
                refresh();
                UIUtil.info(this, "Notification bhej di. Sabke dashboard ke neeche 5 second mein dikh jayegi.");
            } catch (SQLException ex) {
                UIUtil.error(this, ex);
            }
        });
        delete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                UIUtil.info(this, "Pehle ek notification select karo.");
                return;
            }
            try {
                dao.delete((int) model.getValueAt(row, 0));
                refresh();
            } catch (SQLException ex) {
                UIUtil.error(this, ex);
            }
        });
        refresh();
    }

    @Override
    public void refresh() {
        try {
            model.setRowCount(0);
            for (Notification n : dao.getAll())
                model.addRow(new Object[]{n.getId(), n.getMessage(), n.getCreatedAt()});
        } catch (SQLException e) {
            UIUtil.error(this, e);
        }
    }
}
