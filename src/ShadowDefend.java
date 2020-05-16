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
import java.util.*;


/**
 * ShadowDefend class represents an active game of ShadowDefend
 */
public class ShadowDefend extends AbstractGame {

    //static attributes
    private static final int HEIGHT = 768;
    private static final int WIDTH = 1024;

    //attributes
    /*
    map: the tmx map with the polylines
    buyPanel: the background image for the buy panel
    status panel: the background image for the status panel
    defaultTextFont: the font used in the status panel and for the prices of towers and the key binds in the buy panel
    cashFont: the font used to for the available cash
    status: A stack where the top item is to be displayed to the user indicating the current status of the game
    lives: the number of lives the player has remaining
    wave: tracks which wave the user is on
    cash: the variable that tracks the amount of cash available to the user
    framesPassed: represents the number of theoretical frames that have passed since s was pressed
    timeScaleMultiplier: represents the speed at which the frame rate should theoretically increase (doesn't actually increase update rate)
    level: the level the user is on
    sWasPressed: represents whether the user has pressed s or not
    horizontal: indicates whether the next airsupport tower placed should fly horizontal or vertical
    path: a Path object that stores the path along the polylines of the map and the corresponding angle at each point of the path
    slicers: a list of lists of slicers where inner list i contains the slicers and their spawn info for the (i+1)th wave
    purchaseItemBoundingBoxes: the bounding boxes for the purchase items in the buy panel
    gameScreen: the bounding box for the actual game screen (excluding the buy panel and status panel)
    towers: a list of all the towers currently in the game
    towerToBePlaced: the tower object that is to be added to the towers list when a user is placing (buying) a tower
     */
    private TiledMap map = new TiledMap("res/levels/1.tmx");
    private final Image buyPanel = new Image("res/images/buypanel.png"),
            statusPanel = new Image("res/images/statuspanel.png");
    private final Font defaultTextFont = new Font("res/fonts/DejaVuSans-Bold.ttf", 16),
            cashFont = new Font("res/fonts/DejaVuSans-Bold.ttf", 36);
    private Stack<String> status = new Stack<String>();
    private int lives = 25, wave = 1, cash = 5000, framesPassed = 0, timescaleMultiplier = 1, level = 1;
    private boolean sWasPressed = false, horizontal = true;
    private Path path;
    private List<List<Slicer>> slicers;
    private List<Rectangle> purchaseItemBoundingBoxes = new ArrayList<Rectangle>(3);
    private final Rectangle gameScreen = new Rectangle(0, 100, WIDTH, HEIGHT - 125);
    private List<Tower> towers = new ArrayList<Tower>();
    private Tower towerToBePlaced;

    //constructor
    public ShadowDefend(String filename) {
        super(WIDTH, HEIGHT, "ShadowDefend");

        //status is initially awaiting start because that is how the game begins
        this.status.push("Awaiting Start");

        //initializing the purchaseItemBoundingBoxes with 3 rectangles of size 64x64 at the position of each purchase item
        //the first item in the arraylist is the tank's bounding box, then the supertanks's, then the airplane's
        for(int i = 0; i < 3; i++){
            this.purchaseItemBoundingBoxes.add(new Rectangle(32 + i*120, 8, 64,64));
        }

        //creating the path object
        this.path = new Path(this.map.getAllPolylines().get(0), this.gameScreen);

        //initializing the list of slicers from the waves.txt file
        initializeSlicersFromText(filename);
    }

