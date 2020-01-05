/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chronos;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import project2100.commons.swing.ArrayListModel;

/**
 *
 * @author Project2100
 */
public final class ProfileManager {

    private static final String NEW_PROFILE_TITLE = "New profile - Chronos";
    final SortedSet<Profile> profiles;
    Profile currentProfile;

    private JDialog dialog;
    private final JTextField nameField;
    private final ArrayListModel<String> segmentsModel;
    private final SpinnerNumberModel minModel, secModel;

    ProfileManager(String currentProfileName) {

        // Loading profiles
        currentProfile = null;
        profiles = new TreeSet<>();
        try {
            for (String profileName : Main.PROFILES_NODE.childrenNames()) {
                Preferences profileNode = Main.PROFILES_NODE.node(profileName);
                try {
                    Profile profile = new Profile(profileNode);
                    profiles.add(profile);
                    if (profile.getText().equals(currentProfileName))
                        currentProfile = profile;
                    Main.logger.log(Level.FINER, "Profile {0} loaded.", profile.getText());
                }
                catch (BackingStoreException ex) {
                    Main.logger.log(Level.INFO, "Profile " + profileNode.name() + " is corrupted", ex);
                }
            }
        }
        catch (BackingStoreException ex) {
            Main.logger.log(Level.SEVERE, "Cannot access profiles", ex);
        }

        nameField = new JTextField("");
        segmentsModel = new ArrayListModel<>();

        minModel = new SpinnerNumberModel(0, 0, 59, 1);
        secModel = new SpinnerNumberModel(0, 0, 59, 1);

    }

    private void constructDialog() {
        dialog = new JDialog(Main.gui().mainFrame, true);
        JList<String> segmentList = new JList<>(segmentsModel);

        JButton profileOKButton = new JButton("OK");
        profileOKButton.addActionListener((evt) -> {

            String newName = nameField.getText();
            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Missing name!", "Tachyon", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (segmentsModel.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "No segments given!", "Tachyon", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Must dispose of "old" profile if editing 
            if (!dialog.getTitle().equals(NEW_PROFILE_TITLE)) {
                Main.gui().profilesMenu.remove(currentProfile);
                deleteCurrentProfile();
            }

            Profile newProfile = new Profile(newName,
                    segmentsModel.stream().collect(Collectors.toList()));

            Main.gui().addProfile(newProfile);

            //TODO A bit of a hack (an excessive one too)...
            newProfile.doClick();
//          parent.setProfile(new ActionEvent(currentProfile, ActionEvent.ACTION_PERFORMED, currentName, System.currentTimeMillis(), LEFT_MOUSE_BUTTON_ACT_PERF_CODE));

            dialog.setVisible(false);
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener((evt) -> dialog.setVisible(false));

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener((evt) -> segmentsModel.clear());

        JSpinner minSpinner = new JSpinner(minModel);
        JSpinner secSpinner = new JSpinner(secModel);

        JButton addSegmentButton = new JButton("+");
        addSegmentButton.addActionListener((event) -> {
            int min = minModel.getNumber().intValue();
            int sec = secModel.getNumber().intValue();
            //TODO Optimize
            segmentsModel.add(0, Main.getTimeSignature((min * 60 + sec) * 1000));
        });

        JScrollPane segListSP = new JScrollPane(segmentList);

        JLabel nameLabel = new JLabel("Name:"),
                segmentsLabel = new JLabel("Segments:"),
                colon2 = new JLabel(Main.TIME_UNIT_SEPARATOR),
                segInputLabel = new JLabel("New segment:");

        dialog.setResizable(false);

        GroupLayout layout = new GroupLayout(dialog.getContentPane());
        dialog.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(nameLabel)
                                        .addComponent(segmentsLabel))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup()
                                        .addComponent(nameField)
                                        .addComponent(segListSP)))
                        .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(segInputLabel)
                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                .addComponent(minSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(colon2)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(secSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(addSegmentButton))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(profileOKButton, GroupLayout.PREFERRED_SIZE, 152, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(clearButton, GroupLayout.PREFERRED_SIZE, 141, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(cancelButton, GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)))
                .addContainerGap());
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(nameLabel)
                                .addComponent(nameField))
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(colon2)
                                .addComponent(segInputLabel)
                                .addComponent(minSpinner)
                                .addComponent(addSegmentButton)
                                .addComponent(secSpinner))
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(segmentsLabel)
                                .addComponent(segListSP))
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(profileOKButton)
                                .addComponent(cancelButton)
                                .addComponent(clearButton))
                        .addContainerGap());

        dialog.setLocationRelativeTo(Main.gui().mainFrame);
        dialog.pack();
    }

    private void show(Profile p) {
        if (dialog == null) constructDialog();

        segmentsModel.clear();
        minModel.setValue(0);
        secModel.setValue(0);

        if (p == null) {
            dialog.setTitle(NEW_PROFILE_TITLE);
            nameField.setText("");
        }
        else {
            dialog.setTitle("Editing " + p.getText() + " - Chronos");
            nameField.setText(p.getText());
            p.segments.forEach(segmentsModel::append);
        }

        dialog.setVisible(true);
    }

    void createNewProfile() {
        show(null);
    }

    void editCurrentProfile() {
        show(currentProfile);
    }

    void deleteProfile(Profile p) {
        profiles.remove(p);
        p.dispose();
        if (p.equals(currentProfile))
            currentProfile = null;
    }

    void deleteCurrentProfile() {
        deleteProfile(currentProfile);
    }

    boolean hasProfiles() {
        return !profiles.isEmpty();
    }

}
