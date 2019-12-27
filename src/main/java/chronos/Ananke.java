/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chronos;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.text.JTextComponent;
import static chronos.Ananke.State.*;

/**
 *
 * @author Project2100
 */
public class Ananke {

    enum State {
        //Other symbols: ▲►✖✔
        TICKING("●", Color.red),
        FROZEN("■", Color.yellow),
        WASTING("◆", Color.blue);

        private final String symbol;
        private final Color color;

        private State(String s, Color c) {
            symbol = s;
            color = c;
        }
    }

    private final Set<JTextComponent> displays;
    private final Set<JLabel> stateLabels;
    JTextField overScreen;

    private volatile long time;
    private long startingPoint, timeLeft;
    String currentTime;
    State currentState;

    Timer mainTimer, overTimer, blinker;

    Ananke(String t) {

        currentState = WASTING;
        currentTime = t;

        displays = new CopyOnWriteArraySet<>();
        stateLabels = new CopyOnWriteArraySet<>();
        overScreen = new JTextField(Main.TIME_NULL);

        mainTimer = new Timer(100, (tick) -> {
            
            // Syscall (theoretically safer) vs tick.getWhen()?
            currentTime = Main.getTimeSignature(time
                    = timeLeft - System.currentTimeMillis() + startingPoint);
            
            // Average delta of 0.3ms - no delay experimented if first instruction
//            System.out.println(tick.getWhen()-System.currentTimeMillis());
            
            displays.forEach((field) -> field.setText(currentTime));

            switch ((int) time / 100) {
                case 1799:
                case 1798:
                    displays.forEach((field) -> field.setForeground(Color.green));
                    break;
                case 1199:
                case 1198:
                    displays.forEach((field) -> field.setForeground(Color.yellow));
                    break;
                case 599:
                case 598:
                    displays.forEach((field) -> field.setForeground(Color.red));
                    break;
                case 99:
                case 98:
                    if (!blinker.isRunning()) blinker.start();
                    break;
                case 0:
                    mainTimer.stop();
                    displays.forEach((field) -> field.setText(Main.TIME_ZERO));
                    startingPoint += timeLeft;
                    overTimer.start();
                    overScreen.setForeground(Color.red);
            }

        });
        mainTimer.setInitialDelay(0);

        blinker = new Timer(500, new ActionListener() {
            // TODO need color reset
            boolean isBright = true;

            @Override
            public void actionPerformed(ActionEvent tick) {
                isBright = !isBright;
                displays.forEach((screen) -> {
                    screen.setForeground((isBright)
                            ? Color.red
                            : Color.black);
                });
            }
        });
        //TODO Set initial delay? see color reset feature

        overTimer = new Timer(100, (tick) -> overScreen.setText(
                Main.getTimeSignature(time = System.currentTimeMillis() - startingPoint)));
        overTimer.setInitialDelay(0);
    }

    private void changeStateTo(final State s) {
        currentState = s;
        stateLabels.forEach((label) -> {
            label.setText(s.symbol);
            label.setForeground(s.color);
        });
    }

    void set(String time) {
        if (currentState == TICKING)
            throw new IllegalStateException("Time is ticking, can't set new limit");

        displays.forEach((field) -> field.setText(time));
    }

    void go() {
        timeLeft = Main.getMilliseconds(currentTime);
        startingPoint = System.currentTimeMillis();
        mainTimer.restart();
        
        changeStateTo(TICKING);
    }

    void rewind(String time) {
        blinker.stop();
        mainTimer.stop();
        overTimer.stop();
        
        changeStateTo(WASTING);
        
        displays.forEach((field) -> {
            field.setText(time);
            field.setForeground(Color.white);
        });
        currentTime = time;
        overScreen.setText(Main.TIME_NULL);
        overScreen.setForeground(Color.white);

    }

    void stop() {
        if (mainTimer.isRunning()) mainTimer.stop();
        else overTimer.stop();

        changeStateTo(FROZEN);
    }

    boolean isTicking() {
        return mainTimer.isRunning();
    }

    JTextField generateMainDisplay() {
        JTextField f = new JTextField(currentTime);
        f.setEditable(false);
        displays.add(f);
        return f;
    }

    JLabel generateStateLabel() {
        JLabel l = new JLabel(currentState.symbol);
        l.setForeground(currentState.color);
        stateLabels.add(l);
        return l;
    }

    JTextField getOverDisplay() {
        return overScreen;
    }
}
