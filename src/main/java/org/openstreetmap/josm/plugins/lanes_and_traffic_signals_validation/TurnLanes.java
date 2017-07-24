package org.openstreetmap.josm.plugins.lanes_and_traffic_signals_validation;

/**
 * Created by Patryk on 22.07.2017.
 */

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import java.util.*;
import static org.openstreetmap.josm.tools.I18n.tr;

public class TurnLanes extends Test.TagTest{

    // Ordering relation for directions
    final static List<String> listTurns = new ArrayList<String>(){{
        add("reverse");
        add("sharp_left");
        add("left");
        add("slight_left");
        add("sharp_left");
        add("merge_to_right");
        add("through");
        add("merge_to_left");
        add("slight_right");
        add("right");
        add("sharp_right");
    }};
    // Turn lanes with no turning indication
    final static List<String> noneTurns = new ArrayList<String>(){{
        add("none");
        add("");
    }};

    public TurnLanes() {
        super(tr("pmikul: Turn lanes"), tr("Test that validates ''turn:lane:'' tags."));
    }

    static int getLanesCount(String value) {
        return value.isEmpty() ? 0 : value.replaceAll("[^|]", "").length() + 1;
    }

    // Implementation of an ordering relation for sorting purpose
    private static int compareDirection(String s1, String s2){
        // Turn lanes with no turning indication will be ignored, since cycleways can be included in turn:lanes
        if(noneTurns.contains(s1) || noneTurns.contains(s2)) return 0;

        if(listTurns.indexOf(s1) > listTurns.indexOf(s2)) return 1;
        if(listTurns.indexOf(s1) < listTurns.indexOf(s2)) return -1;
        return 0;
    }

    private boolean evaluateTurnLanes(String turnLanes){
        // Split turn:lanes value into list of turn directions
        // ; and | are treated equally, since ordering of lanes with multiple turn directions matters
        // in this implementation any non-word separator is allowed - can be restricted to ; and | only
        List<String> originalTurns = Arrays.asList(turnLanes.split("\\W", -1));
        List<String> sortedTurns = new ArrayList<>(originalTurns);

        sortedTurns.sort((s1, s2) -> compareDirection(s1, s2));
        // Ordering of original turn direction is assumed to be correct if it is equal to the sorted list
        if (sortedTurns.equals(originalTurns)) return true;
        else {
            return false;
        }
    }

    private void checkTurnLanesOrdering(OsmPrimitive p, String lanesKey){
        final String turnLanes = p.get(lanesKey);
        if (turnLanes == null) return;
        if (!(evaluateTurnLanes(turnLanes))){
            errors.add(TestError.builder(this, Severity.WARNING, 8001)
                    .message("turn:lanes are not in left-to-right ordering")
                    .primitives(p)
                    .build());
        }
    }

    private void checkTurnLanesCoherence(OsmPrimitive p, String lanesKey){
        final String turnLanes = p.get(lanesKey);
        if (turnLanes == null) return;
        final int lanesCount = getLanesCount(turnLanes);

    }

    @Override
    public void check(OsmPrimitive p) {
        checkTurnLanesOrdering(p, "turn:lanes");
        checkTurnLanesOrdering(p, "turn:lanes:forward");
        checkTurnLanesOrdering(p, "turn:lanes:backward");
    }

}
