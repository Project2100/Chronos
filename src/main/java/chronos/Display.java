/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chronos;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsDevice;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JWindow;

/**
 *
 * @author Project2100
 */
public final class Display {

    private final JWindow window;
    private final JTextField mainDisplay;
    private final JLabel statusSymbol;
    private final JTextArea echoArea;

    public Display(GraphicsDevice secondaryMonitor, String defaultLimit) {

        mainDisplay = Main.timer().generateMainDisplay();
        mainDisplay.setFont(new Font("Arial", Font.PLAIN, secondaryMonitor.getDisplayMode().getWidth() * 8 / 32));
        mainDisplay.setForeground(Color.white);
        mainDisplay.setBorder(null);
        mainDisplay.setForeground(Color.white);
        mainDisplay.setBackground(Color.black);
        mainDisplay.setSelectedTextColor(Color.black);
        mainDisplay.setSelectionColor(Color.white);

        statusSymbol = Main.timer().generateStateLabel();
        statusSymbol.setFont(new Font("Courier", Font.PLAIN, secondaryMonitor.getDisplayMode().getWidth() * 3 / 38));
        
        echoArea = new JTextArea(Main.gui().echoDocument);
        echoArea.setForeground(Color.white);
        echoArea.setBackground(Color.black);
        echoArea.setFont(new Font("Arial", Font.PLAIN, secondaryMonitor.getDisplayMode().getWidth() * 3 / 27));
        echoArea.setLineWrap(true);

        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);

        JPanel bigPanel = new JPanel();
        bigPanel.setOpaque(false);
        
        GroupLayout layout = new GroupLayout(bigPanel);
        bigPanel.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(mainDisplay)
                        .addGap(30)
                        .addComponent(statusSymbol).addContainerGap(secondaryMonitor.getDisplayMode().getWidth() / 27, secondaryMonitor.getDisplayMode().getWidth() / 27))
                .addComponent(separator)
                .addComponent(echoArea));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(mainDisplay)
                        .addComponent(statusSymbol))
                .addComponent(separator)
                .addComponent(echoArea));

        window = new JWindow(secondaryMonitor.getDefaultConfiguration());
        window.setBackground(Color.black);
        window.getContentPane().setBackground(Color.black);
        window.getContentPane().add(bigPanel);
        window.pack();
        window.setFocusable(false);

        secondaryMonitor.setFullScreenWindow(window);
    }

}
