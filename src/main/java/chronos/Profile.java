/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chronos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.swing.JMenuItem;

/**
 * A profile in Tachyon is a sequence of time durations, or "segments",
 * identified by a name for expediting time-keeping sessions.
 *
 * @implnote This class implements the Comparable interface by using
 * lexicographical order on their names<p/>
 *
 * This class implements the Iterable interface by delegating to its ArrayList
 * of time segments
 *
 * @author Project2100
 */
class Profile extends JMenuItem implements Comparable<Profile>, Iterable<String> {

    static final Profile VOID = new Profile() {
        @Override
        public void dispose() {
        }

        // This object is defined as the "zero" of its class
        @Override
        public int compareTo(Profile other) {
            if (this == other) return 0;
            return -1;
        }
    };

    final ArrayList<String> segments;
    private final Preferences backingNode;

    private Profile() {
        super("<No profile>");
        segments = new ArrayList<>(0);
        backingNode = null;
    }

    /**
     * Dummy constructor, used primarily for testing
     *
     * @param name the profile name
     * @param segments an array of segments
     */
    Profile(String name, List<String> segments) {
        super(name);
        this.segments = new ArrayList<>(segments);
        backingNode = Main.PROFILES_NODE.node(name);
        int i = 0;
        for (String newSeg : segments)
            backingNode.put(Integer.toString(++i), newSeg);
    }

    /**
     * Standard profile constructor.
     *
     * @param node the {@link Preferences} node containing the profile data
     */
    public Profile(Preferences node) throws BackingStoreException {
        super(node.name());
        backingNode = node;
        segments = Arrays.stream(node.keys())
                .sorted()
                .map((k) -> node.get(k, Main.TIME_NULL))
                .collect(Collectors.toCollection(ArrayList::new));

    }

    public void dispose() {
        try {
            backingNode.removeNode();
            // Not syncing as per removeNode definition
            backingNode.flush();
            Main.logger.log(Level.FINE, "Profile {0} successfully disposed", getText());
        }
        catch (BackingStoreException ex) {
            Main.logger.log(Level.WARNING, "Failed to remove profile " + getText() + "from backing store", ex);
        }
    }

    @Override
    public int compareTo(Profile other) {
        if (other == VOID) return 1;
        return String.CASE_INSENSITIVE_ORDER.compare(this.getText(), other.getText());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Profile
                ? getText().toLowerCase().equals(((Profile) obj).getText().toLowerCase())
                : false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(getText().toLowerCase());
        hash = 67 * hash + Objects.hashCode(Profile.class);
        return hash;
    }

    //TODO Encapsulate?
    @Override
    public Iterator<String> iterator() {
        return segments.iterator();
    }

}
