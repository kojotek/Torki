package pl.edu.amu.wmi.min.torcs.fcl;
import java.util.List;
import net.sourceforge.cig.torcs.Controller;
import net.sourceforge.cig.torcs.Action;
import net.sourceforge.cig.torcs.SensorModel;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Set;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.rule.Variable;

public class padDriver extends Controller {

    boolean useFuzzyDriverSteering = true;
    boolean useFuzzyDriverAcceleration = true;
    boolean useBot = false;
    Set<String> variablesInUseSteering;
    Set<String> variablesInUseAccel; 
    boolean pulse;
    net.java.games.input.Controller pad;
    private final float[] angles;
    private List<Point2D.Double> points;
    private  int[] gearUp = {9000, 8000, 8000, 8000, 8000, 0};
    private  int[] gearDown = {0, 2500, 3000, 3000, 3500, 3500};
    private int steeringRawInput, accelerationRawInput = 0;
    private boolean gasPressed, brakePressed = false;
    private float smoothedSteering = 0.0f;
    private RacingLine racingLine;
    
    FIS steeringFis;
    FIS accelFis;
    FIS gearFis;
    
    Action toReturn;
    boolean firstLoop;
    double roadWidth = 0.0f;
    int racingLineIndex = 0;
    
    Visualisation visualisation;
    
    DecimalFormat doubleFormatter;  
    
