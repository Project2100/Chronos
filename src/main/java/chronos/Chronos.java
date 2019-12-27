/*
 * Branch - TachyonLite (21/10/2010 1954)
 *
 * Created on 19-mag-2010, 15.46.50
 */
package chronos;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JPopupMenu.Separator;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Document;
import swing2100.MarkedCollectionModel;

//TODO !CONCEPT: percentage margin option [timerScreen]
//TODO !CONCEPT: editable warning limits (redo main timer) [warningsItem]
//TODO write segment/Locale to settings
//TODO cut-add
//TODO quick command mode
/**
 * Total revamp: Chronos rises again, with Ananke 160604 1826
 *
 * @author Project2100
 */
public final class Chronos {

    private void notImplemented() {
        JOptionPane.showMessageDialog(mainFrame, "This feature is not yet implemented", "Tachyon", JOptionPane.INFORMATION_MESSAGE);
    }

    final AbstractAction startAction, resetAction, displayTimeSetterAction, getMarkerAction,
            dropMarkerAction, setAsLimitAction, clearMarkersAction, newProfileAction,
            editProfileAction, deleteProfileAction, exportProfilesAction, nextSegmentAction;
    final JFrame mainFrame;

    private final JFileChooser exportProfilesDialog;

    final JTextField mainDisplay, segmentScreen, limitScreen;
    final JLabel status;
    final JMenu profilesMenu;
    final Document echoDocument;
    private final JTable markerTable;
    private final DefaultTableModel markerModel;
    private final JLabel profileName;
    private final MarkedCollectionModel<String, ArrayList<String>> profileSegments;

    
    Chronos(boolean restore, String limit, String curSeg, String curProf) {
        GroupLayout layout;

        exportProfilesDialog = new JFileChooser();
        exportProfilesDialog.setDialogTitle("Export profiles");
        exportProfilesDialog.setFileFilter(new FileNameExtensionFilter("XML file (*.xml)", "xml"));
        exportProfilesDialog.setMultiSelectionEnabled(false);
        exportProfilesDialog.setAcceptAllFileFilterUsed(false);
        exportProfilesDialog.setApproveButtonText("Export");

        // <editor-fold defaultstate="collapsed" desc="Actions">
        // Start
        startAction = new AbstractAction("Start") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getValue(NAME).equals("Stop")) {
                    if (Main.timer().isTicking())
                        putValue(NAME, "Resume");
                    else
                        setEnabled(false);
                    Main.timer().stop();
                }
                else {
                    profilesMenu.setEnabled(false);
                    putValue(NAME, "Stop");
                    resetAction.setEnabled(true);
                    nextSegmentAction.setEnabled(false);
                    displayTimeSetterAction.setEnabled(false);
                    Main.timer().go();
                }
            }
        };
        startAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));

        // Reset
        (resetAction = new AbstractAction("Reset") {
            @Override
            public void actionPerformed(ActionEvent evt) {
                setEnabled(false);
                profilesMenu.setEnabled(true);
                displayTimeSetterAction.setEnabled(true);
                nextSegmentAction.setEnabled(profileSegments.getMark() < profileSegments.size() - 1);
                startAction.putValue(NAME, "Start");
                startAction.setEnabled(true);

                Main.timer().rewind(limitScreen.getText());
            }
        }).setEnabled(false);
        resetAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));

        // Set limit
        displayTimeSetterAction = new AbstractAction("Set limit...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TimeSetterDialog(Main.gui()).setVisible(true);
            }
        };
        displayTimeSetterAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));

        // Get marker
        getMarkerAction = new AbstractAction("Get marker") {
            @Override
            public void actionPerformed(ActionEvent e) {
                markerModel.addRow(new Object[] {mainDisplay.getText(), ""});
            }
        };
        getMarkerAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_MASK));

        // Drop marker
        dropMarkerAction = new AbstractAction("Drop marker") {
            @Override
            public void actionPerformed(ActionEvent e) {
                markerModel.removeRow(markerTable.getSelectedRow());
            }
        };
        dropMarkerAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));

        // Set as limit
        setAsLimitAction = new AbstractAction("Set as limit") {
            @Override
            public void actionPerformed(ActionEvent e) {

                setLimit((String) markerModel.getValueAt(markerTable.getSelectedRow(), 0), false);
                resetAction.actionPerformed(null);
            }
        };

        // Clear markers
        clearMarkersAction = new AbstractAction("Clear markers") {
            @Override
            public void actionPerformed(ActionEvent e) {
                markerModel.setRowCount(0);
            }
        };
        clearMarkersAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));

        // New profile
        newProfileAction = new AbstractAction("New profile...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.profileManager().createNewProfile();
            }
        };
        newProfileAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_N,
                        InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));

        // Edit profile
        (editProfileAction = new AbstractAction("Edit profile...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.profileManager().editCurrentProfile();
            }
        }).setEnabled(false);
        editProfileAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_E,
                        InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));

        // Delete profile
        (deleteProfileAction = new AbstractAction("Delete profile") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(mainFrame, "Are you sure you want to delete the current profile?", "Tachyon", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                    profilesMenu.remove(Main.profileManager().currentProfile);
                    Main.profileManager().deleteCurrentProfile();
                    profileName.setText("");
                    profileSegments.clear();
                    editProfileAction.setEnabled(false);
                    deleteProfileAction.setEnabled(false);
                    nextSegmentAction.setEnabled(false);
                }
            }
        }).setEnabled(false);
        deleteProfileAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));

        // Export profiles
        //TODO Observe the profiles list
        (exportProfilesAction = new AbstractAction("Export profiles...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.logger.log(Level.FINE, "Initiating profile export");
                if (exportProfilesDialog.showDialog(mainFrame, null) == JFileChooser.APPROVE_OPTION)
                    try {
                        //TODO DO export on the selected file
                        Main.PROFILES_NODE.exportSubtree(System.out);
                    }
                    catch (IOException | BackingStoreException ex) {
                        Main.logger.log(Level.SEVERE, "Failed exporting profiles", ex);
                    }
            }
        }).setEnabled(Main.profileManager().hasProfiles());
        exportProfilesAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));

        // Next segment
        (nextSegmentAction = new AbstractAction("Next segment") {
            
            /**
             * document
             * 
             * @param e 
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                
                int idx = profileSegments.incrementMark(1);
                
                
                setLimit(Main.profileManager().currentProfile.segments.get(idx), true);
                nextSegmentAction.setEnabled(profileSegments.getMark() < profileSegments.size() - 1);

                clearMarkersAction.actionPerformed(null);

            }
        }).setEnabled(false);
        nextSegmentAction.putValue(AbstractAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_E,
                        InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Time keeper">
        markerTable = new JTable(markerModel = new DefaultTableModel(
                new String[] {"Marker", "Description"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        });
        markerTable.getSelectionModel().addListSelectionListener((event) -> {
            if (!event.getValueIsAdjusting()) {
                boolean selection = !markerTable.getSelectionModel().isSelectionEmpty();
                setAsLimitAction.setEnabled(selection);
                dropMarkerAction.setEnabled(selection);
            }
        });
        markerTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane markerTableSP = new JScrollPane(markerTable);

        JPopupMenu markerMenu = new JPopupMenu();
        markerMenu.add(setAsLimitAction);
        markerMenu.add(dropMarkerAction);
        markerTable.setComponentPopupMenu(markerMenu);

        profileName = new JLabel(curProf);
        //XXX ?! Can't refer to supplier constructor
        profileSegments = new MarkedCollectionModel<>((Supplier) ArrayList::new);
        profileSegments.setMarkColor(Color.orange);
        
        JList<String> segments = new JList<>(profileSegments);
        profileSegments.applyDecorator(segments);
        segments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JLabel profileLabel = new JLabel("Profile:");
        JButton nextButton = new JButton(nextSegmentAction);
        JScrollPane segSP = new JScrollPane(segments);

        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(layout = new GroupLayout(profilePanel));
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(profileLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(profileName)
                        .addPreferredGap(ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(true, nextButton))
                .addComponent(segSP));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createBaselineGroup(true, true)
                        .addComponent(profileLabel)
                        .addComponent(profileName)
                        .addComponent(nextButton))
                .addComponent(segSP));
        // </editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Echo">
        JTextArea echoArea = new JTextArea("", 5, 20);
        echoArea.setFont(new java.awt.Font("Arial", 0, 48));
        echoArea.setLineWrap(true);

        echoDocument = echoArea.getDocument();

        JButton echoBlinkButton = new JButton("Blink");
        Timer blinkTimer = new Timer(200, (ActionEvent evt) -> {
            echoBlinkButton.setEnabled(false);
            echoArea.setForeground(echoArea.getForeground().equals(Color.black)
                    ? Color.white
                    : Color.black);
            System.out.println(evt.getSource());
        }) {
            byte blinkCount = 8;

            @Override
            protected void fireActionPerformed(ActionEvent e) {
                super.fireActionPerformed(e);
                if (--blinkCount == 0) {
                    stop();
                    blinkCount = 8;
                    echoBlinkButton.setEnabled(true);
                }
            }
        };
        echoBlinkButton.addActionListener((event) -> blinkTimer.start());

        JButton echoClearButton = new JButton("Clear");
        echoClearButton.addActionListener((event) -> echoArea.setText(""));

        JScrollPane echoScrollPane = new JScrollPane(echoArea);

        JPanel echoPanel = new JPanel();

        echoPanel.setLayout(layout = new GroupLayout(echoPanel));
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup()
                        .addComponent(echoScrollPane, GroupLayout.DEFAULT_SIZE, 758, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(echoBlinkButton, GroupLayout.PREFERRED_SIZE, 380, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(echoClearButton, GroupLayout.PREFERRED_SIZE, 380, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap());
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(echoScrollPane, GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup()
                        .addComponent(echoBlinkButton)
                        .addComponent(echoClearButton))
                .addContainerGap());
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Menus">
        JMenu actionsMenu = new JMenu("Actions");
        actionsMenu.add(startAction);
        actionsMenu.add(resetAction);
        actionsMenu.add(new Separator());
        actionsMenu.add(getMarkerAction);
        actionsMenu.add(clearMarkersAction);
        actionsMenu.add(nextSegmentAction);
        actionsMenu.add(new Separator());
        actionsMenu.add(displayTimeSetterAction);

        profilesMenu = new JMenu("Profiles");
        profilesMenu.add(newProfileAction);
        profilesMenu.add(editProfileAction);
        profilesMenu.add(deleteProfileAction);
        if (Main.profileManager().hasProfiles())
            profilesMenu.add(new Separator());
        Main.profileManager().profiles.forEach(this::addProfile);

        JMenuItem warningsItem = new JMenuItem("Set warning times...");
        warningsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
        warningsItem.addActionListener((event) -> notImplemented());

        JMenu settingsMenu = new JMenu("Settings");
        settingsMenu.add(warningsItem);
        settingsMenu.add(exportProfilesAction);
        settingsMenu.add(new JCheckBoxMenuItem("Keep session on close", null, restore) {
            @Override
            public void setSelected(boolean b) {
                super.setSelected(b);
                Main.SETTINGS.putBoolean("restore_last_session", b);
            }
        });

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(actionsMenu);
        menuBar.add(profilesMenu);
        menuBar.add(settingsMenu);
        //</editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Form">
        mainDisplay = Main.timer().generateMainDisplay();
        mainDisplay.setFont(new java.awt.Font("Tahoma", 0, 224));
        mainDisplay.setForeground(Color.white);
        mainDisplay.setBackground(Color.black);
        mainDisplay.setSelectedTextColor(Color.black);
        mainDisplay.setSelectionColor(Color.white);

        status = Main.timer().generateStateLabel();
        status.setBackground(Color.BLACK);
        status.setOpaque(true);

        JTextField overScreen = Main.timer().getOverDisplay();
        overScreen.setForeground(Color.white);
        overScreen.setSelectedTextColor(Color.black);
        overScreen.setBackground(Color.black);
        overScreen.setSelectionColor(Color.white);
        JLabel overtimeLabel = new JLabel("Overtime:");
        overtimeLabel.setLabelFor(overScreen);

        segmentScreen = new JTextField(curSeg);
        segmentScreen.setEditable(false);
        segmentScreen.setForeground(Color.white);
        segmentScreen.setSelectedTextColor(Color.black);
        segmentScreen.setBackground(Color.black);
        segmentScreen.setSelectionColor(Color.white);
        JLabel segmentScreenLabel = new JLabel("Segment length:");
        overtimeLabel.setLabelFor(segmentScreen);

        limitScreen = new JTextField(limit);
        limitScreen.setEditable(false);
        limitScreen.setForeground(Color.white);
        limitScreen.setSelectedTextColor(Color.black);
        limitScreen.setBackground(Color.black);
        limitScreen.setSelectionColor(Color.white);
        JLabel limitLabel = new JLabel("Current limit:");
        overtimeLabel.setLabelFor(limitScreen);

        JTabbedPane dataTabs = new JTabbedPane();
        dataTabs.addTab("Time", new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, markerTableSP, profilePanel));
        dataTabs.addTab("Echo", echoPanel);

        JToolBar controls = new JToolBar();
        controls.add(startAction);
        controls.add(resetAction);
        controls.add(new JToolBar.Separator());
        controls.add(getMarkerAction);
        controls.add(setAsLimitAction);
        controls.add(dropMarkerAction);
        controls.add(clearMarkersAction);
        controls.setFloatable(false);
        controls.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        mainFrame = new JFrame("Chronos");
        mainFrame.setJMenuBar(menuBar);
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.setName("ChronosFrame");
        mainFrame.setResizable(false);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                if (!Main.timer().isTicking() || JOptionPane.showConfirmDialog(
                        mainFrame,
                        "Timer is running\n Are you sure you want to quit?",
                        "Chronos",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION)
                    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        });

        mainFrame.getContentPane().setLayout(layout = new GroupLayout(mainFrame.getContentPane()));
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                        .addComponent(controls)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(status)
                        .addContainerGap())
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup()
                                .addComponent(dataTabs, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 783, Short.MAX_VALUE)
                                .addComponent(mainDisplay, GroupLayout.DEFAULT_SIZE, 783, Short.MAX_VALUE)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(limitLabel)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(limitScreen, GroupLayout.PREFERRED_SIZE, 53, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(segmentScreenLabel)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(segmentScreen, GroupLayout.PREFERRED_SIZE, 53, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(overtimeLabel)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(overScreen, GroupLayout.PREFERRED_SIZE, 53, GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap()));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(controls)
                        .addComponent(status))
                .addComponent(mainDisplay, GroupLayout.PREFERRED_SIZE, 230, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(overScreen, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(segmentScreen, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(limitScreen, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(overtimeLabel)
                        .addComponent(segmentScreenLabel)
                        .addComponent(limitLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(dataTabs, GroupLayout.PREFERRED_SIZE, 307, GroupLayout.PREFERRED_SIZE)
                .addContainerGap());

        mainFrame.pack();
        // </editor-fold>
    }

    void setLimit(String limit, boolean asSegment) {
        Main.timer().set(limit);
        limitScreen.setText(limit);
        if (asSegment)
            segmentScreen.setText(limit);
    }

    void setProfile(ActionEvent evt) {
        // Update current profile reference
        Profile profile = (Profile) evt.getSource();
        Main.profileManager().currentProfile = profile;

        // Update profile subpanel
        profileName.setText(profile.getText());
        profileSegments.clear();
        profile.segments.stream().forEach(profileSegments::append);
        nextSegmentAction.setEnabled(profileSegments.getMark() < profileSegments.size() - 1);

        // Update limit and actions
        setLimit(profile.segments.get(0), true);
        clearMarkersAction.actionPerformed(null);
        editProfileAction.setEnabled(true);
        deleteProfileAction.setEnabled(true);

        Main.SETTINGS.put("current_profile", profile.getText());
    }

    final void addProfile(Profile profile) {
        profile.addActionListener(this::setProfile);
        profilesMenu.add(profile);
    }
}
