import bagel.*;
import bagel.Image;
import bagel.Window;
import bagel.map.TiledMap;
import bagel.util.Colour;
import bagel.util.Point;
import bagel.util.Rectangle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
    private List<Slicer> slicers;
    private Path path;
    private int level;
    private List<Rectangle> purchaseItemBoundingBoxes;
    private final Rectangle gameScreen = new Rectangle(0, 100, WIDTH, HEIGHT - 25);
    private List<Attacker> towers;
    private Attacker towerToBePlaced;

    //constructor
    public ShadowDefend(String filename) {
        super(WIDTH, HEIGHT, "ShadowDefend");
        this.timescaleMultiplier = 1;

        //wave is initially 1 because that will be the first wave to come
        this.wave = 1;

        //level is initially 1 because that will be the first level
        this.level = 1;

        //status is initially awaiting start because that is how the game begins
        this.status = "Awaiting Start";

        //the user starts with 25 lives
        this.lives = 25;

        //the user starts with $500
        this.cash = 500;

        //initializing the purchaseItemBoundingBoxes with 3 rectangles of size 64x64 at the position of each purchase item
        //the first item in the arraylist is the tank's bounding box, then the supertanks's, then the airplane's
        this.purchaseItemBoundingBoxes = new ArrayList<Rectangle>(3);
        for(int i = 0; i < 3; i++){
            this.purchaseItemBoundingBoxes.add(new Rectangle(32 + i*120, 8, 64,64));
        }

        this.towers = new ArrayList<Attacker>();

        //creating the path object
        this.path = new Path(this.map.getAllPolylines().get(0));

        //initializing the list of slicers from the waves.txt file
        initializeSlicersFromText(filename);
    }

    //method to initialize the slicers list with slicers based on a text file
    private void initializeSlicersFromText(String filename){
        BufferedReader fileReader = null;
        this.slicers = new ArrayList<Slicer>();
        try{
            //the file reader and the current line of the file
            fileReader = new BufferedReader(new FileReader(filename));
            String currentLine;

            //a variable that tracks the delay in FRAMES from delay events
            int delay = 0;

            //while we have not reached the end of file we will use each line in the file to create slicers or update delay
            while((currentLine = fileReader.readLine()) != null){
                String[] waveEvent = currentLine.split(",");
                int wave = Integer.parseInt(waveEvent[0]);
                String eventType = waveEvent[1];
                switch(eventType){
                    case "spawn":
                        int numSlicers = Integer.parseInt(waveEvent[2]);
                        String slicerType = waveEvent[3];
                        //spawnDelayMS is the spawnDelay in milliseconds. We will convert it to frames when we create the slicer
                        int spawnDelayMS = Integer.parseInt(waveEvent[4]);
                        int i;
                        for(i = 0; i < numSlicers; i++){
                            this.slicers.add(new Slicer(slicerType, wave, delay + 60*i*spawnDelayMS/1000));
                        }
                        delay += 60*(i - 1)*spawnDelayMS/1000;
                        break;
                    case "delay":
                        //updating the delay based on the value in the delay event
                        delay += 60*Integer.parseInt(waveEvent[2])/1000;
                        break;
                }
            }
        }
        catch(IOException e){
            this.level = - 1;
        }
        finally{
            try{
                if(fileReader != null) fileReader.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
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

        //if sWasPressed is false then the user has either beat the game or needs to press s to start the next level
        if(!this.sWasPressed) {
            //if the level is not -1 then the level exists
            if(this.level != -1) {
                this.defaultTextFont.drawString("Press S to Start Level " + this.level,
                        Window.getWidth() / 2 - 30, 85);
            }
            else{
                this.defaultTextFont.drawString("Congratulations. You Beat the Game!",
                        Window.getWidth() / 2 - 30, 85);
            }
        }
    }

    //method to draw the status panel on the screen
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

    private void drawTowers(){
        for(Attacker a: this.towers){
            a.getImage().draw(a.getLocation().x, a.getLocation().y);
        }
    }

    private boolean drawSlicers() {
        boolean levelDone = true;
        for(Slicer s: this.slicers){
            if((s.getSpawnDelayF() <= this.framesPassed && s.getLocationIndex() != - 1) || s.getLocationIndex() > 0){
                if(s.getWave() > this.wave){
                    this.wave = s.getWave();
                }
                s.drawSlicer(this.timescaleMultiplier, this.path);
                levelDone = false;
            }
        }
        return levelDone;
    }

    private void selectTower(Input input) {
        int i = 0;
        for(Rectangle r: this.purchaseItemBoundingBoxes){
            Attacker attackerToBePlaced = null;
            switch(i){
                case 0:
                    attackerToBePlaced = new ActiveTower("tank");
                    break;
                case 1:
                    attackerToBePlaced = new ActiveTower("supertank");
                    break;
                case 2:
                    attackerToBePlaced = new ActiveTower("airsupport");
                    break;
            }
            if(r.intersects(input.getMousePosition()) && this.cash >= attackerToBePlaced.getPrice()){
                this.status = "Placing";
                this.towerToBePlaced = attackerToBePlaced;
                break;
            }
            i++;
        }
    }

    private void placeTower(Input input){
        if(!this.map.hasProperty((int)input.getMouseX(), (int)input.getMouseY(), "blocked")
                && this.gameScreen.intersects(input.getMousePosition())
                && !this.mouseIntersectsTower(input.getMousePosition())){
            this.towerToBePlaced.getImage().draw(input.getMouseX(), input.getMouseY());
            //if they press the left button in a valid position, we add the tower
            if(input.wasPressed(MouseButtons.LEFT)){
                this.cash -= this.towerToBePlaced.getPrice();
                this.towerToBePlaced.setLocation(input.getMousePosition());
                this.towers.add(this.towerToBePlaced);
                //TODO: implement the proper procedures for updating status
                this.towerToBePlaced = null;
                this.status = "Awaiting Start";
            }
        }
        if(input.wasPressed(MouseButtons.RIGHT)){
            this.towerToBePlaced = null;
            //TODO: implement the proper procedures for updating status
            this.status = "Awaiting Start";
        }
    }

    private boolean mouseIntersectsTower(Point mousePosition){
        for(Attacker a : towers){
            if(a.getBounding().intersects(mousePosition)){
                return true;
            }
        }
        return false;
    }

    @Override
    protected void update(Input input) {

        //drawing the map
        this.map.draw(0, 0, 0, 0, Window.getWidth(), Window.getHeight());

        //drawing the buy panel, status panel, and towers
        drawBuyPanel();
        drawStatusPanel();
        drawTowers();

        if(input.wasPressed(Keys.S)){
            //if the user presses S and the level is not -1 then we need to start sending out slicers for the current level
            if(level != -1) {
                this.sWasPressed = true;
                this.status = "Wave In Progress";
            }
            //if the level is -1 then the user beat the game so we will restart from level 1
            else{
                this.initializeSlicersFromText("res/levels/waves.txt");
                this.level = 1;
                this.status = "Wave In Progress";
                this.sWasPressed = true;
            }
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

            //drawing the slicers, the drawSlicers() method returns a boolean that indicates whether the level is done
            boolean levelDone = drawSlicers();

            //updating the number of framesPassed.
            // if timeScaleMultiplier is > 1 we "increase" the frame rate to increase the speed
            this.framesPassed += timescaleMultiplier;

            //if the level is done, we set sWasPressed to false, increase the level and load its corresponding slicers
            if(levelDone){
                this.level++;
                this.sWasPressed = false;
                this.timescaleMultiplier = 1;
                this.framesPassed = 0;
                this.initializeSlicersFromText("res/levels/waves" + this.level + ".txt");
            }
        }

        //if the left mouse button was pressed then we must call the selectTower method which will show the tower at the mouse position
        if(input.wasPressed(MouseButtons.LEFT))
            selectTower(input);

        if(this.status.equals("Placing"))
            placeTower(input);
    }

    //main method
    public static void main(String[] args) throws Exception {
        new ShadowDefend("res/levels/waves.txt").run();
    }
}