    public padDriver() {

        doubleFormatter = new DecimalFormat("0.0000");
        DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
        sym.setDecimalSeparator('.');
        doubleFormatter.setDecimalFormatSymbols(sym);
        
        firstLoop = true;
        
        visualisation = new Visualisation();
        
        this.pulse = false;
        
        toReturn = new Action();
        
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
                + "racingLine5,racingLine10,"
                + "racingLine15,racingLine20,"
                + "racingLine25,racingLine30,"
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
                + "inputSteering,"
//                + "steering,"
                + "inputAcceleration,"
//                + "gas,"
//                + "brake,"
                //+ "gear"
        );

    }
    
    
    @Override
    public Action control(SensorModel sensors) {
        
        
        
        if(firstLoop){
            double[] edges = sensors.getTrackEdgeSensors();
            
            roadWidth = edges[0] + edges[edges.length-1];
            firstLoop = false;
        }
        
        
        pad.poll();
        net.java.games.input.EventQueue queue = pad.getEventQueue();
        net.java.games.input.Event event = new net.java.games.input.Event();

        while(queue.getNextEvent(event)) {
            String id = event.getComponent().getIdentifier().toString();
            float value = event.getValue(); 
            switch(id){
                case "pov":
                    if (value > 0.75f || (value < 0.25f && value > 0.0f)){
                        steeringRawInput = 1;
                    }
                    else if (value > 0.25f && value < 0.75f){
                        steeringRawInput = -1;
                    }
                    else{
                        steeringRawInput = 0;
                    }
                break;

                case "5":
                    gasPressed = (value > 0.0f);
                break;

                case "4":
                    brakePressed = (value > 0.0f);
                break;
            }
        }

        if (gasPressed == brakePressed){
            accelerationRawInput = 0;
        }
        else if (gasPressed){
            accelerationRawInput = 2;
        }
        else if (brakePressed){
            accelerationRawInput = -1;
        }
        

        double[] trackEdge = sensors.getTrackEdgeSensors();
        
        double maxTrackEdge = -5.0f;
        int border = 4;
        for (int i = border; i < trackEdge.length - border; i++) {
            if(trackEdge[i] > maxTrackEdge){
                maxTrackEdge = trackEdge[i];
            }
        }
        
        points.clear();

        //computing x,y points
        double radians;
        for (int i = 0; i < trackEdge.length; i++) {
            radians = Math.toRadians(angles[i]);
            points.add(new Point2D.Double(trackEdge[i] * Math.sin(radians), trackEdge[i] * Math.cos(radians)));
        }
        
        
        
        //computing angles between points
        ArrayList<Double> borderAngles = new ArrayList<>();
        borderAngles.add(Math.PI);
        
        for (int i = 1; i < points.size()-1; i++) {
            double abcAngle = 
                    Math.atan2(points.get(i+1).y - points.get(i).y, points.get(i+1).x - points.get(i).x)
                    - Math.atan2(points.get(i-1).y - points.get(i).y, points.get(i-1).x - points.get(i).x);
            
            borderAngles.add(Math.abs(abcAngle));    
        }
        
        borderAngles.add(Math.PI);

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
            
            /*
            ArrayList<Point2D.Double> newLeftPoints = new ArrayList<>();
            //increasing resolution
            for (int i = 1; i < leftPoints.size(); i++) {
                
                newLeftPoints.add(leftPoints.get(i-1));
                
                double dist = Point2D.distance(leftPoints.get(i).x, leftPoints.get(i).y, leftPoints.get(i-1).x, leftPoints.get(i-1).y);
                int intDist = new Double(dist).intValue();
                double xShift = (leftPoints.get(i).x - leftPoints.get(i-1).x) / (double)intDist;
                double yShift = (leftPoints.get(i).y - leftPoints.get(i-1).y) / (double)intDist;
                for (int j = 1; j < intDist; j++) {
                    newLeftPoints.add(new Point2D.Double(leftPoints.get(i-1).x + xShift * j, leftPoints.get(i-1).y + yShift * j));
                }
            }
            
            if(!leftPoints.isEmpty()){
                newLeftPoints.add(leftPoints.get(leftPoints.size()-1));
                leftPoints = newLeftPoints;
            }
            */
            
            
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
            
            /*
            ArrayList<Point2D.Double> newRightPoints = new ArrayList<>();
            //increasing resolution
            for (int i = 1; i < rightPoints.size(); i++) {
                
                newRightPoints.add(rightPoints.get(i-1));
                
                double dist = Point2D.distance(rightPoints.get(i).x, rightPoints.get(i).y, rightPoints.get(i-1).x, rightPoints.get(i-1).y);
                int intDist = new Double(dist).intValue();
                double xShift = (rightPoints.get(i).x - rightPoints.get(i-1).x) / (double)intDist;
                double yShift = (rightPoints.get(i).y - rightPoints.get(i-1).y) / (double)intDist;
                
                for (int j = 1; j < intDist; j++) {
                    newRightPoints.add(new Point2D.Double(rightPoints.get(i-1).x + xShift * (double)j, rightPoints.get(i-1).y + yShift * (double)j));
                }
            }
            
            if (!rightPoints.isEmpty()){
                newRightPoints.add(rightPoints.get(rightPoints.size()-1));
                rightPoints = newRightPoints;
            }
            */
            

            
            if(rightPoints.size() >= 2){
                double firstPointAngle = -Math.atan2(rightPoints.get(1).y - rightPoints.get(0).y,
                    rightPoints.get(1).x - rightPoints.get(0).x);
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
        
        if(leftPoints.size() == rightPoints.size()){
            
            /*
            if (!leftPoints.isEmpty()){
                previousCenterOfPointX = (rightPoints.get(0).x + leftPoints.get(0).x) / 2.0f;
                previousCenterOfPointY = (rightPoints.get(0).y + leftPoints.get(0).y) / 2.0f;
            }
            */
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
            
            //increasing resolution of racing line
            ArrayList<Point2D.Double> newRacingLinePoints = new ArrayList<>();
            double stepSize = 2.0f;
            double lastPointX = racingLinePoints.get(0).x;
            double lastPointY = racingLinePoints.get(0).y;
            
            newRacingLinePoints.add(racingLinePoints.get(0));
            
            for (int i = 1; i < racingLinePoints.size(); i++) {
                
                //newRacingLinePoints.add(racingLinePoints.get(i-1));
                
                double dist = Point2D.distance(racingLinePoints.get(i).x, racingLinePoints.get(i).y, lastPointX, lastPointY);
                
                while (dist > stepSize){
                    double xShift = stepSize * (racingLinePoints.get(i).x - lastPointX) / dist;
                    double yShift = stepSize * (racingLinePoints.get(i).y - lastPointY) / dist;
                    newRacingLinePoints.add(new Point2D.Double(lastPointX + xShift, lastPointY + yShift));
                    
                    lastPointX = lastPointX + xShift;
                    lastPointY = lastPointY + yShift;
                    
                    dist = Point2D.distance(racingLinePoints.get(i).x, racingLinePoints.get(i).y, lastPointX, lastPointY);
                }
                
            }
            
            if (!racingLinePoints.isEmpty()){
                newRacingLinePoints.add(racingLinePoints.get(racingLinePoints.size()-1));
                racingLinePoints = newRacingLinePoints;
            }
            
        }
        else{
            System.err.println("nierowne krawedzie!");
        }
        
        /*
        double[] distances = new double[] {10.0f, 20.0f, 30.0f, 40.0f, 50.0f};
        ArrayList<Double> racingPointsX = new ArrayList<>();
        
        double distanceChecked = 0.0f;
        
        int indexOfDistanceChecked = 1;
        for (int i = 0; i < distances.length; i++) {
            for (int j = indexOfDistanceChecked; j < racingLinePoints.size(); j++) {
                if (distanceChecked < distances[i]){
                    distanceChecked += 
                            Point2D.distance(racingLinePoints.get(indexOfDistanceChecked).x, racingLinePoints.get(indexOfDistanceChecked).y, 
                            racingLinePoints.get(indexOfDistanceChecked-1).x, racingLinePoints.get(indexOfDistanceChecked-1).y);
                    indexOfDistanceChecked = j;
                }
                else{
                    racingPointsX.add(racingLinePoints.get(indexOfDistanceChecked).x);
                    break;
                }
            }
        }
        */
        
        
        //mean of angles
        double anglesChangeSum = 0.0f;
        double centerLineLength = 0.0f;
        
        for (int i = 1; i < centerLinePoints.size()-1; i++) {
            centerLineLength += Point2D.distance(centerLinePoints.get(i).x, centerLinePoints.get(i).y, centerLinePoints.get(i-1).x, centerLinePoints.get(i-1).y);
            double racingLineAngle = 
                    Math.atan2(centerLinePoints.get(i+1).y - centerLinePoints.get(i).y, centerLinePoints.get(i+1).x - centerLinePoints.get(i).x)
                    - Math.atan2(centerLinePoints.get(i-1).y - centerLinePoints.get(i).y, centerLinePoints.get(i-1).x - centerLinePoints.get(i).x);
            racingLineAngle = Math.abs(racingLineAngle);
            if(racingLineAngle > Math.PI){
                racingLineAngle = Math.PI - (2*Math.PI - racingLineAngle);
            }
            else{
                racingLineAngle = Math.PI - racingLineAngle;
            }
                    //System.err.println(racingLineAngle/Math.PI);
            anglesChangeSum += racingLineAngle * (180.0f/Math.PI) * 100.0f;
        }
        
        if (centerLinePoints.size() >= 3){
            centerLineLength += 
                    Point2D.distance(centerLinePoints.get(centerLinePoints.size()-1).x, centerLinePoints.get(centerLinePoints.size()-1).y,
                    centerLinePoints.get(centerLinePoints.size()-2).x, centerLinePoints.get(centerLinePoints.size()-2).y);
        }
        
        double meanCurve = anglesChangeSum/centerLineLength;
        
        /*
        double racingLine15 = 0.0f;
        if (racingPointsX.size() > 0){
            racingLine15 = racingPointsX.get(0);
        }
        */

        visualisation.Redraw(leftPoints, rightPoints, racingLinePoints);     
        
        
        double angle = sensors.getAngleToTrackAxis() * (180.0f/Math.PI);
        double speed = sensors.getSpeed();
        double sideSpeed = sensors.getLateralSpeed();
        double trackPosition = sensors.getTrackPosition();
        int outOfTrack = trackEdge[0] < 0.1f ? 1 : 0;
        
        
        
        
        
        
                /**********************************************/
        //Here is place for temporary hacks           //
        /**********************************************/
        

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

        ArrayList<Double> racingLineX = new ArrayList<>();
        
        for (int i = 5; i < 31; i+=5) {
            if (i < racingLinePoints.size() ){
                racingLineX.add(racingLinePoints.get(i).x);
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
        
        if (useBot){
            if(refSteer > 0.6f){
                steeringRawInput = -1;
            } 
            else if (refSteer < -0.6f){
                steeringRawInput = 1;
            }
            else{
                steeringRawInput = 0;
            }
        }
        
        
        double trackVisibility = racingLinePoints.size();

        double biggestX = 0.0f;
        double smallestX = 0.0f;
        double trackBBoxHeight = 0.0f;
        for (int i = 0; i < racingLinePoints.size(); i++) {
            if (racingLinePoints.get(i).x > biggestX){
                biggestX = racingLinePoints.get(i).x;
            }
            if (racingLinePoints.get(i).x < smallestX){
                smallestX = racingLinePoints.get(i).x;
            }
            if (Math.abs(racingLinePoints.get(i).y) > trackBBoxHeight){
                trackBBoxHeight = Math.abs(racingLinePoints.get(i).y);
            }
        }
        
        double trackBBoxWidth = biggestX - smallestX;
        
        if (trackBBoxWidth < 1){
            trackBBoxWidth = 1.0f;
        }
        
        double destinatedSpeed = 2800.0f/trackBBoxWidth - 100.0f/trackBBoxHeight;
        
        if (useBot){
            if (speed > destinatedSpeed + 10.0f){
                accelerationRawInput = -1;
            }
            else if (speed < destinatedSpeed - 10.0f){
                accelerationRawInput = 2;
            }
            else{
                accelerationRawInput = 1;
            }
        }

        
        /**********************************************/
        //Here is place for temporary hacks           //
        /**********************************************/
        
        
        
        
        
        
        
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
            if (variablesInUseSteering.contains("outOfTrack")){
                steeringFis.setVariable("outOfTrack", outOfTrack);
            }
            if (variablesInUseSteering.contains("trackBBoxWidth")){
                steeringFis.setVariable("trackBBoxWidth", trackBBoxWidth);
            }
            if (variablesInUseSteering.contains("trackBBoxHeight")){
                steeringFis.setVariable("trackBBoxHeight", trackBBoxHeight);
            }
            if (variablesInUseSteering.contains("outOfTrack")){
                steeringFis.setVariable("outOfTrack", outOfTrack);
            }
            if (variablesInUseSteering.contains("sideSpeed")){
                steeringFis.setVariable("sideSpeed", sideSpeed);
            }
            for (int i = 0; i < trackEdge.length; i++) {
                if (variablesInUseSteering.contains("track" + Integer.toString(i))){
                    steeringFis.setVariable("track" + Integer.toString(i), trackEdge[i]);
                }
            }
            for (int i = 0; i < racingLineX.size(); i++) {
                if (variablesInUseSteering.contains("racingLine" + Integer.toString((i+1)*5))){
                    steeringFis.setVariable("racingLine" + Integer.toString((i+1)*5), racingLineX.get(i));
                }
            }

            steeringFis.evaluate();
            double rawSteer = steeringFis.getVariable("inputSteering").getValue();
            //System.err.println(rawValue);
            if(rawSteer > 0.33f){
                steeringRawInput = 1;
            } else if (rawSteer < -0.33f){
                steeringRawInput = -1;
            } else {
                steeringRawInput = 0;
            }
        }

            
        if (useFuzzyDriverAcceleration){

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
            if (variablesInUseAccel.contains("trackBBoxHeight")){
                accelFis.setVariable("trackBBoxHeight", trackBBoxHeight);
            }
            if (variablesInUseAccel.contains("outOfTrack")){
                accelFis.setVariable("outOfTrack", outOfTrack);
            }
            if (variablesInUseAccel.contains("sideSpeed")){
                accelFis.setVariable("sideSpeed", sideSpeed);
            }
            for (int i = 0; i < trackEdge.length; i++) {
                if (variablesInUseAccel.contains("track" + Integer.toString(i))){
                    accelFis.setVariable("track" + Integer.toString(i), trackEdge[i]);
                }
            }
            for (int i = 0; i < racingLineX.size(); i++) {
                if (variablesInUseAccel.contains("racingLine" + Integer.toString((i+1)*5))){
                    accelFis.setVariable("racingLine" + Integer.toString((i+1)*5), racingLineX.get(i));
                }
            }

            
            accelFis.evaluate();
            double rawAccel = accelFis.getVariable("inputAcceleration").getValue();

            accelerationRawInput = (int) Math.round(rawAccel);
            
            if(rawAccel > 0.33f){
                accelerationRawInput = 2;
            } else if (rawAccel < -0.33f){
                accelerationRawInput = -1;
            } else {
                accelerationRawInput = 0;
            }
        }
        
        
        
        


        
        
        
        
        switch (accelerationRawInput){
            case(0):
                toReturn.accelerate = 0.0f;
                toReturn.brake = 0.0f;
                break;
            case(1):
                if (toReturn.gear > 0){
                    toReturn.accelerate += 0.1f;
                    toReturn.accelerate = Math.min(toReturn.accelerate, 0.5f);
                    toReturn.brake = 0.0f;
                }
                else if (toReturn.gear < 0){
                    toReturn.accelerate = 0f;
                    toReturn.brake += 0.1f;
                }
                break;
            case(2):
                if (toReturn.gear > 0){
                    toReturn.accelerate += 0.1f;
                    toReturn.brake = 0.0f;
                }
                else if (toReturn.gear < 0){
                    toReturn.accelerate = 0f;
                    toReturn.brake += 0.1f; 
                }
                break;
            case(-1):
                if (toReturn.gear > 0){
                    toReturn.accelerate = 0.0f;
                    toReturn.brake += 0.1f;
                }
                else if (toReturn.gear < 0){
                    toReturn.accelerate += 0.1f;
                    toReturn.brake = 0.0f; 
                }
                break;
        }
        
        toReturn.gear = getGear(sensors, accelerationRawInput);
        
        if(toReturn.brake > 0.1f && Math.abs(sensors.getWheelSpinVelocity()[0]) < 1.0f){
            toReturn.brake = 0.0f;
        }
        
        switch (steeringRawInput){
            case(0):
                toReturn.steering = 0.0f;
                break;
            case(1):
                toReturn.steering += 0.05f;
                break;
            case(-1):
                toReturn.steering -= 0.05f;
                break;
        }        
        
        
        

        
        //steeringFis.setVariable("trackPos", trackPosition);

        // Speed (x) (−∞,+∞) (km/h) 
        // Speed of the car along the longitudinal axis of the car.
        
        //steeringFis.setVariable("speed", speed);


//        double[] opponents = sensors.getOpponentSensors();
        

//for (int i = 0; i < opponents.length; i++) {
        //    steeringFis.setVariable("opponent" + i, opponents[i]);
        //}

        // angle [-π,+π] (rad) 
        // Angle between the car direction and the direction of the
        // track axis.

        
        /*
        if (angle < 0.01f && angle < -0.01f){
            angle = 0.0f;
        }
        
        if (distanceFromRacingLine < 0.01f && distanceFromRacingLine < -0.01f){
            distanceFromRacingLine = 0.0f;
        }
        */
        
        //steeringFis.setVariable("angle", angle);

        //steeringFis.evaluate();

        //Variable steering = steeringFis.getVariable("steering");
        //Virtual gas pedal (0 means no gas, 1 full gas). Range [0,1] 
        //Variable accelerate = steeringFis.getVariable("accelerate");
        // Virtual brake pedal (0 means no brake, 1 full brake). Range [0,1]
        //Variable brake = steeringFis.getVariable("brake");
        
        

        //toReturn.steering = steering.getValue();
        //toReturn.accelerate = accelerate.getValue();
        //toReturn.brake = brake.getValue();

        //System.err.println(maxTrackEdge);
        
        StringBuilder strBuilder = new StringBuilder();
        
        for (int i = 0; i < angles.length; i++) {
            strBuilder.append(doubleFormatter.format(trackEdge[i]));
            strBuilder.append(",");
        }
        for (int i = 0; i < racingLineX.size(); i++) {
            strBuilder.append(doubleFormatter.format(racingLineX.get(i)));
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
        strBuilder.append(doubleFormatter.format(trackBBoxHeight));
        strBuilder.append(",");
        strBuilder.append(trackVisibility);
        strBuilder.append(",");

        
        
        //strBuilder.append(steeringRawInput);
        //strBuilder.append(",");        
//Instead:

        
        if (steeringRawInput == 1){
            strBuilder.append("Right");
        } 
        else if (steeringRawInput == -1){
            strBuilder.append("Left");
        } else{
            strBuilder.append("Middle");
        }
        
        strBuilder.append(",");
        
//        strBuilder.append(toReturn.steering);



        if (accelerationRawInput == 2){
            strBuilder.append("Forward");
        } 
        else if (accelerationRawInput == -1){
            strBuilder.append("Backward");
        } else{
            strBuilder.append("Middle");
        }
        
        strBuilder.append(",");

//        strBuilder.append(accelerationRawInput);
//        strBuilder.append(",");


//        strBuilder.append(toReturn.accelerate);
//        strBuilder.append(",");
//        strBuilder.append(toReturn.brake);
//        strBuilder.append(",");
        
        //strBuilder.append(toReturn.gear);
        //strBuilder.append(",");


        
        System.out.println(strBuilder.toString());
        return toReturn;
    }
    
    @Override
    public void reset() {
        //System.out.println("Restarting the race!");
        
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
        gearFis.evaluate();
        return new Double(Math.floor(gearFis.getVariable("gear").getValue())).intValue();
        */
        
        
        
                
                
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
        if (gear < 6 && rpm >= gearUp[gear - 1]) {
            return gear + 1;
        } else // check if the RPM value of car is lower than the one suggested 
        // to shift down the gear from the current one
        if (gear > 1 && rpm <= gearDown[gear - 1]) {
            return gear - 1;
        } else // otherwhise keep current gear
        {
            return gear;
        }
        

    }
    
    @Override
    public void setGearsPreferences(GearPreference gp){
        gearUp = convertIntegers(gp.gearUps);
        gearDown = convertIntegers(gp.gearDowns);
    }
    
    @Override
    public void setRacingLine(RacingLine rl){
        racingLine = rl;
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
}