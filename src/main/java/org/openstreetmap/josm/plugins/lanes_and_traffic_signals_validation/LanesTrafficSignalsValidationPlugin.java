package org.openstreetmap.josm.plugins.lanes_and_traffic_signals_validation;


import org.openstreetmap.josm.data.validation.OsmValidator;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

	public class LanesTrafficSignalsValidationPlugin extends Plugin {
  
 /**
  * Will be invoked by JOSM to bootstrap the plugin
  *
  * @param info  information about the plugin and its local installation    
  */
		public LanesTrafficSignalsValidationPlugin(PluginInformation info) {
			super(info);
		     
            OsmValidator.addTest(SignalsTest.class);
            OsmValidator.addTest(TurnLanes.class);
            OsmValidator.addTest(TurnRestrictions.class);
            OsmValidator.addTest(TurnLanesCoherence.class);
		}
		


 }