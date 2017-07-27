package org.openstreetmap.josm.plugins.lanes_and_traffic_signals_validation;

/**
 * Created by Patryk on 22.07.2017.
 */

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import java.util.*;
import static org.openstreetmap.josm.tools.I18n.tr;

public class TurnLanesCoherence extends Test{

    final static Set<String> substractringTurns = new HashSet<String>(){{
        add("reverse");
        add("sharp_left");
        add("left");
        add("slight_left");
        add("sharp_left");
        add("merge_to_right");
        add("merge_to_left");
        add("slight_right");
        add("right");
        add("sharp_right");
    }};

    final static Set<String> persistingLanes = new HashSet<String>(){{
        add("none");
        add("");
        add("through");
    }};

    public TurnLanesCoherence() {
        super(tr("pmikul: Turn lanes coherence"), tr("Test that validates the ''turn:lanes:'' coherence of subsequent Way objects."));
    }

    static int getLanesCount(String value) {
        return value.isEmpty() ? 0 : value.replaceAll("[^|]", "").length() + 1;
    }

    private void checkTurnLanesCoherence(Way w, String lanesKey){
        final String turnLanes = w.get(lanesKey);
        if (turnLanes == null) return;
        final int lanesCount = getLanesCount(turnLanes);

        final Node firstNode = w.firstNode();
        final List<Way> firstNodeParentWays = firstNode.getParentWays();
        // if (firstNodeParentWays.size() != 1) return;
        for(Way parentWay : firstNodeParentWays){
            if(!parentWay.hasKey("highway")) return;
            if(!parentWay.lastNode().equals(firstNode)) return;
            final String parentTurnLanes = parentWay.get(lanesKey);
            if (turnLanes == null) return;
            int parentLanesCount = getLanesCount(parentTurnLanes);
            if (parentLanesCount != lanesCount){
                int diffLanesCount = parentLanesCount - lanesCount;
                // turn:lanes value will be split by ; and | since we are interested if there is any turn relevant turn direction
                Set<String> originalTurns = new HashSet<>(Arrays.asList(turnLanes.split("\\W", -1)));
                if(diffLanesCount > 0){
                    Set<String> originalTurnsSubstracting = new HashSet<>(originalTurns);
                    originalTurnsSubstracting.retainAll(substractringTurns);
                    if((diffLanesCount != originalTurnsSubstracting.size())) {
                        errors.add(TestError.builder(this, Severity.WARNING, 8002)
                                .message("Preceeded by too few turn or merge lanes")
                                .primitives(w, parentWay)
                                .build());
                    }
                    Set<String> originalTurnsPersisting = new HashSet<>(originalTurns);
                    originalTurnsPersisting.retainAll(persistingLanes);
                    if((diffLanesCount != originalTurnsPersisting.size())){
                        errors.add(TestError.builder(this, Severity.WARNING, 8003)
                                .message("Preceeded by too few persisting lanes (through or none)")
                                .primitives(w, parentWay)
                                .build());
                    }
                }
            }
        }
    }

    @Override
    public void visit(Way w){
        checkTurnLanesCoherence(w, "turn:lanes");
        checkTurnLanesCoherence(w, "turn:lanes:forward");
        checkTurnLanesCoherence(w, "turn:lanes:backward");
    }

}