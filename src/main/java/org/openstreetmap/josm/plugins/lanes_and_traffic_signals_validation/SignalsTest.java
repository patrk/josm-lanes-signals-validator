package org.openstreetmap.josm.plugins.lanes_and_traffic_signals_validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.data.validation.Severity;


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

    @Override
    public void visit(Node n) {
    	if(n.isUsable() && !n.isOutsideDownloadArea()){
    		if(n.hasTag("crossing","traffic_signals") && !n.hasTag("highway", "traffic_signals")){
    			boolean ok = false;
    			List<Way> parentWays = n.getParentWays();
    			for(Way w : parentWays){
    				Set<Node> neighbours = w.getNeighbours(n);
    				for(Node neighbour : neighbours){
    					if(SignalsTest.isJunction(neighbour)  || neighbour.hasTag("highway", "traffic_signals")){
    						ok = true;	
    					}
    				}
    			}
    			if(!ok){
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
			String msg = "Junctions found in Endtest\n";
			System.out.println(msg);
			TestError error = TestError.builder(this, Severity.WARNING, 8888).message(msg).primitives(junction).highlight(junction).build();
			errors.add(error);
		}
		
		super.endTest();

    }

}
