/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chronos;

import java.awt.EventQueue;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Project2100
 */
public class Main {

    //<editor-fold defaultstate="collapsed" desc="Logging and exception handling">
    /**
     * Application logger, central point for exception reporting and general
     * debugging
     */
    public static final Logger logger = Logger.getLogger("Tachyon");

    // This lambda will be used as the default handler for uncaught exceptions
    // in several threads
    private static final Thread.UncaughtExceptionHandler exHandler = (thread, exception) -> {
        Main.logger.log(Level.SEVERE, "Uncaught exception in thread: " + thread.getName(), exception);
        System.exit(1);
    };

    static {
        // This thread should be "main"
        Thread.currentThread().setUncaughtExceptionHandler(exHandler);

        logger.setLevel(Level.FINE);
        StreamHandler h = new StreamHandler(System.out, new Formatter() {
            @Override
            public String format(LogRecord record) {
                return String.format("%1$tY-%1$tm-%1$td  %1$tH:%1$tM:%1$tS - ", record.getMillis())
                        + record.getLevel() + ": "
                        + formatMessage(record) + System.lineSeparator();
            }
        }) {
            @Override
            public synchronized void publish(LogRecord record) {
                super.publish(record);
                flush();
            }

        };
        h.setLevel(Level.FINEST);
        h.setFilter((record) -> record.getLevel() != Level.SEVERE);

        logger.addHandler(h);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Time constructs">
    /**
     * The separator character used in formatting a time signature
     */
    public static final String TIME_UNIT_SEPARATOR = ":";
    /**
     * The time signature representing an undefined value
     */
    public static final String TIME_NULL = "--" + TIME_UNIT_SEPARATOR + "--" + TIME_UNIT_SEPARATOR + "-";
    /**
     * The time signature representing a zero value
     */
    public static final String TIME_ZERO = "00" + TIME_UNIT_SEPARATOR + "00" + TIME_UNIT_SEPARATOR + "0";
    /**
     * The time signature's regular expression
     */
    public static final String TIME_PATTERN = "[0-5]\\d" + TIME_UNIT_SEPARATOR + "[0-5]\\d" + TIME_UNIT_SEPARATOR + "\\d";

    /**
     * Converts an amount of milliseconds to a time signature
     *
     * @param time amount of time expressed in milliseconds
     * @return the corresponding time signature
     */
    public static final String getTimeSignature(long time) {
        return String.format("%1$tM" + TIME_UNIT_SEPARATOR + "%1$tS" + TIME_UNIT_SEPARATOR + "%2$d", time, time % 1000 / 100);
    }

    /**
     * Converts two values to minutes and seconds of a time signature
     *
     * @param min amount of minutes
     * @param sec amount of seconds
     * @return the resulting time signature
     * @throws IllegalArgumentException when either {@code min} or {@code sec}
     * are negative or greater than {@code 60}
     */
    public static final String getTimeSignature(int min, int sec) {
        if (min < 0 || min >= 60)
            throw new IllegalArgumentException("Invalid minutes value: " + min);
        if (sec < 0 || sec >= 60)
            throw new IllegalArgumentException("Invalid minutes value: " + sec);
        return String.format("%02d" + TIME_UNIT_SEPARATOR + "%02d" + TIME_UNIT_SEPARATOR + "0", min, sec);
    }

    /**
     * Converts a time signature to the corresponding amount of milliseconds
     * 
     * @param target a time signature 
     * @return the amount of milliseconds
     */
    // TODO CHECK CORRECTNESS!!!
    public static final long getMilliseconds(String target) {
        if (Pattern.matches(TIME_PATTERN, target))
            return 60000 * Long.parseLong(target.substring(0, 2))
                    + 100 * Long.parseLong(target.substring(3, 5) + target.substring(6, 7));
        throw new IllegalArgumentException("Not a valid time signature: " + target);
    }
    //</editor-fold>

    // Preferences nodes
    static final Preferences SETTINGS = Preferences.userNodeForPackage(Main.class),
            PROFILES_NODE = SETTINGS.node("profiles");
//    static final String ERROR_MESSAGE
//            = "Invalid value retrieved from property \"{0}\": {1}";

    // Central fields and relative getters
    private static Ananke core;
    private static Chronos gui;
    private static ProfileManager profileManager;
    private static GraphicsDevice primaryMonitor, secondaryMonitor;

    public static Ananke timer() {
        return core;
    }

    public static Chronos gui() {
        return gui;
    }

    public static ProfileManager profileManager() {
        return profileManager;
    }

    public static GraphicsDevice getSecondaryMonitor() {
        return secondaryMonitor;
    }

    /**
     * Starting point
     *
     * @param args no arguments are read
     */
    public static void main(String args[]) {

        // Setting Look & Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
            Main.logger.log(Level.WARNING, "Could not set system look&feel - applying default");
        }

        primaryMonitor = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        // Detecting secondary screens
        List<GraphicsDevice> secondaryMonitors
                = Arrays.stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices())
                .filter((device) ->
                        device.getType() == GraphicsDevice.TYPE_RASTER_SCREEN
                        && device.isFullScreenSupported()
                        && !device.equals(primaryMonitor))
                .collect(Collectors.toList());
        if (!secondaryMonitors.isEmpty())
            secondaryMonitor = secondaryMonitors.get(0);

        // Retrieving application settings
        final String limit, curSeg, curProf;
        final boolean restore;
        if (restore = SETTINGS.getBoolean("restore_last_session", false)) {
            limit = Main.SETTINGS.get("last_limit", "10" + TIME_UNIT_SEPARATOR + "00" + TIME_UNIT_SEPARATOR + "0");
            curProf = Main.SETTINGS.get("current_profile", "");
            curSeg = Main.SETTINGS.get("current_segment", TIME_NULL);
            // markers
            // echo text (?)
        }
        else {
            limit = "10" + TIME_UNIT_SEPARATOR + "00" + TIME_UNIT_SEPARATOR + "0";
            curSeg = TIME_NULL;
            curProf = "";
        }

        // Interface construction
        EventQueue.invokeLater(() -> {
            Thread.currentThread().setUncaughtExceptionHandler(exHandler);
            Main.logger.log(Level.FINE, "EDT awake");

            core = new Ananke(limit);
            profileManager = new ProfileManager(curProf);
            gui = new Chronos(restore, limit, curSeg, curProf);

            gui.mainFrame.setVisible(true);

            if (secondaryMonitor != null) {
                Display d = new Display(secondaryMonitor, limit);

            }

        });
        Main.logger.log(Level.FINE, "Terminating main thread");
    }
}
