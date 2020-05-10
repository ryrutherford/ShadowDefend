import bagel.*;
import bagel.Image;
import bagel.Window;
import bagel.map.TiledMap;
import bagel.util.Colour;
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
    buyPanel: the background image for the buy panel
    status panel: the background image for the status panel
    cash: the variable that tracks the amount of cash available to the user
    defaultTextFont: the font used in the status panel and for the prices of towers and the key binds in the buy panel
    cashFont: the font used to for the available cash
    wave: tracks which wave the user is on
    status: the string to be displayed to the user indicating the current status of the game
    lives: the number of lives the user has left
    framesPassed: represents the number of theoretical frames that have passed since s was pressed
    sWasPressed: represents whether the user has pressed s or not
    timeScaleMultiplier: represents the speed at which the frame rate should theoretically increase (doesn't actually increase update rate)
    slicerPoints: a list of length 5 that represents the current points on the window of all five slicers
    slicerIndex: a list of length 5 that represents which index in the path list the ith slicer is at
    path: a list of points corresponding to the path along the polylines of the map, each consecutive point is 1px magnitude away
    pathAngle: a list of the angle that the slicer should have, same length as the path list
    lengthOfPath: keeps track of the length of the path list (for convenience)
     */
    private final TiledMap map = new TiledMap("res/levels/1.tmx");
    private final Image slicer = new Image("res/images/slicer.png");
    private final Image buyPanel = new Image("res/images/buypanel.png");
    private final Image statusPanel = new Image("res/images/statuspanel.png");
    private int cash;
    private final Font defaultTextFont = new Font("res/fonts/DejaVuSans-Bold.ttf", 16);
    private final Font cashFont = new Font("res/fonts/DejaVuSans-Bold.ttf", 36);
    private int wave;
    private String status;
    private int lives;
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
        this.timescaleMultiplier = 1;

        //wave is initially 1 because that will be the first wave to come
        this.wave = 1;

        //status is initially awaiting start because that is how the game begins
        this.status = "Awaiting Start";

        //TODO: use the real number of lives once Rohyl confirms it
        this.lives = 25;

        //TODO: convert this statement to use a Path object
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

    //method to draw the buy panel on the screen
    private void drawBuyPanel(){
        buyPanel.drawFromTopLeft(0,0);
        //drawing the purchase items
        new Image("res/images/tank.png").draw(64, 40);
        new Image("res/images/supertank.png").draw(184, 40);
        new Image("res/images/airsupport.png").draw(304, 40);

        //drawing the purchase items price
        this.defaultTextFont.drawString("$250", 44,85, new DrawOptions().setBlendColour(
                this.cash < 250 ? Colour.RED : Colour.GREEN));
        this.defaultTextFont.drawString("$600", 164,85, new DrawOptions().setBlendColour(
                this.cash < 600 ? Colour.RED : Colour.GREEN));
        this.defaultTextFont.drawString("$500", 284,85, new DrawOptions().setBlendColour(
                this.cash < 500 ? Colour.RED : Colour.GREEN));
        //drawing the available cash
        this.cashFont.drawString(String.format("$%,d", cash), Window.getWidth() - 200, 65);

        //drawing the key binds
        this.defaultTextFont.drawString("Key Binds:\nS - Start Wave\nL - Increase Timescale\nK - Decrease Timescale",
                Window.getWidth()/2 - 30, 15);
    }

    private void drawStatusPanel(){
        statusPanel.drawFromTopLeft(0, Window.getHeight() - 25);

        //drawing the wave status
        this.defaultTextFont.drawString("Wave: " + this.wave, 6, Window.getHeight() - 6);

        //drawing the timescale status
        this.defaultTextFont.drawString("Time Scale: " + this.timescaleMultiplier, 256, Window.getHeight() - 6,
                new DrawOptions().setBlendColour(this.timescaleMultiplier <= 1.0 ? Colour.WHITE : Colour.GREEN));

        //drawing the current status
        this.defaultTextFont.drawString("Status: " + this.status, 460, Window.getHeight() - 6);

        //drawing the lives remaining
        this.defaultTextFont.drawString("Lives: " + this.lives, Window.getWidth() - 100, Window.getHeight() - 6);
    }

    @Override
    protected void update(Input input) {

        //drawing the map
        map.draw(0, 0, 0, 0, Window.getWidth(), Window.getHeight());

        drawBuyPanel();
        drawStatusPanel();
        //if the user presses S then we need to start sending out slicers
        if(input.wasPressed(Keys.S)){
            //the sWasPressed attribute will be set to true
            this.sWasPressed = true;
            this.status = "Wave In Progress";
        }
        //if sWasPressed then we start drawing slicers
        if(sWasPressed) {

            //if the user presses l then we need to increase the speed of the game
            if(input.wasPressed(Keys.L)){
                this.timescaleMultiplier++;
            }
            //if the user presses k then we need to decrease the speed of the game as long as the speed is not already 1
            if(input.wasPressed(Keys.K)){
                if(this.timescaleMultiplier != 1)
                    this.timescaleMultiplier--;
            }

            for(int i = 0; i < 5; i++){
                //the path index is the index in the path list that corresponds to the next point for the slicer to be drawn
                int pathIndex = this.slicerIndex.get(i);
                //we call drawSlicer for each slicer
                drawSlicer(pathIndex, i);
            }
            //updating the number of framesPassed.
            // if timeScaleMultiplier is > 1 we "increase" the frame rate to increase the speed
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
