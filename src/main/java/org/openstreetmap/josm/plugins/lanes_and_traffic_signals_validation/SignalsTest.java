package org.openstreetmap.josm.plugins.lanes_and_traffic_signals_validation;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;



public class SignalsTest extends Test{

	final static List<String> pedestrianWays = new ArrayList<>() {{
        add("footway");
        add("bridleway");
        add("steps");
        add("path");
        add("pedestrian");
    }};

	final static List<String> motorizedWays = new ArrayList<>() {{
        add("motorway");
        add("trunk");
        add("primary");
        add("secondary");
        add("tertiary");
        add("residential");
        add("service");
        add("motorway_link");
        add("trunk_link");
        add("primary_link");
        add("secondary_link");
        add("tertiary_link");
        add("living_street");
        add("road");
    }};

	private List<Node> junctions;
	public SignalsTest() {
		
		super("pmikul: Traffic Signals");
		this.junctions = new ArrayList<>();
	}
    static boolean isJunction(Node n){
    	return n.getParentWays().size() > 2;
    }

    private static int getNodeIdxAlongWay(Way w, Node n){
    	if (n.getParentWays().contains(w)){
			int nodesCount = w.getNodesCount();
			for (int i = 0; i < nodesCount; i++){
				if (w.getNode(i).equals(n)){
					return i;
				}
			}
		}
		return 0;
	}

	private static Predicate<Node> hasTrafficSignals(){
		return p -> p.hasTag("highway", "traffic_signals");
	}

	private void checkPedestrianCrossingWithoutIntersection(Node n){
		if(!(n.hasTag("highway","crossing"))) return;
		// Consider only nodes which mark a pedestrian crossing
		if(!(n.hasTag("crossing", "traffic_signals")) && crossesLargeRoad(n)){
			errors.add(TestError.builder(this, Severity.WARNING, 7001)
					.message("found pedestrian crossing and lanes > 2 without pedestrian signals")
					.primitives(n)
					.build());
			return;
		}
		if((n.hasTag("crossing", "traffic_signals")) && !(isPrecededbyTrafficSignals(n)) && !n.hasTag("highway", "traffic_signals")) {
			errors.add(TestError.builder(this, Severity.WARNING, 7002)
					.message("found pedestrian traffic signal but no traffic signals for motorized traffic")
					.primitives(n)
					.build());
		}


	}

	private static boolean isPrecededbyTrafficSignals(Node n){
		List<Way> parentWays = n.getParentWays();

		for(Way parentWay : parentWays) {
			if(!(parentWay.hasKey("highway")) && pedestrianWays.contains(parentWay.get("highway"))) continue;
			int nIdx = getNodeIdxAlongWay(parentWay, n);
			int end;
			if (parentWay.isOneway() > -1) {
				end = 0;
			} else {
				end = parentWay.getNodesCount();
			}
			for (int i = nIdx; i > end; ) {
				if (parentWay.isOneway() > -1) {
					i--;
				} else {
					i++;
				}
				if (parentWay.getNode(i).hasTag("highway", "traffic_signals")) {
					return true;
				}
			}
		}
	return false;
	}

	private static boolean crossesLargeRoad(Node n){
		// crossing has no traffic signal check lane count of involved motorized way
		boolean crosses = false;
		List<Way> parentWays = n.getParentWays();
		//List<Way> parentHighways = parentWays.stream().filter(p -> p.hasKey("highway").collect(Collectors.toList()));
		for(Way pw : parentWays){
			if(!(pw.hasKey("highway"))) continue;
			if(!(pw.hasKey("lanes"))) continue;
			if(Integer.parseInt(pw.get("lanes")) > 2) {
				return true;
			}
		}
		return false;
	}

	private void checkSimpleIntersection(Node n){	}

	@Override
	public void visit(Node n){
		checkPedestrianCrossingWithoutIntersection(n);
	}
}
