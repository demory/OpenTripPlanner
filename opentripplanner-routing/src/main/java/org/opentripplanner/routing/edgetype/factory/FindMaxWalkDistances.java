/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.routing.edgetype.factory;

import java.util.HashSet;

import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.Graph;
import org.opentripplanner.routing.core.GraphVertex;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TransitStop;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.edgetype.StreetVertex;
import org.opentripplanner.routing.pqueue.BinHeap;
import org.opentripplanner.routing.spt.BasicShortestPathTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindMaxWalkDistances {
    
    private final static Logger _log = LoggerFactory.getLogger(FindMaxWalkDistances.class);

    public static void find(Graph graph) {
        _log.debug("finding max walk distances");
        for (GraphVertex gv : graph.getVertices()) {
            gv.vertex.setDistanceToNearestTransitStop(Double.MAX_VALUE);
        }
        for (GraphVertex gv : graph.getVertices()) {
            if (gv.vertex instanceof TransitStop) { 
                assignStopDistances(graph, (TransitStop) gv.vertex);
            }
        }
        for (GraphVertex gv : graph.getVertices()) {
            if (gv.vertex.getDistanceToNearestTransitStop() == Double.MAX_VALUE) {
                /* transit vertices don't get explored by assignStopDistances */
                gv.vertex.setDistanceToNearestTransitStop(0);
            }
        }
    }
    
    /**
     * TODO - Can this reuse one of the existing search algorithms? 
     */
    private static void assignStopDistances(Graph graph, TransitStop origin) {
        
        TraverseOptions options = new TraverseOptions(new TraverseModeSet(TraverseMode.WALK));
        options.setMaxWalkDistance(Double.MAX_VALUE);
        options.walkReluctance = 1.0;
        options.speed = 1.0;
        
        // Iteration Variables
        State u, v;
        HashSet<Vertex> closed = new HashSet<Vertex>();
        BinHeap<State> queue = new BinHeap<State>(50);
        BasicShortestPathTree spt = new BasicShortestPathTree();
        State init = new State(origin, options);
        spt.add(init);
        queue.insert(init, init.getWeight());

        while (!queue.empty()) { // Until the priority queue is empty:

            u = queue.extract_min(); // get the lowest-weightSum Vertex 'u',

            Vertex fromv = u.getVertex();

            closed.add(fromv);

            Iterable<Edge> outgoing = graph.getOutgoing(fromv);

            for (Edge edge : outgoing) {

                v = edge.traverse(u);

                // When an edge leads nowhere (as indicated by returning NULL), the iteration is
                // over.
                if (v == null)
                    continue;

                double dw = v.getWeight() - u.getWeight();
                if (dw < 0) {
                    throw new NegativeWeightException(String.valueOf(dw));
                }
                
                Vertex toVertex = v.getVertex();

                if (closed.contains(toVertex)) {
                    continue;
                }

                double new_w = v.getWeight();

                if (toVertex instanceof StreetVertex) {
                    StreetVertex sv = (StreetVertex) toVertex;
                    if (sv.getDistanceToNearestTransitStop() <= new_w) {
                        continue;
                    }
                    sv.setDistanceToNearestTransitStop(new_w);
                }
                
                if (spt.add(v))
                    queue.insert(v, new_w);
            }
        }
    }

}
