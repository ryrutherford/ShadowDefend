import bagel.*;
import bagel.Image;
import bagel.Window;
import bagel.map.TiledMap;
import bagel.util.Point;
import bagel.util.Vector2;

import java.util.ArrayList;
import java.util.List;


public class ShadowDefend extends AbstractGame {

    //static attributes
    private static final int HEIGHT = 768;
    private static final int WIDTH = 1024;

    //attributes
    /*
    map: the tmx map with the polylines
    slicer: the slicer image
    framesPassed: represents the number of theoretical frames that have passed since s was pressed
    sWasPressed: represents whether the user has pressed s or not
    timeScaleMultiplier: represents the speed at which the frame rate should theoretically increase (doesn't actually increase update rate)
    slicerPoints: a list of length 5 that represents the current points on the window of all five slicers
    slicerIndex: a list of length 5 that represents which index in the path list the ith slicer is at
    path: a list of points corresponding to the path along the polylines of the map, each consecutive point is 1px magnitude away
    pathAngle: a list of the angle that the slicer should have, same length as the path list
    lengthOfPath: keeps track of the length of the path list (for convenience)
     */
    private final TiledMap map;
    private final Image slicer;
    private int framesPassed;
    private boolean sWasPressed;
    private int timescaleMultiplier;
    private List<Point> slicerPoints;
    private List<Integer> slicerIndex;
    private List<Point> path;
    private List<Double> pathAngle;
    private int lengthOfPath;

    //constructor
    public ShadowDefend() {
        super(WIDTH, HEIGHT, "ShadowDefend");
        this.map = new TiledMap("res/levels/2.tmx");
        this.slicer = new Image("res/images/slicer.png");
        this.timescaleMultiplier = 1;

        generatePath(this.map.getAllPolylines().get(0));
        lengthOfPath = this.path.size();

        //initializing the slicer points with the starting point on the map
        this.slicerPoints = new ArrayList<Point>(5);
        this.slicerIndex = new ArrayList<Integer>(5);
        for(int i = 0; i < 5; i++) {
            //initializing each slicer point with the first point on the polyline
            this.slicerPoints.add(this.path.get(0));
            //initializing each slicer index with 0 since that is where each slicer will start on the path
            this.slicerIndex.add(0);
        }
    }

    //a method that will generate a path that traverses the polyline where each consecutive point is 1 pixel apart
    private void generatePath(List<Point> polyline){
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

            //while the distance is more than 1 pixel we will add Points to the path
            while(distance > 1.0){

                //we add v1 to the path as a point
                this.path.add(v1.asPoint());

                //adding the normalized betweenPoints vector to v1
                //we normalize it because then it will have a magnitude of 1 pixel
                Vector2 v1Added = v1.add(betweenPoints.normalised());

                //update v1
                v1 = v1Added;

                //adding the angle to the pathAngle list
                this.pathAngle.add(angle);
                //recalculate the distance based on the updated v1
                distance = Math.sqrt(Math.pow(v1.asPoint().x - v2.asPoint().x, 2) + Math.pow(v1.asPoint().y - v2.asPoint().y, 2));
            }
            //after the while loop we will have a path filled with points 1 pixel of magnitude away that will follow the polyline
        }
    }

    @Override
    protected void update(Input input) {
        map.draw(0, 0, 0, 0, Window.getWidth(), Window.getHeight());

        //if the user presses S then we need to start sending out slicers
        if(input.wasPressed(Keys.S)){
            //the sWasPressed attribute will be set to true
            this.sWasPressed = true;
        }
        //if sWasPressed then we start drawing slicers
        if(sWasPressed) {

            //if the user presses l then we need to double the speed of the game
            if(input.wasPressed(Keys.L)){
                this.timescaleMultiplier *= 2;
            }
            //if the user presses k then we need to halve the speed of the game as long as the speed is not already 1
            if(input.wasPressed(Keys.K)){
                if(this.timescaleMultiplier != 1)
                    this.timescaleMultiplier /= 2;
            }

            for(int i = 0; i < 5; i++){
                //the path index is the index in the path list that corresponds to the next point for the slicer to be drawn
                int pathIndex = this.slicerIndex.get(i);
                //we call drawSlicer for each slicer
                drawSlicer(pathIndex, i);
            }
            //updating the number of framesPassed.
            // if timeScaleMultiplier is > 1 we "double" the frame rate to double the speed
            this.framesPassed += timescaleMultiplier;
        }
    }

    private void drawSlicer(int pathIndex, int i) {

        //after 300*i frames have passed the ith slicer can be drawn
        //the 0th slicer will be drawn immediately the others will have to wait 5*i seconds when timeScaleMultiplier = 1
        //if the pathIndex is > 0 then it means that the slicer has already been drawn once
        if(this.framesPassed >= 300*i || pathIndex > 0){
            //if the path index is equal to -1 then this slicer has traversed the entire path
            if(pathIndex != -1){
                //getting the point coords to draw the slicer
                double x = this.slicerPoints.get(i).x;
                double y = this.slicerPoints.get(i).y;
                slicer.draw(x, y, new DrawOptions().setRotation(this.pathAngle.get(pathIndex)));

                //setting the next point for the slicer to traverse
                this.slicerPoints.set(i, this.path.get(pathIndex));

                //if we've traversed the whole path then we will set the index to -1 and not run this code again
                if(pathIndex + timescaleMultiplier >= this.lengthOfPath) {
                    this.slicerIndex.set(i, -1);
                }
                //otherwise we will set the new slicerIndex to be the current pathIndex + timeScaleMultiplier
                //this means we will traverse at $timeScaleMultiplier px per frame
                else {
                    this.slicerIndex.set(i, pathIndex + timescaleMultiplier);
                }
            }
            //if the pathIndex is -1 and we are on the 4th (last) slicer => all slicers have traversed the entire polyline
            //so we must close the game
            else if(i == 4){
                Window.close();
            }
        }
    }

    //main method
    public static void main(String[] args) throws Exception {
        new ShadowDefend().run();
    }
}
