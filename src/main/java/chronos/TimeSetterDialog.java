/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chronos;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author Project2100
 */
public class TimeSetterDialog extends JDialog {

    JCheckBox timerSetterCheckBox;
    SpinnerNumberModel minModel, secModel;

    public TimeSetterDialog(Chronos parent) {

        super(parent.mainFrame, "Set timer - Tachyon", true);

        minModel = new SpinnerNumberModel(10, 0, 59, 1);
        secModel = new SpinnerNumberModel(0, 0, 59, 1);

        JLabel timerSetterLabel = new JLabel("Set timer:");

        JSpinner minSpinner = new JSpinner(minModel);
        JSpinner secSpinner = new JSpinner(secModel);

        JLabel colon = new JLabel(Main.TIME_UNIT_SEPARATOR);

        timerSetterCheckBox = new JCheckBox("Set as new segment", false);

        JButton okButton = new JButton("OK");
        okButton.addActionListener((event) -> {
            int min = minModel.getNumber().intValue();
            int sec = secModel.getNumber().intValue();
            if (min == 0 && sec <= 10)
                JOptionPane.showMessageDialog(
                        parent.mainFrame,
                        "Input is too short!",
                        "Set timer - Tachyon",
                        JOptionPane.WARNING_MESSAGE);
            else {
                setVisible(false);
                parent.setLimit(
                        Main.getTimeSignature(min, sec),
                        timerSetterCheckBox.isSelected());

                //Dialog cleanup
                minModel.setValue(10);
                secModel.setValue(0);
                timerSetterCheckBox.setSelected(false);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener((evt) -> setVisible(false));

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(timerSetterLabel)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(minSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(colon)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(secSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                .addComponent(timerSetterCheckBox))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)))
                .addContainerGap()
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(timerSetterLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(minSpinner)
                        .addComponent(colon)
                        .addComponent(secSpinner)
                        .addComponent(timerSetterCheckBox))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup()
                        .addComponent(okButton)
                        .addComponent(cancelButton))
                .addContainerGap()
        );
        setLocationRelativeTo(parent.mainFrame);
        setResizable(false);
        pack();
    }
}
