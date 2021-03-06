package pl.edu.amu.wmi.min.torcs.fcl;
import java.util.List;
import net.sourceforge.cig.torcs.Controller;
import net.sourceforge.cig.torcs.Action;
import net.sourceforge.cig.torcs.SensorModel;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.Gpr;
import org.jgap.InvalidConfigurationException;
import org.jgap.UnsupportedRepresentationException;
import org.jgap.data.DataTreeBuilder;
import org.jgap.data.IDataCreators;
import org.jgap.xml.GeneCreationException;
import org.jgap.xml.ImproperXMLException;
import org.jgap.xml.XMLDocumentBuilder;
import org.jgap.xml.XMLManager;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class padDriver extends Controller {

    private Double trackLength;

    enum InputType{
        fuzzyControl, pad;
    }
    
    double ABS_MINSPEED = 3.0;    /* [m/s] */
    double ABS_SLIP = 0.9;
    
    double TCL_SLIP = 0.9;        /* [-] range [0.95..0.3] */
    double TCL_MINSPEED = 3.0;    /* [m/s] */
    
    int FRONT_RIGHT = 0;
    int FRONT_LEFT = 1;
    int REAR_RIGHT = 2;
    int REAR_LEFT = 3;

    
    /*GENETIC SHIT*/
    public static volatile int generationNumber = 0;
    public static volatile int unitNumber = 0;
    private GeneticAlgorithm geneticAlg;
    double bestScoreSoFar = 0.0f;
    
    
    InputType steeringInputType = InputType.fuzzyControl;
    InputType accelerationInputType = InputType.fuzzyControl;
    boolean useSpeedLimiter = true;
        
        
    boolean fuzzyAccelControllerAlwaysGasOn = true;
    
    double counter = 0.0f;
    double distancesFromRacingLineSum = 0.0f;
    double squareDistancesFromRacingLineSum = 0.0f;
    double safeDistanceFromEdgeSum = 0.0f;
    int lapsDone = 0;
    boolean useFuzzyDriverSteering = true;
    boolean useFuzzyDriverAcceleration = true;
    boolean automaticGear = true;
    boolean useBot = false;
    boolean firstLoop;
    double roadWidth = 0.0f;
    int racingLineIndex = 0;
    boolean raceFinished = false;

    Set<String> variablesInUseSteering;
    Set<String> variablesInUseAccel; 
    //boolean pulse;
    //boolean simulationOver;
    net.java.games.input.Controller pad;
    private final float[] angles;
    private List<Point2D.Double> points;
    /*
    private  int[] gearUp = {9000, 8000, 8000, 8000, 8000, 0};
    private  int[] gearDown = {0, 2500, 3000, 3000, 3500, 3500};
    */
    private int steeringCommand, accelerationCommand = 0;
    private int padGasPressed, padBrakePressed = 0;
    private int padSteering, padAcceleration = 0;
    private int padGear = 0;
    private int fuzzyControllerSteering, fuzzyControllerAcceleration = 0;
    private float smoothedSteering = 0.0f;
    private RacingLine racingLine;
    private GearPreference gearPreference;
    
    FIS steeringFis;
    FIS accelFis;
    FIS gearFis;
    
    Action toReturn;
    Action emptyAction;
    
    double currentFrameTime, lastFrameTime;
    
    Visualisation visualisation;
    
    DecimalFormat doubleFormatter;  
    
    public padDriver(FIS strFis, FIS accFis, FIS grFis, String fileName) throws InvalidConfigurationException, ParserConfigurationException, ImproperXMLException, UnsupportedRepresentationException, GeneCreationException, SAXException, IOException {
        
        resetState();
        
        setFIS(strFis, accFis, grFis);
        
        geneticAlg = new GeneticAlgorithm(steeringFis, fileName);
        new Thread(new GeneticThread()).start();
        
        doubleFormatter = new DecimalFormat("0.0000");
        DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
        sym.setDecimalSeparator('.');
        doubleFormatter.setDecimalFormatSymbols(sym);

        visualisation = new Visualisation();
        
        toReturn = new Action();
        emptyAction = new Action();
        /*
        net.java.games.input.Controller[] controllers = net.java.games.input.ControllerEnvironment.getDefaultEnvironment().getControllers();
        
        if(controllers.length==0) {
            System.out.println("Found no controllers.");
            System.exit(0);
        }

        for (net.java.games.input.Controller controller : controllers) {
            if (controller.getType() == net.java.games.input.Controller.Type.GAMEPAD) {
                pad = controller;
            }
        }
        */
        
        this.angles = new float[19];
        /* set angles as {-90,-75,-60,-45,-30,-20,-15,-10,-5,0,5,10,15,20,30,45,60,75,90} */
        for (int i = 0; i < 5; i++) {
            angles[i] = -90 + i * 15;
            angles[18 - i] = 90 - i * 15;
        }
        
        for (int i = 5; i < 9; i++) {
            angles[i] = -20 + (i - 5) * 5;
            angles[18 - i] = 20 - (i - 5) * 5;
        }
        angles[9] = 0;
        
        
        points = new ArrayList<>();

        System.out.println(""
                + "track0,track1,track2,"
                + "track3,track4,track5,"
                + "track6,track7,track8,"
                + "track9,"
                + "track10,track11,track12,"
                + "track13,track14,track15,"
                + "track16,track17,track18,"
                + "racingLine0,"
                + "racingLine5,racingLine10,"
                + "racingLine15,racingLine20,"
                + "racingLine25,racingLine30,"
                + "leftWall0,leftWall5,leftWall10,leftWall15,leftWall20,leftWall25,leftWall30,"
                + "rightWall0,rightWall5,rightWall10,rightWall15,rightWall20,rightWall25,rightWall30,"
                + "trackPosition,"
                + "maxTrackEdge,"
                + "speed,"
                + "sideSpeed,"
                + "rpm,"
                + "gear,"
                + "distanceFromStartLine,"
                + "angle,"
                + "distanceFromRacingLine,"
                + "outOfTrack,"
                + "meanCurve,"
                + "trackBBoxWidth,"
                + "trackBBoxHeigth,"
                + "trackVisibility,"
                + "unknownSteeringSituation,"
                + "unknownAccelerationSituation,"
                + "inputSteering,"
                + "steering,"
                + "inputAcceleration,"
                + "speedLimit,"
                + "gas,"
                + "brake,"
                //+ "gear"
        );

    }
    
    
    @Override
    public Action control(SensorModel sensors) {
        
            if(raceFinished){
                return emptyAction;
            }
        

            if(geneticAlg.fitFunc.state != TorcsFitnessFunction.CurrentState.TestRide){
                synchronized(geneticAlg.fitFunc.state){
                    if(geneticAlg.fitFunc.state == TorcsFitnessFunction.CurrentState.ReadyForTestRide){
                        geneticAlg.fitFunc.state = TorcsFitnessFunction.CurrentState.TestRide;
                    }
                    else{
                        return emptyAction;
                    }
                }
            }
            
            
            counter++;

            if(firstLoop){
                double[] edges = sensors.getTrackEdgeSensors();

                roadWidth = edges[0] + edges[edges.length-1];
                firstLoop = false;
            }

            double[] trackEdge = sensors.getTrackEdgeSensors();

            double maxTrackEdge = -5.0f;
            double minTrackEdge = 201.0f;

            for (int i = 0; i < trackEdge.length; i++) {
                if(trackEdge[i] > maxTrackEdge){
                    maxTrackEdge = trackEdge[i];
                }
                if(trackEdge[i] < minTrackEdge){
                    minTrackEdge = trackEdge[i];
                }
            }

            points.clear();

            //computing x,y points
            double radians;
            for (int i = 0; i < trackEdge.length; i++) {
                radians = Math.toRadians(angles[i]);
                points.add(new Point2D.Double(trackEdge[i] * Math.sin(radians), trackEdge[i] * Math.cos(radians)));
            }



            //trying to realize, which point belongs to which band
            ArrayList<Point2D.Double> leftPoints = new ArrayList<>();
            ArrayList<Point2D.Double> rightPoints = new ArrayList<>();

            double angleLimit = 3*Math.PI/4.0f;

            for (int i = 0; i < points.size(); i++) {
                if(trackEdge[i] < 199.9f
                        && (i == 0 || Point2D.distance(points.get(i).x, points.get(i).y, points.get(i-1).x, points.get(i-1).y) <= roadWidth * 1.5f)
                        //&& (i == 0 || borderAngles.get(i-1) > angleLimit)
                )
                {
                    leftPoints.add(points.get(i));
                }
                else{
                    break;
                }
            }

            for (int i = points.size()-1; i >= leftPoints.size(); i--) {
                if (trackEdge[i] < 199.9f 
                        && (i == points.size()-1 || Point2D.distance(points.get(i).x, points.get(i).y, points.get(i+1).x, points.get(i+1).y) <= roadWidth)
                       // && (i == points.size()-1 || borderAngles.get(i+1) > angleLimit)
                )
                {
                    rightPoints.add(points.get(i));
                    //if (i >= points.size() && borderAngles.get(i+1) > angleLimit){
                    //    leftPoints.remove(leftPoints.size()-1);
                    //}
                }
                else{
                    break;
                }
            }


            double leftLength = 0.0f;
            double rightLength = 0.0f;

            for (int i = 1; i < leftPoints.size(); i++) {
                leftLength += Point2D.distance(leftPoints.get(i).x, leftPoints.get(i).y, leftPoints.get(i-1).x, leftPoints.get(i-1).y);
            }

            for (int i = 1; i < rightPoints.size(); i++) {
                rightLength += Point2D.distance(rightPoints.get(i).x, rightPoints.get(i).y, rightPoints.get(i-1).x, rightPoints.get(i-1).y);
            }

            //extending points
            boolean left = leftLength > rightLength;
            ArrayList<Point2D.Double> estimatedSet = new ArrayList<>();




            //estimating right side
            if(left){

                //ArrayList<Point2D.Double> newLeftPoints = new ArrayList<>();

                leftPoints = ChangeLineResoultion(leftPoints, 2.0f);

                if(leftPoints.size() >= 2){
                    double firstPointAngle = -Math.atan2(leftPoints.get(1).y - leftPoints.get(0).y, leftPoints.get(1).x - leftPoints.get(0).x);
                    Point2D.Double firstPoint = new Point2D.Double();
                    firstPoint.x = -roadWidth * Math.sin(firstPointAngle) + leftPoints.get(0).x;
                    firstPoint.y = -roadWidth * Math.cos(firstPointAngle) + leftPoints.get(0).y;

                    estimatedSet.add(firstPoint);
                }

                for (int i = 1; i < leftPoints.size()-1; i++) {

                    Point2D.Double oppositePoint = new Point2D.Double();

                    double bisectorAngle;
                    double ab = Math.atan2(leftPoints.get(i+1).y - leftPoints.get(i).y, leftPoints.get(i+1).x - leftPoints.get(i).x);
                    double bc = Math.atan2(leftPoints.get(i).y - leftPoints.get(i-1).y, leftPoints.get(i).x - leftPoints.get(i-1).x);
                    bisectorAngle = (ab + bc) / 2.0f;
                    bisectorAngle = -bisectorAngle;

                    oppositePoint.x = -roadWidth * Math.sin(bisectorAngle) + leftPoints.get(i).x;
                    oppositePoint.y = -roadWidth * Math.cos(bisectorAngle) + leftPoints.get(i).y;

                    estimatedSet.add(oppositePoint);
                }

                if(leftPoints.size() >= 2){
                    double lastPointAngle = -Math.atan2(leftPoints.get(leftPoints.size()-1).y - leftPoints.get(leftPoints.size()-2).y,
                    leftPoints.get(leftPoints.size()-1).x - leftPoints.get(leftPoints.size()-2).x);
                    Point2D.Double lastPoint = new Point2D.Double();
                    lastPoint.x = -roadWidth * Math.sin(lastPointAngle) + leftPoints.get(leftPoints.size()-1).x;
                    lastPoint.y = -roadWidth * Math.cos(lastPointAngle) + leftPoints.get(leftPoints.size()-1).y;

                    estimatedSet.add(lastPoint);
                }

                rightPoints = estimatedSet;

            }






            //estimating left side
            else{

                rightPoints = ChangeLineResoultion(rightPoints, 2.0f);

                if(rightPoints.size() >= 2){
                    double firstPointAngle = -Math.atan2(rightPoints.get(1).y - rightPoints.get(0).y, rightPoints.get(1).x - rightPoints.get(0).x);
                    Point2D.Double firstPoint = new Point2D.Double();
                    
                    firstPoint.x = roadWidth * Math.sin(firstPointAngle) + rightPoints.get(0).x;
                    firstPoint.y = roadWidth * Math.cos(firstPointAngle) + rightPoints.get(0).y;

                    estimatedSet.add(firstPoint);
                }


                for (int i = 1; i < rightPoints.size()-1; i++) {

                    Point2D.Double oppositePoint = new Point2D.Double();

                    double bisectorAngle;

                    double ab = Math.atan2(rightPoints.get(i+1).y - rightPoints.get(i).y, rightPoints.get(i+1).x - rightPoints.get(i).x);
                    double bc = Math.atan2(rightPoints.get(i).y - rightPoints.get(i-1).y, rightPoints.get(i).x - rightPoints.get(i-1).x);
                    bisectorAngle = (ab + bc) / 2.0f;
                    bisectorAngle = -bisectorAngle;

                    oppositePoint.x = roadWidth * Math.sin(bisectorAngle) + rightPoints.get(i).x;
                    oppositePoint.y = roadWidth * Math.cos(bisectorAngle) + rightPoints.get(i).y;

                    estimatedSet.add(oppositePoint);

                }

                if(rightPoints.size() >= 2){
                    double lastPointAngle = -Math.atan2(rightPoints.get(rightPoints.size()-1).y - rightPoints.get(rightPoints.size()-2).y,
                        rightPoints.get(rightPoints.size()-1).x - rightPoints.get(rightPoints.size()-2).x);
                    Point2D.Double lastPoint = new Point2D.Double();
                    lastPoint.x = roadWidth * Math.sin(lastPointAngle) + rightPoints.get(rightPoints.size()-1).x;
                    lastPoint.y = roadWidth * Math.cos(lastPointAngle) + rightPoints.get(rightPoints.size()-1).y;

                    estimatedSet.add(lastPoint);
                }

                leftPoints = estimatedSet;

            }

            ArrayList<Double> leftPointsX = new ArrayList<>();

            for (int i = 0; i < 31; i+=5) {
                if (i < leftPoints.size() ){

                    boolean found = false;
                    for (int j = 0; j < leftPoints.size(); j++) {
                        if(leftPoints.get(j).y >= i){
                            leftPointsX.add(leftPoints.get(i).x);
                            found = true;
                            break;
                        }
                    }
                    if (!found){
                        if (leftPointsX.isEmpty()){
                            leftPointsX.add(new Double(0.0f));
                        }
                        else{
                            leftPointsX.add(leftPointsX.get(leftPointsX.size()-1));
                        }
                    }

                }
                else if (leftPointsX.size() >= 2){
                    leftPointsX.add(leftPointsX.get(leftPointsX.size()-1) + (leftPointsX.get(leftPointsX.size()-1) - leftPointsX.get(leftPointsX.size()-2)));
                }
                else if (leftPointsX.size() == 1){
                    leftPointsX.add(leftPointsX.get(leftPointsX.size()-1));
                }
                else {
                    leftPointsX.add(new Double(0.0f));
                }
            }



            ArrayList<Double> rightPointsX = new ArrayList<>();

            for (int i = 0; i < 31; i+=5){
                if (i < rightPoints.size()){

                    boolean found = false;
                    for (int j = 0; j < rightPoints.size(); j++){
                        if(rightPoints.get(j).y >= i){
                            rightPointsX.add(rightPoints.get(i).x);
                            found = true;
                            break;
                        }
                    }
                    if (!found){
                        if (rightPointsX.isEmpty()){
                            rightPointsX.add(new Double(0.0f));
                        }
                        else{
                            rightPointsX.add(rightPointsX.get(rightPointsX.size()-1));
                        }
                    }

                }
                else if (rightPointsX.size() >= 2){
                    rightPointsX.add(rightPointsX.get(rightPointsX.size()-1) + (rightPointsX.get(rightPointsX.size()-1) - rightPointsX.get(rightPointsX.size()-2)));
                }
                else if (rightPointsX.size() == 1){
                    rightPointsX.add(rightPointsX.get(rightPointsX.size()-1));
                }
                else {
                    rightPointsX.add(new Double(0.0f));
                }
            }



            //putting racing line on the road
            double distanceFromStartLine = sensors.getDistanceFromStartLine();

            ArrayList<Point2D.Double> racingLinePoints = new ArrayList<>();
            ArrayList<Point2D.Double> centerLinePoints = new ArrayList<>();
            double distanceFromCar = 0.0f;

            double previousCenterOfPointX = 0.0f;
            double previousCenterOfPointY = 0.0f;

            double distanceFromRacingLine = sensors.getTrackPosition() - racingLine.GetFirstPointAfter(distanceFromStartLine);
            double actualDistanceFromRacingLine = distanceFromRacingLine * (roadWidth/2.0f);

            double radAngle = sensors.getAngleToTrackAxis();
            double distanceToTrackCenter = sensors.getTrackPosition() * (roadWidth/2.0f);

            centerLinePoints.add(new Point2D.Double(distanceToTrackCenter * Math.cos(radAngle), distanceToTrackCenter * Math.sin(radAngle)));
            racingLinePoints.add(new Point2D.Double(actualDistanceFromRacingLine * Math.cos(radAngle), actualDistanceFromRacingLine * Math.sin(radAngle)));

            //setting up a racing line
            if(leftPoints.size() == rightPoints.size()){

                previousCenterOfPointX = centerLinePoints.get(0).x;
                previousCenterOfPointY = centerLinePoints.get(0).y;

                for (int i = 0; i < leftPoints.size(); i++) {

                    double centerOfPointX = (rightPoints.get(i).x + leftPoints.get(i).x) / 2.0f;
                    double centerOfPointY = (rightPoints.get(i).y + leftPoints.get(i).y) / 2.0f;

                    if(rightPoints.get(i).y <= 2.0f || leftPoints.get(i).y <= 2.0f){
                        continue;
                    }

                    centerLinePoints.add(new Point2D.Double(centerOfPointX, centerOfPointY));

                    distanceFromCar += Point2D.distance(previousCenterOfPointX, previousCenterOfPointY, centerOfPointX, centerOfPointY);

                    previousCenterOfPointX = centerOfPointX;
                    previousCenterOfPointY = centerOfPointY;

                    double racingLinePosition = racingLine.GetFirstPointAfter(distanceFromStartLine + distanceFromCar);
                    racingLinePoints.add(
                            new Point2D.Double(
                                leftPoints.get(i).x + (rightPoints.get(i).x - leftPoints.get(i).x) * (1.0f-((racingLinePosition+1.0f)/2.0f)),
                                leftPoints.get(i).y + (rightPoints.get(i).y - leftPoints.get(i).y) * (1.0f-((racingLinePosition+1.0f)/2.0f))));
                }

                racingLinePoints = ChangeLineResoultion(racingLinePoints, 2.0f);

            }
            else{
                System.err.println("nierowne krawedzie!");
            }


            //double meanCurve = anglesChangeSum/centerLineLength;
            double meanCurve = 0.0f;


            visualisation.Redraw(leftPoints, rightPoints, racingLinePoints);     


            double angle = sensors.getAngleToTrackAxis() * (180.0f/Math.PI);
            double speed = sensors.getSpeed();
            double sideSpeed = sensors.getLateralSpeed();
            double trackPosition = sensors.getTrackPosition();
            int outOfTrack = trackEdge[0] < 0.1f ? 1 : 0;






            /**********************************************/
            //      Here is place for temporary hacks     //
            /**********************************************/

    /*
            int racingLineindex = 5 + new Double(Math.abs(speed)).intValue() / 25;

            if (Math.abs(sideSpeed) > 25.0f){
                racingLineindex += (new Double(Math.abs(sideSpeed)).intValue() - 25) / 1;
            }


            while(racingLineindex >= racingLinePoints.size()){
                racingLineindex--;
            }


            double refSteer = 0;


            if (racingLineindex >= 0){
                refSteer = racingLinePoints.get(racingLineindex).x;
            }
    */
            ArrayList<Double> racingLineX = new ArrayList<>();

            for (int i = 0; i < 31; i+=5) {

                if (i < racingLinePoints.size()){

                    boolean found = false;
                    for (int j = 0; j < racingLinePoints.size(); j++) {
                        if(racingLinePoints.get(j).y >= i){
                            racingLineX.add(racingLinePoints.get(i).x);
                            found = true;
                            break;
                        }
                    }
                    if (!found){
                        if (racingLineX.isEmpty()){
                            racingLineX.add(new Double(0.0f));
                        }
                        else{
                            racingLineX.add(racingLineX.get(racingLineX.size()-1));
                        }
                    }

                }
                else if (racingLineX.size() >= 2){
                    racingLineX.add(racingLineX.get(racingLineX.size()-1) + (racingLineX.get(racingLineX.size()-1) - racingLineX.get(racingLineX.size()-2)));
                }
                else if (racingLineX.size() == 1){
                    racingLineX.add(racingLineX.get(racingLineX.size()-1));
                }
                else {
                    racingLineX.add(new Double(0.0f));
                }

            }

            double trackVisibility = racingLinePoints.size();

            //double biggestX = 0.0f;
            //double smallestX = 0.0f;
            double trackBBoxHeigth = 0.0f;

            /*
            for (int i = 0; i < racingLinePoints.size(); i++) {
                if (racingLinePoints.get(i).x > biggestX){
                    biggestX = racingLinePoints.get(i).x;
                }
                if (racingLinePoints.get(i).x < smallestX){
                    smallestX = racingLinePoints.get(i).x;
                }
                if (Math.abs(racingLinePoints.get(i).y) > trackBBoxHeigth){
                    trackBBoxHeigth = Math.abs(racingLinePoints.get(i).y);
                }
            }
            */

            //double trackBBoxWidth = biggestX - smallestX;
            double trackBBoxWidth = 0;

            /*
            if (trackBBoxWidth < 1){
                trackBBoxWidth = 1.0f;
            }
            */

            //double destinatedSpeed = 2800.0f/trackBBoxWidth - 100.0f/trackBBoxHeigth;

            /*
            if (useBot){
                if (speed > destinatedSpeed + 10.0f){
                    accelerationCommand = -1;
                }
                else if (speed < destinatedSpeed - 10.0f){
                    accelerationCommand = 2;
                }
                else{
                    accelerationCommand = 1;
                }
            }
    */

            /**********************************************/
            //Here is place for temporary hacks           //
            /**********************************************/


            int unknownSteeringSituation = 0;
            int unknownAccelerationSituation = 0;


            if (useFuzzyDriverSteering){
                if (variablesInUseSteering.contains("angle")){
                    steeringFis.setVariable("angle", angle);
                }
                if (variablesInUseSteering.contains("speed")){
                    steeringFis.setVariable("speed", speed);
                }
                if (variablesInUseSteering.contains("trackPosition")){
                    steeringFis.setVariable("trackPosition", trackPosition);
                }
                if (variablesInUseSteering.contains("distanceFromRacingLine")){
                    steeringFis.setVariable("distanceFromRacingLine", distanceFromRacingLine);
                }
                if (variablesInUseSteering.contains("maxTrackEdge")){
                    steeringFis.setVariable("maxTrackEdge", maxTrackEdge);
                }
                //if (variablesInUseSteering.contains("racingLine15")){
                //    steeringFis.setVariable("racingLine15", racingLine15);
                //}
                if (variablesInUseSteering.contains("meanCurve")){
                    steeringFis.setVariable("meanCurve", meanCurve);
                }
                if (variablesInUseSteering.contains("trackBBoxWidth")){
                    steeringFis.setVariable("trackBBoxWidth", trackBBoxWidth);
                }
                if (variablesInUseSteering.contains("trackBBoxHeigth")){
                    steeringFis.setVariable("trackBBoxHeigth", trackBBoxHeigth);
                }
                if (variablesInUseSteering.contains("trackVisibility")){
                    steeringFis.setVariable("trackVisibility", trackVisibility);
                }
                if (variablesInUseSteering.contains("outOfTrack")){
                    steeringFis.setVariable("outOfTrack", outOfTrack);
                }
                if (variablesInUseSteering.contains("sideSpeed")){
                    steeringFis.setVariable("sideSpeed", sideSpeed);
                }
                if (variablesInUseSteering.contains("rpm")){
                    steeringFis.setVariable("rpm", sensors.getRPM());
                }
                if (variablesInUseSteering.contains("gear")){
                    steeringFis.setVariable("gear", sensors.getGear());
                }
                if (variablesInUseSteering.contains("gas")){
                    steeringFis.setVariable("gas", toReturn.accelerate);
                }
                if (variablesInUseSteering.contains("brake")){
                    steeringFis.setVariable("brake", toReturn.brake);
                }
                if (variablesInUseSteering.contains("steering")){
                    steeringFis.setVariable("steering", toReturn.steering);
                }
                for (int i = 0; i < trackEdge.length; i++) {
                    if (variablesInUseSteering.contains("track" + Integer.toString(i))){
                        steeringFis.setVariable("track" + Integer.toString(i), trackEdge[i]);
                    }
                }
                for (int i = 0; i < racingLineX.size(); i++) {
                    if (variablesInUseSteering.contains("racingLine" + Integer.toString((i)*5))){
                        steeringFis.setVariable("racingLine" + Integer.toString((i)*5), racingLineX.get(i));
                    }
                }
                for (int i = 0; i < leftPointsX.size(); i++) {
                    if (variablesInUseSteering.contains("leftWall" + Integer.toString((i)*5))){
                        steeringFis.setVariable("leftWall" + Integer.toString((i)*5), leftPointsX.get(i));
                    }
                }
                for (int i = 0; i < rightPointsX.size(); i++) {
                    if (variablesInUseSteering.contains("rightWall" + Integer.toString((i)*5))){
                        steeringFis.setVariable("rightWall" + Integer.toString((i)*5), rightPointsX.get(i));
                    }
                }



                steeringFis.evaluate();
                double rawSteer = steeringFis.getVariable("inputSteering").getValue();


                if (rawSteer > 90.0f){
                    unknownSteeringSituation = 1;
                }
                else if(rawSteer > 0.33f){
                    fuzzyControllerSteering = 1;
                } else if (rawSteer < -0.33f){
                    fuzzyControllerSteering = -1;
                } else {
                    fuzzyControllerSteering = 0;
                }
            }


            if (useFuzzyDriverAcceleration){
    /*
                if (variablesInUseAccel.contains("angle")){
                    accelFis.setVariable("angle", angle);
                }
                if (variablesInUseAccel.contains("speed")){
                    accelFis.setVariable("speed", speed);
                }
                if (variablesInUseAccel.contains("trackPosition")){
                    accelFis.setVariable("trackPosition", trackPosition);
                }
                if (variablesInUseAccel.contains("distanceFromRacingLine")){
                    accelFis.setVariable("distanceFromRacingLine", distanceFromRacingLine);
                }
                if (variablesInUseAccel.contains("maxTrackEdge")){
                    accelFis.setVariable("maxTrackEdge", maxTrackEdge);
                }
                //if (variablesInUseAccel.contains("racingLine15")){
                //    accelFis.setVariable("racingLine15", racingLine15);
                //}
                if (variablesInUseAccel.contains("meanCurve")){
                    accelFis.setVariable("meanCurve", meanCurve);
                }
                if (variablesInUseAccel.contains("outOfTrack")){
                    accelFis.setVariable("outOfTrack", outOfTrack);
                }
                if (variablesInUseAccel.contains("trackBBoxWidth")){
                    accelFis.setVariable("trackBBoxWidth", trackBBoxWidth);
                }
                if (variablesInUseAccel.contains("trackBBoxHeigth")){
                    accelFis.setVariable("trackBBoxHeigth", trackBBoxHeigth);
                }
                if (variablesInUseAccel.contains("sideSpeed")){
                    accelFis.setVariable("sideSpeed", sideSpeed);
                }
                if (variablesInUseAccel.contains("rpm")){
                    accelFis.setVariable("rpm,", sensors.getRPM());
                }
                if (variablesInUseAccel.contains("gear")){
                    accelFis.setVariable("gear,", sensors.getGear());
                }
                if (variablesInUseAccel.contains("trackVisibility")){
                    accelFis.setVariable("trackVisibility,", trackVisibility);
                }
                if (variablesInUseAccel.contains("gas")){
                    accelFis.setVariable("gas", toReturn.accelerate);
                }
                if (variablesInUseAccel.contains("brake")){
                    accelFis.setVariable("brake", toReturn.brake);
                }
                if (variablesInUseAccel.contains("steering")){
                    accelFis.setVariable("steering", toReturn.steering);
                }
                for (int i = 0; i < trackEdge.length; i++) {
                    if (variablesInUseAccel.contains("track" + Integer.toString(i))){
                        accelFis.setVariable("track" + Integer.toString(i), trackEdge[i]);
                    }
                }
                for (int i = 0; i < racingLineX.size(); i++) {
                    if (variablesInUseAccel.contains("racingLine" + Integer.toString((i)*5))){
                        accelFis.setVariable("racingLine" + Integer.toString((i)*5), racingLineX.get(i));
                    }
                }
                for (int i = 0; i < leftPointsX.size(); i++) {
                    if (variablesInUseAccel.contains("leftWall" + Integer.toString((i)*5))){
                        accelFis.setVariable("leftWall" + Integer.toString((i)*5), leftPointsX.get(i));
                    }
                }
                for (int i = 0; i < rightPointsX.size(); i++) {
                    if (variablesInUseAccel.contains("rightWall" + Integer.toString((i)*5))){
                        accelFis.setVariable("rightWall" + Integer.toString((i)*5), rightPointsX.get(i));
                    }
                }



                accelFis.evaluate();
                double rawAccel = accelFis.getVariable("inputAcceleration").getValue();

                if (fuzzyAccelControllerAlwaysGasOn){
                    fuzzyControllerAcceleration = 2;
                }
                else{
                    if(rawAccel > 90.00f){
                        unknownAccelerationSituation = 1;
                    } else if(rawAccel > 0.33f){
                        fuzzyControllerAcceleration = 2;
                    } else if (rawAccel < -0.33f){
                        fuzzyControllerAcceleration = -1;
                    } else {
                        fuzzyControllerAcceleration = 0;
                    }
                }
                */
            }





            if (steeringInputType == InputType.fuzzyControl){
                steeringCommand = fuzzyControllerSteering;
            }
            if (steeringInputType == InputType.pad){
               steeringCommand = padSteering;
            }


            if (accelerationInputType == InputType.fuzzyControl){
               accelerationCommand = fuzzyControllerAcceleration;
            }
            if (accelerationInputType == InputType.pad){
                accelerationCommand = padAcceleration;

            }


            if (useSpeedLimiter){
                double speedLimit = racingLine.GetSpeedLimitAfter(distanceFromStartLine);
                double speedDifference = speed - speedLimit;
                if(speedDifference > 10.0f){
                    accelerationCommand = -1;
                }
                else if (speedDifference < 10.0f && speedDifference >= 0.0f ){
                    accelerationCommand = 1;
                }
                else if (speedDifference < 0.0f){
                    accelerationCommand = 2;
                }
            }



            //time update

            currentFrameTime = System.nanoTime();
            double deltaTime = (currentFrameTime - lastFrameTime) / 1000000000.0f;
            lastFrameTime = currentFrameTime;


            double accelerationAdjustSpeed = steeringFis.getVariable("accelerationAdjustSpeed").getValue();
            double brakeAdjustSpeed = steeringFis.getVariable("brakeAdjustSpeed").getValue();
            double steeringAdjustSpeed = steeringFis.getVariable("steeringAdjustSpeed").getValue();


            double desiredFPS = 50.0f;

            switch (accelerationCommand){
                case(0):
                    toReturn.accelerate = 0.0f;
                    toReturn.brake = 0.0f;
                    break;
                case(1):
                    if (toReturn.gear > 0){
                        toReturn.accelerate += deltaTime * desiredFPS * accelerationAdjustSpeed;
                        toReturn.accelerate = Math.min(toReturn.accelerate, 0.5f);
                        toReturn.brake = 0.0f;
                    }
                    else if (toReturn.gear < 0){
                        toReturn.accelerate = 0f;
                        toReturn.brake += deltaTime * desiredFPS * brakeAdjustSpeed;
                    }
                    break;
                case(2):
                    if (toReturn.gear > 0){
                        toReturn.accelerate += deltaTime * desiredFPS * accelerationAdjustSpeed;
                        toReturn.brake = 0.0f;
                    }
                    else if (toReturn.gear < 0){
                        toReturn.accelerate = 0f;
                        toReturn.brake += deltaTime * desiredFPS * brakeAdjustSpeed;
                    }
                    break;
                case(-1):
                    if (toReturn.gear > 0){
                        toReturn.accelerate = 0.0f;
                        toReturn.brake += deltaTime * desiredFPS * brakeAdjustSpeed;
                    }
                    else if (toReturn.gear < 0){
                        toReturn.accelerate += deltaTime * desiredFPS * accelerationAdjustSpeed;
                        toReturn.brake = 0.0f; 
                    }
                    break;
            }

            toReturn.gear = getGear(sensors, accelerationCommand);

            //ABS
            toReturn.brake = filterABS(sensors, toReturn.brake);
            toReturn.accelerate = filterTCL(sensors, toReturn.accelerate);

            switch (steeringCommand){
                case(0):
                    toReturn.steering = 0.0f;
                    break;
                case(1):
                    if(toReturn.steering < 0.0f){
                        toReturn.steering = 0.0f;
                    }
                    toReturn.steering += deltaTime * desiredFPS * steeringAdjustSpeed;
                    break;
                case(-1):
                    if(toReturn.steering > 0.0f){
                        toReturn.steering = 0.0f;
                    }
                    toReturn.steering -= deltaTime * desiredFPS * steeringAdjustSpeed;
                    break;
            }

            toReturn.accelerate = Math.min( toReturn.accelerate, 1.0f);
            toReturn.brake = Math.min( toReturn.brake, 1.0f);
            toReturn.steering = Math.max(-1.0f, Math.min( toReturn.steering, 1.0f));


            StringBuilder strBuilder = new StringBuilder();

            for (int i = 0; i < angles.length; i++) {
                strBuilder.append(doubleFormatter.format(trackEdge[i]));
                strBuilder.append(",");
            }
            for (int i = 0; i < racingLineX.size(); i++) {
                strBuilder.append(doubleFormatter.format(racingLineX.get(i)));
                strBuilder.append(",");
            }
            for (int i = 0; i < leftPointsX.size(); i++) {
                strBuilder.append(doubleFormatter.format(leftPointsX.get(i)));
                strBuilder.append(",");
            }
            for (int i = 0; i < rightPointsX.size(); i++) {
                strBuilder.append(doubleFormatter.format(rightPointsX.get(i)));
                strBuilder.append(",");
            }


            strBuilder.append(doubleFormatter.format(trackPosition));
            strBuilder.append(",");
            strBuilder.append(doubleFormatter.format(maxTrackEdge));
            strBuilder.append(",");
            strBuilder.append(doubleFormatter.format(speed));
            strBuilder.append(",");
            strBuilder.append(doubleFormatter.format(sideSpeed));
            strBuilder.append(",");
            strBuilder.append(doubleFormatter.format(sensors.getRPM()));
            strBuilder.append(",");
            strBuilder.append(sensors.getGear());
            strBuilder.append(",");
            strBuilder.append(doubleFormatter.format(distanceFromStartLine));
            strBuilder.append(",");
            strBuilder.append(doubleFormatter.format(angle));
            strBuilder.append(",");
            strBuilder.append(doubleFormatter.format(distanceFromRacingLine));
            strBuilder.append(",");
            //strBuilder.append(doubleFormatter.format(racingLine15));
            //strBuilder.append(",");
            strBuilder.append(outOfTrack);
            strBuilder.append(",");
            strBuilder.append(doubleFormatter.format(meanCurve));
            strBuilder.append(",");
            strBuilder.append(doubleFormatter.format(trackBBoxWidth));
            strBuilder.append(",");
            strBuilder.append(doubleFormatter.format(trackBBoxHeigth));
            strBuilder.append(",");
            strBuilder.append(trackVisibility);
            strBuilder.append(",");
            strBuilder.append(unknownSteeringSituation);
            strBuilder.append(",");
            strBuilder.append(unknownAccelerationSituation);
            strBuilder.append(",");


            if (steeringCommand == 1){
                strBuilder.append("s1");
            } else if (steeringCommand == -1){
                strBuilder.append("s_1");
            } else if (steeringCommand == 0){
                strBuilder.append("s0");
            } else{
                strBuilder.append("Unknown");
            }

            strBuilder.append(",");

            strBuilder.append(doubleFormatter.format(toReturn.steering));
            strBuilder.append(",");



            if (accelerationCommand == 2){
                strBuilder.append("a2");
            } else if (accelerationCommand == 1){
                strBuilder.append("a1");
            } else if (accelerationCommand < 0){
                strBuilder.append("a_1");
            } else if (accelerationCommand == 0){
                strBuilder.append("a0");
            } else{
                strBuilder.append("Unknown");
            }

            strBuilder.append(",");

            strBuilder.append("v" + new Double(racingLine.GetSpeedLimitAfter(distanceFromStartLine)).intValue());
            strBuilder.append(",");

            strBuilder.append(doubleFormatter.format(toReturn.accelerate));
            strBuilder.append(",");
            strBuilder.append(doubleFormatter.format(toReturn.brake));
            strBuilder.append(",");

            System.out.println(strBuilder.toString());



            double distanceRaced = sensors.getDistanceRaced();

            lapsDone = (int)Math.floor(distanceRaced/trackLength);

            double trackCompleted = (lapsDone >= (Consts.lapsPerCandidate) ? Consts.trackCompletedBonus : Consts.trackNotCompletedBonus);
            distancesFromRacingLineSum += Math.abs(distanceFromRacingLine);
            squareDistancesFromRacingLineSum += (distanceFromRacingLine * distanceFromRacingLine);
            if(minTrackEdge > Consts.safeDistance){
                safeDistanceFromEdgeSum += 1.0f;
            }

            double racingLineAverageDist = squareDistancesFromRacingLineSum/counter;   
            double safeDistanceAverage = safeDistanceFromEdgeSum/counter;


            double score = (distanceRaced + distanceRaced * (1.0 - racingLineAverageDist) + distanceRaced * Math.pow(safeDistanceAverage, 10.0f)) * trackCompleted;

            geneticAlg.fitFunc.score = Math.max(score, 0.0f);

            if (
                    //(chromosomeCounter > Consts.earlyKillAfter && distanceRaced < 100.0f)|| 
                    sensors.getDamage() > Consts.maxDamage){
                counter = Double.MAX_VALUE;
            }

            if (trackEdge[9] <= 0.0f){
                counter = Double.MAX_VALUE;
            }

            if (lapsDone >= Consts.lapsPerCandidate || counter > Consts.maxTicksPerCandidate ){

                raceFinished = true;
                
                System.err.println("score = (distanceRaced + distanceRaced * (1.0 - racingLineAverageDist) + distanceRaced * Math.pow(safeDistanceAverage, 10.0f)) * trackCompleted");
                System.err.println("distanceRaced: " + distanceRaced);
                System.err.println("distanceRaced * (1.0 - racingLineAverageDist): " + distanceRaced * (1.0 - racingLineAverageDist));
                System.err.println("distanceRaced * Math.pow(safeDistanceAverage, 10.0f): " + distanceRaced * Math.pow(safeDistanceAverage, 10.0f));
                System.err.println("trackCompleted: " + trackCompleted);


                toReturn.restartRace = true;

                if (score > bestScoreSoFar){
                    Gpr.toFile("furia_genetic/robot_best_fit_gen_" + generationNumber + ".fcl", steeringFis.getFunctionBlock(null).toString());
                    bestScoreSoFar = score;
                }

                Gpr.toFile("furia_genetic/robots/robot_" + generationNumber + "_unit_" + unitNumber + "_result_" + Math.round(score) + ".fcl", steeringFis.getFunctionBlock(null).toString());

            }

            return toReturn;
    }
    
    @Override
    public void reset() {
        resetState();
        synchronized(geneticAlg.fitFunc.state){
            geneticAlg.fitFunc.state = TorcsFitnessFunction.CurrentState.TestRideCompleted;
        }
    }
    
    @Override
    public void shutdown() {
        //System.out.println("Bye bye!");
    }
    
    @Override
    public float[] initAngles() {
        return angles;
    }
    
    private int getGear(SensorModel sensors, int accelerate) {
        
        int gear = sensors.getGear();
        double rpm = sensors.getRPM();
        double speed = sensors.getSpeed();
        
        /*
        gearFis.setVariable("speed", speed);
        gearFis.setVariable("rpm", rpm);
        gearFis.setVariable("gear", rpm);
        gearFis.evaluate();
        return new Double(Math.floor(gearFis.getVariable("nextGear").getValue())).intValue();
        */
        
        
        if (automaticGear){
            
            // jeżeli się cofam
            if (speed <= -1.0f){
                return -1;
            }

            //jezeli stoje w miejscu
            if (speed > -1.0f && speed <= 1.0f){
                if(accelerate == 0 ){
                    return 0;
                }
                else if(accelerate > 0){
                    return 1;
                }
                else if(accelerate < 0){
                    return -1;
                }
            }

            // jezeli jadę do przodu
            if(speed > 1.0f && gear < 1){
                return 1;
            }

            // check if the RPM value of car is greater than the one suggested 
            // to shift up the gear from the current one     
            if (gear < 6 && rpm >= gearPreference.gearUps.get(gear - 1) && speed >= gearPreference.speedUps.get(gear-1)) {
                return gear + 1;
            } else // check if the RPM value of car is lower than the one suggested 
            // to shift down the gear from the current one
            if (gear > 1 && (rpm <= gearPreference.gearDowns.get(gear - 1) || speed <= gearPreference.speedDowns.get(gear-1))) {
                return gear - 1;
            } else // otherwhise keep current gear
            {
                return gear;
            }
        
        }
        else{
            return padGear;
        }
        
    }
    
    private void resetState(){
        toReturn = new Action();
        counter = 0.0f;
        distancesFromRacingLineSum = 0.0f;
        squareDistancesFromRacingLineSum = 0.0f;
        safeDistanceFromEdgeSum = 0.0f;
        lapsDone = 0;
        firstLoop = true;
        roadWidth = 0.0f;
        racingLineIndex = 0;
        raceFinished = false;
    }
    
    @Override
    public void setGearsPreferences(GearPreference gp){
        gearPreference = gp;
    }
    
    @Override
    public void setRacingLine(RacingLine rl){
        racingLine = rl;
    }
    
    void setTrackLength(Double trackLength) {
        this.trackLength = trackLength;
    }
    
    private int[] convertIntegers(List<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = integers.get(i);
        }
        return ret;
    }

    void setFIS(FIS steeringFis, FIS accelFis, FIS gearFis) {
        this.steeringFis = steeringFis;
        this.accelFis = accelFis;
        this.gearFis = gearFis;
        variablesInUseSteering = this.steeringFis.getFunctionBlock("fb").getVariables().keySet();
        variablesInUseAccel = this.accelFis.getFunctionBlock("fb").getVariables().keySet();
    }
    
    double filterABS(SensorModel sensors,double brake){
        double newBrake = brake;
        if ((sensors.getSpeed()/3.6f) < ABS_MINSPEED) {
            return newBrake;
        }
        else{
            double slip = 0.0f;
            for (int i = 0; i < 4; i++) {
                slip += sensors.getWheelSpinVelocity()[i] * getwheelRadius(i) / (sensors.getSpeed()/3.6f);
            }
            slip = slip/4.0f;
        
            if (slip < ABS_SLIP){
                newBrake = brake*slip;
            }
                
            return newBrake;
        }
    }
    
    
    double getwheelRadius(int wheel){
        if (wheel == FRONT_LEFT || wheel == FRONT_RIGHT){
            return 0.3306f;
        }
        else{
            return 0.3192;
        }
    }
    
    
    double filterTCL(SensorModel sensors, double accel){
        
        double newAccel = accel;
        
        if ((sensors.getSpeed()/3.6f) < TCL_MINSPEED){
            return newAccel;
        }
        double DRIVEN_WHEEL_SPEED = (sensors.getWheelSpinVelocity()[REAR_RIGHT] + sensors.getWheelSpinVelocity()[REAR_LEFT]) * getwheelRadius(REAR_LEFT) / 2.0;
        
        double slip = (sensors.getSpeed()/3.6f)/DRIVEN_WHEEL_SPEED;
        
        if (slip < TCL_SLIP) {
            newAccel = 0.0;
        }
        return newAccel;

    }
    
    private ArrayList<Point2D.Double> ChangeLineResoultion(ArrayList<Point2D.Double> line, double stepSize){
        
        if (line.isEmpty()){
            return new ArrayList<Point2D.Double>();
        }
        
        ArrayList<Point2D.Double> newLine = new ArrayList<>();

        double lastPointX = line.get(0).x;
        double lastPointY = line.get(0).y;

        newLine.add(line.get(0));

        for (int i = 1; i < line.size(); i++) {

            double dist = Point2D.distance(line.get(i).x, line.get(i).y, lastPointX, lastPointY);

            while (dist > stepSize){
                double xShift = stepSize * (line.get(i).x - lastPointX) / dist;
                double yShift = stepSize * (line.get(i).y - lastPointY) / dist;
                newLine.add(new Point2D.Double(lastPointX + xShift, lastPointY + yShift));

                lastPointX = lastPointX + xShift;
                lastPointY = lastPointY + yShift;

                dist = Point2D.distance(line.get(i).x, line.get(i).y, lastPointX, lastPointY);
            }

        }

        newLine.add(line.get(line.size()-1));
        return newLine;

    }
 
    class GeneticThread implements Runnable{

        @Override
        public void run() {
            
            padDriver.unitNumber = 0;
            
            for( int i = geneticAlg.starGen; i < Consts.generations; i++ )
            {
                padDriver.generationNumber = i;
                
                System.err.println("");
                System.err.println("**********************************");
                System.err.println("Generation " + Integer.toString(i));
                System.err.println("**********************************");
                System.err.println("");
                geneticAlg.genotype.evolve();
                
                DataTreeBuilder builder = DataTreeBuilder.getInstance();
                IDataCreators doc;
                try {
                    doc = builder.representGenotypeAsDocument(geneticAlg.genotype);
                    XMLDocumentBuilder docbuilder = new XMLDocumentBuilder();
                    Document xmlDoc = null;
                    xmlDoc = (Document) docbuilder.buildDocument(doc);
                    XMLManager.writeFile(xmlDoc, new File("furia_genetic/generations/" + Integer.toString(i) + ".xml"));
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                    Logger.getLogger(padDriver.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    ex.printStackTrace(System.err);
                    Logger.getLogger(padDriver.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        }
    }
    
}