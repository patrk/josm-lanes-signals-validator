package org.openstreetmap.josm.plugins.lanes_and_traffic_signals_validation;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;

import java.util.*;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Created by Patryk on 23.07.2017.
 */
public class TurnRestrictions extends Test {

    public TurnRestrictions(){
        super(tr("pmikul: Turn restrictions"), tr("Test that validates whether turn restrictions are violated by turn:lanes tags"));
    }

    private void checkTurnRestrictions(Relation r){
        if(!r.hasTag("type", "restriction")) return;
        if(!r.hasKey("restriction")) return;

        final String restrictionValue = r.get("restriction");

        final List<String> rightTurns = new ArrayList<String>(){{
            add("slight_right");
            add("right");
            add("sharp_right");
        }};

        final List<String> leftTurns = new ArrayList<String>(){{
            add("sharp_left");
            add("left");
            add("slight_left");
            add("sharp_left");
        }};

        final List<String> rightRestrictions = new ArrayList<String>(){{
            add("no_right_turn");
            add("only_left_turn");
        }};

        final List<String> leftRestrictions = new ArrayList<String>(){{
            add("no_left_turn");
            add("only_right_turn");
        }};



        List<RelationMember> members = r.getMembers();
        Set<OsmPrimitive> memberPrimitives = r.getMemberPrimitives();
        for(RelationMember member : members) {
            if (!member.hasRole("from")) return;
            if (member.hasRole("to")) return;
            if (member.hasRole("via")) return;

            Way fromWay = member.getWay();
            final String fromWayTurns = fromWay.get("turn:lanes");
            List<String> fromWayTurnsList = Arrays.asList(fromWayTurns.split("\\W"));
            final String fromWayTurnsForward = fromWay.get("turn:lanes:forward");
            List<String> fromWayTurnsForwardList = Arrays.asList(fromWayTurnsForward.split("\\W"));
            final String fromWayTurnsBackward = fromWay.get("turn:lanes:backward");
            final List<String> fromWayTurnsBackwardList = Arrays.asList(fromWayTurnsBackward.split("\\W"));

            final List<String> fromWayAllTurns = new ArrayList<String>(fromWayTurnsList);
            fromWayAllTurns.addAll(fromWayTurnsForwardList);
            fromWayAllTurns.addAll(fromWayTurnsBackwardList);


            if (restrictionValue == "no_u_turn" && fromWayAllTurns.contains("reverse")) {
                errors.add(TestError.builder(this, Severity.WARNING, 9000)
                        .message("Turn restriction no_u_turn violated by turn:lanes=reverse tag")
                        .primitives(memberPrimitives)
                        .build());
            }
            if (leftRestrictions.contains(restrictionValue) && !Collections.disjoint(fromWayAllTurns, leftTurns)){
                errors.add(TestError.builder(this, Severity.WARNING, 9001)
                        .message("Turn restriction no left turn violated by left turn")
                        .primitives(memberPrimitives)
                        .build());
            }

            if (rightRestrictions.contains(restrictionValue) && !Collections.disjoint(fromWayAllTurns, rightTurns)){
                errors.add(TestError.builder(this, Severity.WARNING, 9002)
                        .message("Turn restriction no right turn violated by right turn")
                        .primitives(memberPrimitives)
                        .build());
            }

            if ((rightRestrictions.contains(restrictionValue) || leftRestrictions.contains(restrictionValue) || restrictionValue=="no_straight_on")
                    && fromWayAllTurns.contains("through")){
                errors.add(TestError.builder(this, Severity.WARNING, 9003)
                        .message("Turn restriction violated by turn:lanes=through")
                        .primitives(memberPrimitives)
                        .build());
            }

            if (restrictionValue=="only_straight_on" && !Collections.disjoint(fromWayAllTurns, rightTurns) && !Collections.disjoint(fromWayAllTurns, leftTurns)){
                errors.add(TestError.builder(this, Severity.WARNING, 9004)
                        .message("Restriction only_straight_on violated by turn:lanes")
                        .primitives(memberPrimitives)
                        .build());
            }
        }
    }

    @Override
    public void visit(Relation r) {
       checkTurnRestrictions(r);

    }

}
