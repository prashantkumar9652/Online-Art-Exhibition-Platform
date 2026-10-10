import ui.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Swing UI hamesha Event Dispatch Thread par chalana chahiye
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
