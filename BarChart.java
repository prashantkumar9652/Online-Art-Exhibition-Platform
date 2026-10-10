package ui;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

// Simple bar chart (Graphics2D se khud draw kiya, koi external library nahi)
public class BarChart extends JPanel {
    private static final Color[] COLORS = {
            new Color(66, 133, 244), new Color(52, 168, 83), new Color(251, 188, 5),
            new Color(234, 67, 53), new Color(154, 88, 210), new Color(0, 172, 193)};
    private final String title;
    private final Map<String, Number> data;

    public BarChart(String title, Map<String, Number> data) {
        this.title = title;
        this.data = data;
        setPreferredSize(new Dimension(400, 250));
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString(title, 12, 22);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));

        if (data.isEmpty()) {
            g.drawString("No data yet", 12, 50);
            return;
        }
        double max = data.values().stream().mapToDouble(Number::doubleValue).max().orElse(1);
        if (max == 0) max = 1;

        int w = getWidth(), h = getHeight();
        int left = 15, top = 45, bottom = h - 30;
        int slot = (w - left - 10) / data.size();
        int barW = Math.max(12, slot - 16);
        g.drawLine(left, bottom, w - 10, bottom);

        int i = 0;
        for (Map.Entry<String, Number> e : data.entrySet()) {
            double v = e.getValue().doubleValue();
            int barH = (int) ((bottom - top) * (v / max));
            int x = left + i * slot + 8;
            g.setColor(COLORS[i % COLORS.length]);
            g.fillRect(x, bottom - barH, barW, barH);
            g.setColor(Color.BLACK);
            String val = (v == Math.floor(v)) ? String.valueOf((long) v) : String.format("%.0f", v);
            g.drawString(val, x, bottom - barH - 4);
            String k = e.getKey();
            if (k.length() > 10) k = k.substring(0, 8) + "..";
            g.drawString(k, x - 2, bottom + 15);
            i++;
        }
    }
}
