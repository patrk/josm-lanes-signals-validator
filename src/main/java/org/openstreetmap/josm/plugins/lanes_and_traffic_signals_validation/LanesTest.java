package org.openstreetmap.josm.plugins.lanes_and_traffic_signals_validation;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Test;

import java.util.*;

/**
 * Created by Patryk on 09.07.17.
 */
public class LanesTest extends Test {

    final private static int NONE = -1;
    final private List<String> turnLanesList = new ArrayList<>(Arrays.asList("turn:lanes", "turn:lanes:forward", "turn:lanes:backward", "turn:lanes:both_ways"));
    final private List<String> directionsList = new ArrayList<>(Arrays.asList("left", "slight_left", "sharp_left", "through", "right", "slight_right", "sharp_right", "reverse", "merge_to_left", "merge_to_right", "none", ""));

    public LanesTest() {
        super("Lanes Validation");
    }


    @Override
    public void visit(Way w) {

        final TagMap tags = w.getKeys();

        if (!w.hasKey("highway")) {
            return;
        }

        if (!w.hasKey("lanes")) {
            return;
        }

        final Map<String, String> tagsLanes = new HashMap<>();
        for (String tag : tags.keySet()) {
            // TODO what about case sensitivity?
            if (tag.contains("lanes") && !tag.contains("source") && !tag.contains("FIXME")) {
                tagsLanes.put(tag, tags.get(tag));
            }
        }

        // TODO replace with own err object, Patryk
        final ArrayList<Map<String, Integer>> err = new ArrayList<>();

        // Check turn lanes values
        final boolean tl = tagsLanes.containsKey("turn:lanes");
        final boolean tlf = tagsLanes.containsKey("turn:lanes:forward");
        final boolean tlb = tagsLanes.containsKey("turn:lanes:backward");
        final boolean tl2 = tagsLanes.containsKey("turn:lanes:both_ways");

        if (tl || tlf || tlb || tl2) {
            for (String tld : this.turnLanesList) { // Todo Ich musste das in tld umbenennen, Overshadow ist ein Fehler in Java
                if (tagsLanes.containsKey(tld)) {
                    final String[] ttt = tagsLanes.get(tld).split("|");
                    boolean unknown = false;
                    int i = 0;
                    for (String tt : ttt) {
                        for (String t : tt.split(";")) {
                            if (!this.directionsList.contains(t)) {
                                unknown = true;
                                final Map<String, Integer> temporaryMap = new HashMap<>();
                                temporaryMap.put("class", 31606);
                                temporaryMap.put("subclass", 1);
                                temporaryMap.put("text", 0);
                                err.add(temporaryMap);
                            }
                            if ((t.equals("merge_to_left") && i == 0) || (t.equals("merge_to_right") && i == (ttt.length - 1))) {
                                final Map<String, Integer> temporaryMap = new HashMap<>();
                                temporaryMap.put("class", 31600);
                                temporaryMap.put("subclass", 1);
                                err.add(temporaryMap);
                            }
                            i++;
                        }
                        if (!unknown) {
                            // merge_to_left is a on the right and vice versa
                            final String[] t_ = tagsLanes.get(tld) // TODO musste das Umbenennen weil t im Python code ganz unten ein String ist und hier ein String-Array
                                    .replace("left", "l")
                                    .replace("sharp_left", "l")
                                    .replace("through", " ")
                                    .replace("right", "r").replace("slight_right", "r").replace("sharp_right", "r")
                                    .replace("reverse", "U")
                                    .replace("merge_to_left", "r").replace("merge_to_right", "l")
                                    .replace("none", " ").replace(";", "").split("|");

                            String t; // TODO man der Typ weiss echt wie man Sachen kompli macht...
                            Arrays.sort(t_);
                            if (t_.length == 0 || t_[0].equals(t_[t_.length])) { // VORSICHT! String-Vergleich, daher equals statt ==
                                t = " ";
                            } else {
                                t = t_[0];
                            }
                            t = t.replace("U", ""); // Ignore reverse
                            final int lastLeft = this.rindex_(t, "l");
                            final int firstSpace = this.index_(t, " ");
                            final int lastSpace = this.rindex_(t, " ");
                            final int firstRight = this.index_(t, "r");
                            // Check right is on the right and left is on the left...
                            if (!((lastLeft == NONE || firstSpace == NONE || lastLeft < firstSpace) && (firstSpace == NONE || lastSpace == NONE || firstSpace <= lastSpace) && (lastSpace == NONE || firstRight == NONE || lastSpace < firstRight) && (lastLeft == NONE || firstRight == NONE || lastLeft < firstRight))) {
                                final Map<String, Integer> temporaryMap = new HashMap<>();
                                temporaryMap.put("class", 31607);
                                temporaryMap.put("subclass", 1);
                                err.add(temporaryMap);
                            }
                        }
                    }
                }
            }
        }
    }

    private int index_(String l, String e) {
        try {
            return l.indexOf(e);
        } catch (ValueException exception) {
            return NONE;
        }
    }

    private int rindex_(String l, String e) {
        try {
            return l.lastIndexOf(e);
        } catch (ValueException exception) {
            return NONE;
        }
    }

}
