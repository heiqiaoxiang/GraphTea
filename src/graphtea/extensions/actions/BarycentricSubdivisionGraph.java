// GraphTea Project: http://github.com/graphtheorysoftware/GraphTea
// Copyright (C) 2012 Graph Theory Software Foundation: http://GraphTheorySoftware.com
// Copyright (C) 2008 Mathematical Science Department of Sharif University of Technology
// Distributed under the terms of the GNU Lesser General Public License (LGPL): http://www.gnu.org/licenses/

package graphtea.extensions.actions;

import graphtea.graph.graph.Edge;
import graphtea.graph.graph.GraphModel;
import graphtea.graph.graph.GraphPoint;
import graphtea.graph.graph.Vertex;
import graphtea.platform.parameter.Parameter;
import graphtea.platform.parameter.Parametrizable;
import graphtea.plugins.main.GraphData;
import graphtea.plugins.main.extension.GraphActionExtension;


/**
 * Creates a line graph from the current graph and shows it in a new tab
 *
 * @author Mohammad Ali Rostami
 * @author Azin Azadi
 */
public class BarycentricSubdivisionGraph implements GraphActionExtension, Parametrizable {
    @Parameter
    public int k = 2;

    public void action(GraphData graphData) {

        GraphModel g1 = graphData.getGraph();
        GraphModel g2 = new GraphModel(false);//

        for(Vertex v : g1.getVertexArray()) {
            Vertex tmp = new Vertex();
            tmp.setLocation(v.getLocation());
            g2.addVertex(tmp);
        }

        for(Edge e : g1.getEdges()) {
            GraphPoint v1 = e.source.getLocation();
            GraphPoint v2 = e.target.getLocation();
            double dis = v1.distance(v2);
            GraphPoint v3 = GraphPoint.sub(v2, v1);
            v3 = GraphPoint.div(v3, k+1);

            Vertex src = g2.getVertex(e.source.getId());
            for (int i = 0; i < k; i++) {
                Vertex tmp = new Vertex();
                GraphPoint v4 = new GraphPoint(v3);
                v4.multiply(i + 1);
                v4.add(v1);
                tmp.setLocation(v4);
                g2.addVertex(tmp);
                g2.addEdge(new Edge(
                        src,
                        tmp
                ));
                src=tmp;
            }

            g2.addEdge(new Edge(
                    src,
                    g2.getVertex(e.target.getId())
            ));
        }

       graphData.core.showGraph(g2);

    }

    public String getName() {
        return "Barycentric Subdivision Graph";
    }

    public String getDescription() {
        return "Barycentric Subdivision Graph";
    }

    @Override
    public String checkParameters() {
        return null;
    }

    @Override
    public String getCategory() {
        return "Transformations";
    }
}
