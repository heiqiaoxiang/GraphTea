// GraphTea Project: http://github.com/graphtheorysoftware/GraphTea
// Copyright (C) 2012 Graph Theory Software Foundation: http://GraphTheorySoftware.com
// Copyright (C) 2008 Mathematical Science Department of Sharif University of Technology
// Distributed under the terms of the GNU General Public License (GPL): http://www.gnu.org/licenses/
package graphtea.plugins.main.select;

import graphtea.graph.event.GraphEvent;
import graphtea.graph.event.VertexEvent;
import graphtea.graph.graph.AbstractGraphRenderer;
import graphtea.graph.graph.GraphPoint;
import graphtea.graph.graph.SubGraph;
import graphtea.graph.graph.Vertex;
import graphtea.library.exceptions.InvalidVertexException;
import graphtea.platform.core.AbstractAction;
import graphtea.platform.core.BlackBoard;
import graphtea.plugins.main.GraphData;

/**
 * @author azin azadi
 */
public class MoveSelected extends AbstractAction {
    public static final String SELECTION_MOVED = "Selection Moved";
    public static final String SELECTION_MOVING = "Selection Moving";

    /**
     * constructor
     *
     * @param bb the blackboard of the action
     */
    public MoveSelected(BlackBoard bb) {
        super(bb);
        listen4Event(VertexEvent.EVENT_KEY);
        listen4Event(GraphEvent.EVENT_KEY);
        gd = new GraphData(blackboard);
    }

    GraphData gd;
    private GraphPoint[] verticesPositionsBackUp;

    public void performAction(String eventName, Object value) {
        gv = blackboard.getData(AbstractGraphRenderer.EVENT_KEY);
        //while resizing the vertex it shouldn't move it
//resize
//        if (b == null || !b) {
        if (eventName == VertexEvent.EVENT_KEY) {

            VertexEvent vdd = blackboard.getData(VertexEvent.EVENT_KEY);


            SubGraph sd = Select.getSelection(blackboard);
            if (sd.vertices.contains(vdd.v))   //start if the vertex selected
            {
                if (vdd.eventType == VertexEvent.DRAGGING_STARTED) {
                    verticesPositionsBackUp = new GraphPoint[gd.getGraph().getVerticesCount()];
                    for (Vertex _ : gd.getGraph()) {
                        verticesPositionsBackUp[_.getId()] = _.getLocation();
                    }
                    startx = vdd.v.getLocation().x;
                    starty = vdd.v.getLocation().y;
                    drag();
                }
                if (vdd.eventType == VertexEvent.RELEASED || vdd.eventType == VertexEvent.DROPPED) {
                    drop();
                    //add undo data
                    GraphPoint[] newPos = new GraphPoint[gd.getGraph().getVerticesCount()];
                    for (Vertex _ : gd.getGraph()) {
                        newPos[_.getId()] = _.getLocation();
                    }

                    blackboard.setData(SELECTION_MOVED, new GraphPoint(vdd.v.getLocation().x - startx, vdd.v.getLocation().y - starty));

                }
                if (vdd.eventType == VertexEvent.DRAGGING) {
//                    VertexMouseDraggingData d = blackboard.get(VertexMouseDraggingData.name);
                    xx = vdd.mousePos.x;
                    yy = vdd.mousePos.y;
                    moveVertices();
                }

            }
        }
    }

    private void mouseMove() {
        GraphEvent ge = blackboard.getData(GraphEvent.EVENT_KEY);
//        GraphMouseMoveData gmmd = blackboard.get(GraphMouseMoveData.name);
        xx = ge.mousePos.x;
        yy = ge.mousePos.y;
        moveVertices();
    }

    private void moveVertices() {
        blackboard.setData("MoveSelected.moving", "yes");
//        System.out.println("mm");
        final SubGraph selection = Select.getSelection(blackboard);
        final double dx = xx - x;
        final double dy = yy - y;
//        System.out.println(xx + ", " + x + ", " + dx);
        AbstractGraphRenderer ren = blackboard.getData(AbstractGraphRenderer.EVENT_KEY);
        ren.ignoreRepaints(new Runnable() {
            public void run() {
                for (Vertex v : selection.vertices) {
                    GraphPoint loc = v.getLocation();
                    v.setLocation(new GraphPoint(loc.x + dx, loc.y + dy));
                }
            }
        });
        blackboard.setData(SELECTION_MOVING, new GraphPoint(dx, dy));
//        blackboard.setData(SELECTION_MOVING, new GraphPoint(vdd.v.getLocation().x - startx, vdd.v.getLocation().y - starty));
    }

    double x;
    double startx;
    double y;
    double starty;
    AbstractGraphRenderer gv;
    Vertex v;

    private void drag() {
        VertexEvent data = blackboard.getData(VertexEvent.EVENT_KEY);
        x = data.mousePos.x;
        y = data.mousePos.y;
        v = data.v;
    }

    private void drop() {
        blackboard.setData("undo point", null);
        blackboard.setData("MoveSelected.moving", "no");

        AbstractGraphRenderer ren = blackboard.getData(AbstractGraphRenderer.EVENT_KEY);
        ren.ignoreRepaints(new Runnable() {
            public void run() {
                VertexEvent ve = blackboard.getData(VertexEvent.EVENT_KEY);
                int _x = (int) ve.mousePos.getX();
                int _y = (int) ve.mousePos.getY();
                int dx = (int) (_x - x);
                int dy = (int) (_y - y);
                x = x + dx;
                y = y + dy;
                SubGraph selection = Select.getSelection(blackboard);
                for (Vertex v : selection.vertices) {
                    try {
                        v.setLocation(new GraphPoint(v.getLocation().x + dx, v.getLocation().y + dy));
                    } catch (InvalidVertexException e) {
                        selection.vertices.remove(v);     //as in java any thing is references, there is no need to update the log in the blackboard :D:D
                        //exception occurs whenever a selected vertex, or edge was deleted by user
//                ExceptionHandler.catchException(e);
                    }
                }

            }
        });


    }

    double xx;
    double yy;

    @Override
    public boolean trackUndos() {
        // the undos are tracked in the drop() method. we don't want to track undos during the move
        return false;
    }
}
