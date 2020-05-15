import bagel.util.Point;
import bagel.util.Rectangle;
import bagel.util.Vector2;
import java.util.ArrayList;
import java.util.List;

/**
 * Path class represents the path along the polylines of a map and the corresponding direction angle in radians at each point
 */
public class Path {
    //path: a list of all the points in the path (each point is 0.25px apart)
    //pathAngle: a list of the direction angle at each point in the path
    //the length of the path (and pathAngle) lists
    //gameScreen: a rectangle that represents the game screen (screen without the panels)
    private ArrayList<Point> path;
    private ArrayList<Double> pathAngle;
    private int pathLength;
    private Rectangle gameScreen;

    public Path(List<Point> polyline, Rectangle gameScreen){
        this.gameScreen = gameScreen;
        generatePath(polyline);
        this.pathLength = path.size();
    }

    //method to generate a path based on a polyline
    public void generatePath(List<Point> polyline){
        this.path = new ArrayList<>();
        this.pathAngle = new ArrayList<>();

        //iterate over all the points in the polyline up until the second last point
        for(int i = 0; i < polyline.size() - 1; i++){
            //getting the first point and the point after it in the polyline
            Point p1 = polyline.get(i);
            Point p2 = polyline.get(i+1);

            //turning these points into vectors respectively
            Vector2 v1 = new Vector2(p1.x, p1.y);
            Vector2 v2 = new Vector2(p2.x, p2.y);

            //finding the vector between the two points using vector subtraction
            Vector2 betweenPoints = v2.sub(v1);

            //unit vector pointing east
            Vector2 unitEast = new Vector2(1, 0);
            Vector2 unitDir = betweenPoints.normalised();

            //to compute the angle that the slicer should be facing:
            //if the y coord of the betweenPoints vector is negative => find the angle between unitEast and unitDir and flip the sign
            //otherwise just find the angle between unitEat and unitDir
            double angle = unitDir.y < 0 ? -Math.acos(unitEast.dot(unitDir)) : Math.acos(unitEast.dot(unitDir));

            //we will add the normalized vector betweenPoints (v2 - v1) to v1 repeatedly until we have reached sufficiently close to v2 (within 1 pixel)
            //we will know when we are within sufficient distance by calculating the distance between v1 and v2 as follows
            double distance = Math.sqrt(Math.pow(v1.asPoint().x - v2.asPoint().x, 2) + Math.pow(v1.asPoint().y - v2.asPoint().y, 2));

            //while the distance is more than 0.25 pixels we will add Points to the path
            while(distance > 0.25){

                //we add v1 to the path as a point and the corresponding angle
                //NOTE: I added this check to make it so points are only added if they are on the game screen. This prevents slicers from travelling over panels
                if(this.gameScreen.intersects(v1.asPoint())) {
                    this.path.add(v1.asPoint());
                    this.pathAngle.add(angle);
                }

                //adding the betweenPoints vector with 1/4 of its original length to v1
                //we divide it by 4 times its original length because then it will have a magnitude of 0.25 pixels
                //Vector2 v1Added = v1.add(betweenPoints.normalised());
                Vector2 v1Added = v1.add(betweenPoints.div(betweenPoints.length()*4));

                //update v1
                v1 = v1Added;

                //recalculate the distance based on the updated v1
                distance = Math.sqrt(Math.pow(v1.asPoint().x - v2.asPoint().x, 2) + Math.pow(v1.asPoint().y - v2.asPoint().y, 2));
            }
            //after the while loop we will have a path filled with points 1 pixel of magnitude away that will follow the polyline
        }
    }

    //Getters

    public ArrayList<Point> getPath() { return path; }

    public ArrayList<Double> getPathAngle() { return pathAngle; }

    public int getPathLength(){ return pathLength; }
}
