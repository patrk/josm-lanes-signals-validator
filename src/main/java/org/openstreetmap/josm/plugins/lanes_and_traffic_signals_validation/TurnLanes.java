package org.openstreetmap.josm.plugins.lanes_and_traffic_signals_validation;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.openstreetmap.josm.tools.I18n.tr;

public class TurnLanes extends Test.TagTest{

    // Ordering relation for directions
    final static List<String> listTurns = new ArrayList<>() {{
        add("reverse");
        add("sharp_left");
        add("left");
        add("slight_left");
        add("sharp_left");
        add("merge_to_right");
        add("left;through");
        add("slight_left;through");
        add("through");
        add("through;right");
        add("through;slight_right");
        add("merge_to_left");
        add("slight_right");
        add("right");
        add("sharp_right");
    }};
    // Turn lanes with no turning indication
    final static List<String> noneTurns = new ArrayList<>() {{
        add("none");
        add("");
    }};

    public TurnLanes() {
        super(tr("pmikul: Turn lanes"), tr("Test that validates ''turn:lanes:'' tags."));
    }



    // Implementation of an ordering relation for sorting purpose
    private static int compareDirection(String s1, String s2){

       // Turn lanes with no turning indication will be ignored, since cycleways can be included in turn:lanes
        if(noneTurns.contains(s1) || noneTurns.contains(s2)) return 0;

        return Integer.compare(listTurns.indexOf(s1), listTurns.indexOf(s2));
    }

    private boolean evaluateTurnLanes(String turnLanes){
        // Split turn:lanes value into list of turn directions
        // ; and | are treated equally, since ordering of lanes with multiple turn directions matters
        // in this implementation any non-word separator is allowed - can be restricted to ; and | only
        List<String> originalTurns = Arrays.asList(turnLanes.split("\\W", -1));
        List<String> sortedTurns = new ArrayList<>(originalTurns);

        sortedTurns.sort(TurnLanes::compareDirection);
        // Ordering of original turn direction is assumed to be correct if it is equal to the sorted list
        return (sortedTurns.equals(originalTurns));
    }

    private void checkTurnLanesOrdering(OsmPrimitive p, String lanesKey){
        final String turnLanes = p.get(lanesKey);
        if (turnLanes == null) return;
        errors.add(TestError.builder(this, Severity.OTHER, 9999)
                .message("turn:lanes found")
                .primitives(p)
                .build());
        if (!(evaluateTurnLanes(turnLanes))){
            errors.add(TestError.builder(this, Severity.WARNING, 8001)
                    .message("turn:lanes are not in left-to-right ordering")
                    .primitives(p)
                    .build());
        }
    }

    @Override
    public void check(OsmPrimitive p) {
        checkTurnLanesOrdering(p, "turn:lanes");
        checkTurnLanesOrdering(p, "turn:lanes:forward");
        checkTurnLanesOrdering(p, "turn:lanes:backward");
    }

}