    //method to initialize the slicers list with slicers based on a text file
    private void initializeSlicersFromText(String filename){
        BufferedReader fileReader = null;
        this.slicers = new ArrayList<List<Slicer>>();
        try{
            //the file reader and the current line of the file
            fileReader = new BufferedReader(new FileReader(filename));
            String currentLine;

            //a variable that tracks the delay in FRAMES from delay events
            int delay = 0;

            //while we have not reached the end of file we will use each line in the file to create slicers or update delay
            while((currentLine = fileReader.readLine()) != null){
                //each line in the text file is delimited using a comma
                String[] waveEvent = currentLine.split(",");

                //extracting the wave number and event type
                int wave = Integer.parseInt(waveEvent[0]);
                String eventType = waveEvent[1];

                switch(eventType){
                    //if it is a spawn event, we extract the number of slicers, type of slicer, and the delay between spawns
                    case "spawn":

                        int numSlicers = Integer.parseInt(waveEvent[2]);
                        String slicerType = waveEvent[3];

                        //spawnDelayMS is the spawnDelay in milliseconds. We will convert it to frames when we create the slicer
                        int spawnDelayMS = Integer.parseInt(waveEvent[4]);

                        //we try to get the slicer list for the corresponding wave
                        try{
                            this.slicers.get(wave-1);
                        }
                        //if no slicers have been added for this wave yet we will catch the exception and add a new ArrayList of slicers and reset the delay to 0
                        catch(IndexOutOfBoundsException e){
                            this.slicers.add(new ArrayList<Slicer>());
                            delay = 0;
                        }
                        int i;
                        for(i = 0; i < numSlicers; i++){
                            //the spawnDelayF param of the slicer uses the fact that there are 60fps to convert the spawnDelayMS into frames
                            this.slicers.get(wave-1).add(new Slicer(slicerType, wave, delay + 60*i*spawnDelayMS/1000));
                        }
                        //incrementing the delay value (in frames)
                        delay += 60*(i - 1)*spawnDelayMS/1000;
                        break;
                    case "delay":
                        //updating the delay based on the value in the delay event (waveEvent[2])
                        delay += 60*Integer.parseInt(waveEvent[2])/1000;
                        break;
                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
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

        //if sWasPressed is false then the user has either beat the game or needs to press s to start the next wave
        if(!this.sWasPressed) {
            if(this.lives == 0){
                this.defaultTextFont.drawString("You're out of lives. Press S to Restart",
                        Window.getWidth() / 2 - 30, 85);
            }
            //if the wave is not -1 then the wave exists
            else if(this.wave != -1) {
                this.defaultTextFont.drawString("Press S to Start Wave " + this.wave,
                        Window.getWidth() / 2 - 30, 85);
            }
            //if the wave is -1 then the player beat the game
            else{
                this.defaultTextFont.drawString("You Won! Press S to Restart",
                        Window.getWidth() / 2 - 30, 85);
            }
        }
    }

    //method to draw the status panel on the screen
    private void drawStatusPanel(){
        statusPanel.drawFromTopLeft(0, Window.getHeight() - 25);

        //calling the updateStatus method to handle status changes
        updateStatus();

        //drawing the wave status
        this.defaultTextFont.drawString("Wave: " + (this.wave == -1 ? "N/A" : this.wave), 6, Window.getHeight() - 6);

        //drawing the timescale status
        this.defaultTextFont.drawString("Time Scale: " + this.timescaleMultiplier, 256, Window.getHeight() - 6,
                new DrawOptions().setBlendColour(this.timescaleMultiplier <= 1.0 ? Colour.WHITE : Colour.GREEN));

        //drawing the current status
        this.defaultTextFont.drawString("Status: " + this.status.peek(), 460, Window.getHeight() - 6);

        //drawing the lives remaining
        this.defaultTextFont.drawString("Lives: " + this.lives, Window.getWidth() - 100, Window.getHeight() - 6);
    }

    private void updateStatus(){

        //when the wave is done (sWasPressed == false) and the previous status was Wave In Progress => we need to make the status Awaiting Start
        if(this.status.peek().equals("Wave In Progress") && this.sWasPressed == false){
            this.status.pop();
            if(this.status.peek().equals("Awaiting Start") == false){
                this.status.removeAllElements();
                this.status.push("Awaiting Start");
            }
        }

        //if the wave is -1 then the player has won the game, winner will be displayed
        if(this.wave == -1){
            if(this.status.peek().equals("Winner") == false) {
                this.status.removeAllElements();
                this.status.push("Winner");
            }
        }
    }

    //method to draw all the towers on the game screen
    private void drawTowers(){
        Iterator<Tower> itr = this.towers.iterator();
        while(itr.hasNext()){
            Tower t = itr.next();

            //calling the towers drawTower method
            t.drawTower();

            //if the wave is active then we also need to call the towers attack methods
            //for airsupport the attack method also moves the airsupport across the screen
            if(this.sWasPressed) {
                //if the tower's bounding is not null and the tower is on the game screen we will call its attack method
                //this check is used to make sure airsupport don't continue to attack when they're off the screen
                if(t.getBounding() != null && t.getBounding().intersects(this.gameScreen)) {
                    t.attack(this.slicers.get(this.wave-1), this.timescaleMultiplier);
                }
                //this condition will only be triggered for airsupport since tanks will never have a null bounding or be outside the game screen
                else {
                    //we set the bounding to null because this will prevent any new explosives from being dropped in the attack method
                    t.setBounding(null);

                    //if there are no more explosives left, we remove the tower from the game
                    if(t.getAmmo().size() == 0){
                        itr.remove();
                    }
                    //otherwise we continue to attack with the remaining explosives
                    else{
                        t.attack(this.slicers.get(this.wave-1), this.timescaleMultiplier);
                    }
                }
            }
        }
    }

    //method to draw slicers on the screen
    private boolean drawSlicers() {
        boolean waveDone = true;

        //we use a ListIterator to iterate over the list because we may modify the list during iteration
        ListIterator<Slicer> itr = this.slicers.get(this.wave - 1).listIterator(this.slicers.get(this.wave-1).size());
        while(itr.hasPrevious()){
            Slicer s = itr.previous();

            //if the slicer has started moving (location index > 0) or the spawn delay of the Slicer has passed and the slicer is not dead/finished the path (index != -1) then we can draw the slicer
            if(s.getLocationIndex() > 0 || (s.getSpawnDelayF() <= this.framesPassed && s.getLocationIndex() != - 1)){
                s.drawSlicer(this.timescaleMultiplier, this.path);
                waveDone = false;
            }
            //if there are any slicers with locationIndex == 0 then the wave is not done yet
            else if(s.getLocationIndex() == 0){
                waveDone = false;
            }
            //if the locationIndex is -1, health is greater than 0, and spawnDelay is -1 then the slicer has completed the path and not been eliminated
            else if(s.getLocationIndex() == -1 && s.getHealth() > 0 && s.getSpawnDelayF() == -1){
                //we deduct the penalty from the number of lives
                this.lives -= s.getPenalty();

                //if the lives is less than or equal to 0 after the deduction we set the lives to 0 and return true to indicate the wave is done and the player has lost
                if(this.lives <= 0){
                    this.lives = 0;
                    return true;
                }

                //we set the health to -1 to indicate that the slicer should not spawn any children in the 1st else if condition above
                s.setHealth(-1);
            }

            //if the locationindex is -1 and health is 0 then the slicer has been eliminated so we must add its children to the list
            if(s.getLocationIndex() == -1 && s.getHealth() == 0){
                for(Slicer child: s.getChildren()){
                    itr.add(child);
                }
                //we set the health to -1 so this condition isn't triggered again and add its reward to our cash
                s.setHealth(-1);
                this.cash += s.getReward();
            }
        }
        return waveDone;
    }


    //a method called when the user left clicks anywhere on the screen
    private void selectTower(Input input) {

        //we must first detect which purchase item (if any) was clicked
        int i = 0;
        for(Rectangle r: this.purchaseItemBoundingBoxes){
            //if the user clicked on one of the purchase items, we figure out which one it was and begin placing it if they have enough cash
            if(r.intersects(input.getMousePosition())){
                Tower attackerToBePlaced = null;
                switch(i){
                    //bounding box 0 corresponds to a tank
                    case 0:
                        attackerToBePlaced = new ActiveTower("tank");
                        break;
                    //bounding box 1 corresponds to a supertank
                    case 1:
                        attackerToBePlaced = new ActiveTower("supertank");
                        break;
                    //bounding box 2 corresponds to an airsupport
                    case 2:
                        attackerToBePlaced = new PassiveTower("airsupport");
                        break;
                }
                if(this.cash >= attackerToBePlaced.getPrice()){
                    this.status.push("Placing");
                    this.towerToBePlaced = attackerToBePlaced;
                    break;
                }
            }
            i++;
        }
    }

    //once a tower has been selected, the placeTower method will be called
    //it will draw the tower at the mouse position (if the position is valid for placing a tower)
    //it will place the tower when the user left clicks on a valid position
    //it will remove the towerToBePlaced and stop placing the tower if the user right clicks
    private void placeTower(Input input){
        //checking for valid mouse position for placing towers
        if(!this.map.hasProperty((int)input.getMouseX(), (int)input.getMouseY(), "blocked")
                && this.gameScreen.intersects(input.getMousePosition())
                && !this.mouseIntersectsTower(input.getMousePosition())){

            //drawing the towerToBePlaced at the current mouse position with the proper direction (for airsupport)
            this.towerToBePlaced.getImage().draw(input.getMouseX(),
                    input.getMouseY(),
                    new DrawOptions().setRotation(this.towerToBePlaced.getDirection()));

            //if they press the left button in a valid position, we add the tower
            if(input.wasPressed(MouseButtons.LEFT)){

                //if the towerToBePlaced is airsupport, we need to place it horizontally or vertically
                //otherwise we just place it at the current mouse position
                switch(this.towerToBePlaced.getType()){
                    case "airsupport":
                        //if the tower is to be placed horizontally we place it on the x axis
                        if(this.horizontal){
                            this.towerToBePlaced.setLocation(new Point(0, input.getMouseY()));
                            this.towerToBePlaced.setDirection(Math.PI/2);
                        }
                        //if the tower is to be placed vertically we place it on the y axis
                        else{
                            this.towerToBePlaced.setLocation(new Point(input.getMouseX(), 100));
                            this.towerToBePlaced.setDirection(Math.PI);
                        }
                        this.horizontal = !this.horizontal;
                        break;
                    default:
                        this.towerToBePlaced.setLocation(input.getMousePosition());
                }

                //after the tower has been placed we:
                // deduct the price from the cash,
                // add the tower to the towers list,
                // reset the towerToBePlaced attribute, and
                // update the status
                this.cash -= this.towerToBePlaced.getPrice();
                this.towers.add(this.towerToBePlaced);
                this.towerToBePlaced = null;
                this.status.remove("Placing");
            }
        }
        //if the user right clicks, we reset the towerToBePlaced attribute and remove the Placing status
        if(input.wasPressed(MouseButtons.RIGHT)){
            this.towerToBePlaced = null;
            this.status.remove("Placing");
        }
    }

    //helper method used in the placeTower method to detect whether the current mouse position intersects with a current tower
    private boolean mouseIntersectsTower(Point mousePosition){
        switch(this.towerToBePlaced.getType()){
            case "airsupport":
                if(this.horizontal){
                    if(this.towerToBePlaced.getDirection() != Math.PI/2)
                        this.towerToBePlaced.setDirection(Math.PI/2);
                    for(Tower t : towers){
                        //the null check is used to avoid nullpointerexceptions caused by the null bounding of airsupport outside the game screen
                        if(t.getBounding() != null){
                            if(t.getBounding().intersects(new Point(0, mousePosition.y))){
                                return true;
                            }
                        }
                    }
                }
                else{
                    if(this.towerToBePlaced.getDirection() != Math.PI)
                        this.towerToBePlaced.setDirection(Math.PI);
                    for(Tower t : towers){
                        //the null check is used to avoid nullpointerexceptions caused by the null bounding of airsupport outside the game screen
                        if(t.getBounding() != null){
                            if(t.getBounding().intersects(new Point(mousePosition.x, 100))){
                                return true;
                            }
                        }
                    }
                }
                break;
            default:
                for(Tower t : towers){
                    //the null check is used to avoid nullpointerexceptions caused by the null bounding of airsupport outside the game screen
                    if(t.getBounding() != null){
                        if(t.getBounding().intersects(mousePosition)){
                            return true;
                        }
                    }
                }
                break;
        }
        return false;
    }

    //method to reset the game state (we don't reset the lives because lives will be reset after the user restarts the game
    private void resetState() {
        this.map = new TiledMap("res/levels/1.tmx");
        this.initializeSlicersFromText("res/levels/waves.txt");
        this.path = new Path(this.map.getAllPolylines().get(0), this.gameScreen);
        this.towers = new ArrayList<Tower>();
        this.wave = 1;
        this.level = 1;
        this.cash = 500;
        this.status.removeAllElements();
        this.status.push("Awaiting Start");
    }

    @Override
    protected void update(Input input) {

        //drawing the map
        this.map.draw(0, 0, 0, 0, Window.getWidth(), Window.getHeight());

        if(input.wasPressed(Keys.S)){
            //if the user presses S, and sWasPressed is false and the wave is not -1 then we need to start sending out slicers for the current wave
            if(this.wave != -1 && this.sWasPressed == false) {

                //if lives equals 0 then the player died and wants to restart the game so we must reset the lives
                if(this.lives == 0){
                    this.lives = 25;
                }

                this.sWasPressed = true;

                //if the player is currently placing a tower we will push Wave In Progress below it in the stack
                //this way when the user is no longer placing a tower, wave in progress will be displayed
                if(this.status.peek().equals("Placing")){
                    this.status.pop();
                    this.status.push("Wave In Progress");
                    this.status.push("Placing");
                }
                //otherwise we just update the status to be wave in progress
                else {
                    this.status.push("Wave In Progress");
                }
            }
            else if(this.wave == -1){
                resetState();
                this.lives = 25;
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

            //drawing the slicers, the drawSlicers() method returns a boolean that indicates whether the wave is done
            boolean waveDone = drawSlicers();

            //updating the number of framesPassed.
            // if timeScaleMultiplier is > 1 we "increase" the frame rate to increase the speed
            this.framesPassed += timescaleMultiplier;

            //if the wave is done = true => the game play stops
            if(waveDone){
                //if the lives = 0 then we reset the game, but keep the lives at 0
                //the user will have to press s to start from the beginning again (and then lives will be reset to 25)
                if(this.lives == 0){
                    resetState();
                }
                //if the user is still alive we need to load the next wave
                else {
                    this.wave++;
                    //removing all ammo from towers and removing airsupport
                    for(Tower t: this.towers){
                        t.setAmmo(new ArrayList<Ammo>());

                        //any active airsupport from the previous wave shouldn't be drawn anymore so we set the bounding to null
                        if(t.getType().equals("airsupport")){
                            t.setBounding(null);
                        }
                    }
                    //if we have run all waves, then we increase the level and try to load the next map
                    if (this.wave > this.slicers.size()) {
                        this.level++;
                        //try to open a map for the corresponding level, if it doesn't exist then the player has won the game
                        try {
                            this.map = new TiledMap("res/levels/" + this.level + ".tmx");
                            this.initializeSlicersFromText("res/levels/waves.txt");
                            this.path = new Path(this.map.getAllPolylines().get(0), this.gameScreen);
                            this.towers = new ArrayList<Tower>();
                            this.wave = 1;
                            this.cash = 500;
                            this.towerToBePlaced = null;
                        } catch (Exception e) {
                            //setting the wave to -1 indicates that the player has won the game
                            this.wave = -1;
                            this.towerToBePlaced = null;
                        }
                    }
                }
                this.sWasPressed = false;
                this.timescaleMultiplier = 1;
                this.framesPassed = 0;
            }
        }

        //if the left mouse button was pressed and the game is not over
        //then we must call the selectTower method which will show the tower at the mouse position
        if(input.wasPressed(MouseButtons.LEFT) && this.status.peek().equals("Winner") == false && this.towerToBePlaced == null)
            selectTower(input);

        if(this.towerToBePlaced != null)
            placeTower(input);

        //drawing the buy panel, status panel, and towers
        drawBuyPanel();
        drawStatusPanel();
        drawTowers();
    }

    //main method
    public static void main(String[] args) throws Exception {
        new ShadowDefend("res/levels/waves.txt").run();
    }
}
