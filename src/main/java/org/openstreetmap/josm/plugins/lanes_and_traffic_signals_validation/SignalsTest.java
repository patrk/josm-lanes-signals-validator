package org.openstreetmap.josm.plugins.lanes_and_traffic_signals_validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Predicate;


import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.data.validation.Severity;

import org.openstreetmap.josm.tools.Pair;



public class SignalsTest extends Test{

	private List<Way> waysWithLanes;
	private List<Node> junctions;
	public SignalsTest() {
		
		super("Traffic Signals Validation");
		// TODO Auto-generated constructor stub
		this.waysWithLanes = new ArrayList<>();
		this.junctions = new ArrayList<>();
	}
	
    @Override
    public void visit(Way w) {
       
    	if (w.isUsable()) {
            if (w.hasKey("lanes")){
            	this.waysWithLanes.add(w);
            	//System.out.println("visitor found lane\n");
            }
        }
    }
    
    
    static int getLanesCount(String value) {
        return value.isEmpty() ? 0 : value.replaceAll("[^|]", "").length() + 1;
    }
    
    static boolean isJunction(Node n){
    	return n.getParentWays().size() > 2;
    }

    static int getNodeIdxAlongWay(Way w, Node n){
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

	private static boolean preceededByTrafficSignal(Way w, Node n){
		int nIdx = getNodeIdxAlongWay(w, n);
		int end;
		if (w.isOneway() > -1) {
			end = 0;
		}
		else{
			end = w.getNodesCount();
		}
		for (int i = nIdx; i > end;){
			if (w.isOneway() > -1){
				i--;
			}
			else{
				i++;
			}
			if (w.getNode(i).hasTag("highway", "traffic_signals")){
				return true;
			}
			if (SignalsTest.isJunction(w.getNode(i))){
				return false;
			}
		}
		return false;
	}

    @Override
    public void visit(Node n) {
    	if(n.isUsable() && !n.isOutsideDownloadArea()) {
			if (n.hasTag("crossing", "traffic_signals") && !n.hasTag("highway", "traffic_signals")) {
				boolean ok = false;


				List<Way> parentWays = n.getParentWays();
				for (Way w : parentWays) {

					if (w.hasTag("highway", "primary") ||
							w.hasTag("highway", "secondary") ||
							w.hasTag("highway", "teriary")) {

						if (SignalsTest.preceededByTrafficSignal(w, n)) {
							ok = true;
						}

					}
				}
				if (!ok) {
					this.junctions.add(n);
				}
			}
		}
    }

    @Override
	public void endTest() {

//		for (Way way : waysWithLanes){
//			String msg = "Lane found in Endtest\n";
//			System.out.println(msg);
//			TestError error = TestError.builder(this, Severity.ERROR, 9999).message(msg).primitives(way).highlight(way).build();
//			errors.add(error);
//		}
		
		for (Node junction : junctions){
			String msg = "Pedestrian crossing but no corresponding traffic signals found\n";
			System.out.println(msg);
			TestError error = TestError.builder(this, Severity.WARNING, 8888).message(msg).primitives(junction).highlight(junction).build();
			errors.add(error);
		}
		
		super.endTest();

    }

}
