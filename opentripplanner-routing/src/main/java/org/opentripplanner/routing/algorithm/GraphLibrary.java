/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (props, at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.routing.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.Graph;
import org.opentripplanner.routing.core.GraphVertex;
import org.opentripplanner.routing.core.HasEdges;
import org.opentripplanner.routing.core.Vertex;

public class GraphLibrary {

    public static Collection<Edge> getIncomingEdges(Graph graph, Vertex tov,
            Map<Vertex, List<Edge>> extraEdges) {

        Collection<Edge> incoming = null;

        if (tov instanceof HasEdges) {

            /**
             * As a performance tweak, note that we don't examine 'extraEdges' if the Vertex
             * implements HasEdges, since the whole point is to avoid the HashMap lookup.
             */
            incoming = extendEdges(incoming, ((HasEdges) tov).getIncoming());
            
        } else {
            GraphVertex gv = graph.getGraphVertex(tov);
            if (gv != null)
                incoming = extendEdges(incoming, gv.getIncoming());

            if (extraEdges != null && extraEdges.containsKey(tov))
                incoming = extendEdges(incoming, extraEdges.get(tov));
        }

        if (incoming == null)
            incoming = Collections.emptyList();

        return incoming;
    }

    public static Collection<Edge> getOutgoingEdges(Graph graph, Vertex fromv,
            Map<Vertex, List<Edge>> extraEdges) {

        Collection<Edge> outgoing = null;

        if (fromv instanceof HasEdges) {
            
            /**
             * As a performance tweak, note that we don't examine 'extraEdges' if the Vertex
             * implements HasEdges, since the whole point is to avoid the HashMap lookup.
             */
            outgoing = extendEdges(outgoing, ((HasEdges) fromv).getOutgoing());
            
        } else {
            GraphVertex gv = graph.getGraphVertex(fromv);
            if (gv != null)
                outgoing = extendEdges(outgoing, gv.getOutgoing());

            if (extraEdges != null && extraEdges.containsKey(fromv))
                outgoing = extendEdges(outgoing, extraEdges.get(fromv));
        }

// Is this necessary? It includes both incoming and outgoing extra edges in the result, 
// which causes a lot of defective traversals. (AMB)
//        if (fromv instanceof StreetLocation) {
//            StreetLocation sl = (StreetLocation) fromv;
//            outgoing = extendEdges(outgoing, sl.getExtra());
//        }

        if (outgoing == null)
            outgoing = Collections.emptyList();

        return outgoing;
    }

    /****
     * Private Methods
     ****/

    private static <E extends Edge> Collection<Edge> extendEdges(Collection<Edge> existing,
            Collection<E> additionalEdges) {

        if (existing == null || existing.size() == 0) {
            if (additionalEdges == null || additionalEdges.isEmpty())
                return null;
            return new ArrayList<Edge>(additionalEdges);
        }

        if (additionalEdges == null || additionalEdges.size() == 0)
            return existing;

        List<Edge> edges = new ArrayList<Edge>(existing.size() + additionalEdges.size());
        edges.addAll(existing);
        edges.addAll(additionalEdges);
        return edges;
    }
}
