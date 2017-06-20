package org.openstreetmap.josm.plugins.lanes_and_traffic_signals_validation;

import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

import org.openstreetmap.josm.data.validation.OsmValidator;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;

	public class LanesTrafficSignalsValidationPlugin extends Plugin {
  
 /**
  * Will be invoked by JOSM to bootstrap the plugin
  *
  * @param info  information about the plugin and its local installation    
  */
		public LanesTrafficSignalsValidationPlugin(PluginInformation info) {
			super(info);
		     // init your plugin 
		     MainMenu menu = Main.main.menu;
		     
		     OsmValidator.addTest(SignalsTest.class);
		}
		


 }