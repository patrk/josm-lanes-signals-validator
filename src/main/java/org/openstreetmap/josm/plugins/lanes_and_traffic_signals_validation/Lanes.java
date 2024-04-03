package org.openstreetmap.josm.plugins.lanes_and_traffic_signals_validation;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

//  import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.tools.Utils;

/**
 * Test that validates {@code lane:} tags.
 * @since 6592
 */
public class Lanes extends Test.TagTest {

    /**
     * Constructs a new {@code Lanes} test.
     */
    public Lanes() {
        super(tr("pmikul: Lane tags"), tr("Test that validates ''lane:'' tags."));
    }

    @Override
    public void check(OsmPrimitive p) {
        errors.add(TestError.builder(this, Severity.WARNING, 8000)
                .message("primitive found in Lanes test")
                .primitives(p)
                .build());
        //checkNumberOfLanesByKey(p, "lanes", tr("Number of lane dependent values inconsistent"));
        //checkNumberOfLanesByKey(p, "lanes:forward", tr("Number of lane dependent values inconsistent in forward direction"));
        //checkNumberOfLanesByKey(p, "lanes:backward", tr("Number of lane dependent values inconsistent in backward direction"));
        //checkNumberOfLanes(p);
    }
}