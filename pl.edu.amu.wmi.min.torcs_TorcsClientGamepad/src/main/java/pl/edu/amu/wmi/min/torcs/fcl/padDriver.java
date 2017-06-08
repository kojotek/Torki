package pl.edu.amu.wmi.min.torcs.fcl;
import java.util.List;
import net.sourceforge.cig.torcs.Controller;
import net.sourceforge.cig.torcs.Action;
import net.sourceforge.cig.torcs.SensorModel;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class padDriver extends Controller {

    boolean pulse;
    net.java.games.input.Controller pad;
    private final float[] angles;
    private List<Point2D.Double> points;
    /* Gear Changing Constants*/
    private  int[] gearUp = {9000, 8000, 8000, 8000, 8000, 0};
    private  int[] gearDown = {0, 2500, 3000, 3000, 3500, 3500};
    private int steeringRawInput, accelerationRawInput = 0;
    private boolean gasPressed, brakePressed = false;
    private float smoothedSteering = 0.0f;
    private RacingLine racingLine;
    
    Action toReturn;
    boolean firstLoop;
    double roadWidth = 0.0f;
    int racingLineIndex = 0;
    
    Visualisation visualisation;
    
    public padDriver() {

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
        
        
        points = new ArrayList<Point2D.Double>();

        System.out.println(""
                + "track -90,track -75,track -60,"
                + "track -45,track -30,track -20,"
                + "track -15,track -10,track -5,"
                + "track 0,"
                + "track 5,track 10,track 15,"
                + "track 20,track 30,track 45,"
                + "track 60,track 75,track 90,"
                + "trackPosition,"
                + "speed,"
                + "distanceFromStartLine,"
                + "angle,"
                + "input steering,"
                + "steering,"
                + "input acceleration,"
                + "gas,"
                + "brake,"
                + "gear");

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
            accelerationRawInput = 1;
        }
        else if (brakePressed){
            accelerationRawInput = -1;
        }
        
        toReturn.gear = getGear(sensors, accelerationRawInput);
        
        switch (accelerationRawInput){
            case(0):
                toReturn.accelerate = 0.0f;
                toReturn.brake = 0.0f;
                break;
            case(1):
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

        double[] trackEdge = sensors.getTrackEdgeSensors();
        
        
        points.clear();

        //computing x,y points
        double radians;
        for (int i = 0; i < trackEdge.length; i++) {
            radians = Math.toRadians(angles[i]);
            points.add(new Point2D.Double(trackEdge[i] * Math.sin(radians), trackEdge[i] * Math.cos(radians)));
        }
        
        
        
        //computing angles between points
        ArrayList<Double> borderAngles = new ArrayList<>();
        borderAngles.add(new Double(Math.PI));
        
        for (int i = 1; i < points.size()-1; i++) {
            double abcAngle = 
                    Math.atan2(points.get(i+1).y - points.get(i).y, points.get(i+1).x - points.get(i).x)
                    - Math.atan2(points.get(i-1).y - points.get(i).y, points.get(i-1).x - points.get(i).x);
            
            borderAngles.add(Math.abs(abcAngle));    
        }
        
        borderAngles.add(new Double(Math.PI));

        //trying to realize, which point belongs to which band
        ArrayList<Point2D.Double> leftPoints = new ArrayList<>();
        ArrayList<Point2D.Double> rightPoints = new ArrayList<>();
        
        double angleLimit = 3*Math.PI/4.0f;
        
        for (int i = 0; i < points.size(); i++) {
            if(trackEdge[i] < 199.9f
                    && (i == 0 || Point2D.distance(points.get(i).x, points.get(i).y, points.get(i-1).x, points.get(i-1).y) <= roadWidth * 1.5f)
                    //&& (i == 0 || borderAngles.get(i-1) > angleLimit)
            ){
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
            ){
                rightPoints.add(points.get(i));
                if (i >= points.size() && borderAngles.get(i+1) > angleLimit){
                    leftPoints.remove(leftPoints.size()-1);
                }
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
        //List<Point2D.Double> longerSet = rightPoints;//(leftLength > rightLength ? leftPoints : rightPoints);
        ArrayList<Point2D.Double> estimatedSet = new ArrayList<>();
        
        
        
        
        
        
        
        
        
        
        //estimating right side
        if(left){
            
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
            newLeftPoints.add(leftPoints.get(leftPoints.size()-1));
            leftPoints = newLeftPoints;
            
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
            newRightPoints.add(rightPoints.get(rightPoints.size()-1));
            rightPoints = newRightPoints;
            

            
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
        
        double distanceFromStartLine = sensors.getDistanceFromStartLine();

        ArrayList<Point2D.Double> racingLinePoints = new ArrayList<>();
        double distanceFromCar = 0.0f;
        
        double previousCenterOfPointX = 0.0f;
        double previousCenterOfPointY = 0.0f;
        
        if(leftPoints.size() == rightPoints.size()){
            
            if (!leftPoints.isEmpty()){
                previousCenterOfPointX = (rightPoints.get(0).x + leftPoints.get(0).x) / 2.0f;
                previousCenterOfPointY = (rightPoints.get(0).y + leftPoints.get(0).y) / 2.0f;
            }
            for (int i = 0; i < leftPoints.size(); i++) {
                
                double centerOfPointX = (rightPoints.get(i).x + leftPoints.get(i).x) / 2.0f;
                double centerOfPointY = (rightPoints.get(i).y + leftPoints.get(i).y) / 2.0f;
                
                distanceFromCar += Point2D.distance(previousCenterOfPointX, previousCenterOfPointY, centerOfPointX, centerOfPointY);
                
                previousCenterOfPointX = centerOfPointX;
                previousCenterOfPointY = centerOfPointY;
                
                double racingLinePosition = racingLine.GetFirstPointAfter(distanceFromStartLine + distanceFromCar);
                racingLinePoints.add(
                        new Point2D.Double(
                            leftPoints.get(i).x + (rightPoints.get(i).x - leftPoints.get(i).x) * (1.0f-((racingLinePosition+1.0f)/2.0f)),
                            leftPoints.get(i).y + (rightPoints.get(i).y - leftPoints.get(i).y) * (1.0f-((racingLinePosition+1.0f)/2.0f))));
            }
        }
        else{
            System.err.println("nierowne krawedzie!");
        }
        
        visualisation.Redraw(leftPoints, rightPoints, racingLinePoints);
        
        double trackPosition = sensors.getTrackPosition();
        //fis.setVariable("trackPos", trackPosition);

        // Speed (x) (−∞,+∞) (km/h) 
        // Speed of the car along the longitudinal axis of the car.
        double speed = sensors.getSpeed();
        //fis.setVariable("speed", speed);


//        double[] opponents = sensors.getOpponentSensors();
        

//for (int i = 0; i < opponents.length; i++) {
        //    fis.setVariable("opponent" + i, opponents[i]);
        //}

        // angle [-π,+π] (rad) 
        // Angle between the car direction and the direction of the
        // track axis.
        double angle = sensors.getAngleToTrackAxis();

        //fis.setVariable("angle", angle);

        //fis.evaluate();

        //Variable steering = fis.getVariable("steering");
        //Virtual gas pedal (0 means no gas, 1 full gas). Range [0,1] 
        //Variable accelerate = fis.getVariable("accelerate");
        // Virtual brake pedal (0 means no brake, 1 full brake). Range [0,1]
        //Variable brake = fis.getVariable("brake");
        
        

        //toReturn.steering = steering.getValue();
        //toReturn.accelerate = accelerate.getValue();
        //toReturn.brake = brake.getValue();

        StringBuilder strBuilder = new StringBuilder();
        
        for (int i = 0; i < angles.length; i++) {
            strBuilder.append(trackEdge[i]);
            strBuilder.append(",");
        }
        
        strBuilder.append(trackPosition);
        strBuilder.append(",");
        strBuilder.append(speed);
        strBuilder.append(",");
        strBuilder.append(distanceFromStartLine);
        strBuilder.append(",");
        strBuilder.append(angle);
        strBuilder.append(",");
        
        strBuilder.append(steeringRawInput);
        strBuilder.append(",");
        strBuilder.append(toReturn.steering);
        strBuilder.append(",");
        strBuilder.append(accelerationRawInput);
        strBuilder.append(",");
        strBuilder.append(toReturn.accelerate);
        strBuilder.append(",");
        strBuilder.append(toReturn.brake);
        strBuilder.append(",");
        strBuilder.append(toReturn.gear);
        strBuilder.append(",");

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
}