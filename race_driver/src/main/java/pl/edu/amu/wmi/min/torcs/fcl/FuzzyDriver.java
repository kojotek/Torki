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

public class FuzzyDriver extends Controller {
    
    double ABS_MINSPEED = 3.0;    /* [m/s] */
    double ABS_SLIP = 0.9;
    
    double TCL_SLIP = 0.9;        /* [-] range [0.95..0.3] */
    double TCL_MINSPEED = 3.0;    /* [m/s] */
    
    int FRONT_RIGHT = 0;
    int FRONT_LEFT = 1;
    int REAR_RIGHT = 2;
    int REAR_LEFT = 3;
    
    boolean useVisualisation = false;
    
    int raceNumber = 1;
    private Double trackLength;
    double roadWidth = 0.0f;
    double framesCounter = 0.0f;
    double relativeDistancesFromRacingLineSum = 0.0f;
    double relativeSquareDistancesFromRacingLineSum = 0.0f;
    double actualDistancesFromRacingLineSum = 0.0f;
    double actualSquaredDistancesFromRacingLineSum = 0.0f;
    double safeDistanceFromEdgeSum = 0.0f;
    int lapsDone = 0;
    Set<String> variablesInUseSteering;
    Set<String> variablesInUseAccel; 
    private int steeringCommand, accelerationCommand = 0;
    private int fuzzyControllerSteering = 0;
    double currentFrameTime, lastFrameTime;   
    boolean firstFrame;
    int racingLineIndex = 0;
    boolean raceFinished = false;
    double[] edges;
    final float[] angles;
    double[] trackEdge;
    double leftLength = 0.0f;
    double rightLength = 0.0f;
    double distanceFromStartLine;
    double bestLapTime = 0.0f;
    
    RacingLine racingLine;
    GearPreference gearPreference;
    FIS steeringFis;
    Action toReturn;
    Visualisation visualisation;
    DecimalFormat doubleFormatter;  

    ArrayList<Point2D.Double> leftPoints;
    ArrayList<Point2D.Double> rightPoints;
    List<Point2D.Double> points;
    ArrayList<Point2D.Double> racingLinePoints;
    ArrayList<Point2D.Double> centerLinePoints;
    
    public FuzzyDriver(FIS strFis, String fileName, boolean useVisuals){
        
        resetState();
        
        setFIS(strFis);
        
        doubleFormatter = new DecimalFormat("0.0000");
        DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
        sym.setDecimalSeparator('.');
        doubleFormatter.setDecimalFormatSymbols(sym);
        
        toReturn = new Action();
        
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
        leftPoints = new ArrayList<>();
        rightPoints = new ArrayList<>();
        racingLinePoints = new ArrayList<>();
        centerLinePoints = new ArrayList<>();

        useVisualisation = useVisuals;
        
        if(useVisualisation){
            visualisation = new Visualisation();
        }
        
        /*
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
        );
*/
    }
    
    
    @Override
    public Action control(SensorModel sensors) {
        
            framesCounter++;

            if(firstFrame){
                edges = sensors.getTrackEdgeSensors();

                roadWidth = edges[0] + edges[edges.length-1];
                firstFrame = false;
            }

            trackEdge = sensors.getTrackEdgeSensors();

            points.clear();
            leftPoints.clear();
            rightPoints.clear();
            
            //computing x,y points
            double radians;
            for (int i = 0; i < trackEdge.length; i++) {
                radians = Math.toRadians(angles[i]);
                points.add(new Point2D.Double(trackEdge[i] * Math.sin(radians), trackEdge[i] * Math.cos(radians)));
            }

            //trying to realize, which point belongs to which band
            for (int i = 0; i < points.size(); i++) {
                if(trackEdge[i] < 199.9f && (i == 0 || Point2D.distance(points.get(i).x, points.get(i).y, points.get(i-1).x, points.get(i-1).y) <= roadWidth * 1.5f)){
                    leftPoints.add(points.get(i));
                }
                else{
                    break;
                }
            }

            for (int i = points.size()-1; i >= leftPoints.size(); i--) {
                if (trackEdge[i] < 199.9f && (i == points.size()-1 || Point2D.distance(points.get(i).x, points.get(i).y, points.get(i+1).x, points.get(i+1).y) <= roadWidth)){
                    rightPoints.add(points.get(i));
                }
                else{
                    break;
                }
            }

            leftLength = 0.0f;
            rightLength = 0.0f;

            for (int i = 1; i < leftPoints.size(); i++) {
                leftLength += Point2D.distance(leftPoints.get(i).x, leftPoints.get(i).y, leftPoints.get(i-1).x, leftPoints.get(i-1).y);
            }

            for (int i = 1; i < rightPoints.size(); i++) {
                rightLength += Point2D.distance(rightPoints.get(i).x, rightPoints.get(i).y, rightPoints.get(i-1).x, rightPoints.get(i-1).y);
            }

            //extending points
            boolean leftEdgeLonger = leftLength > rightLength;
            ArrayList<Point2D.Double> estimatedSet = new ArrayList<>();


            //estimating shorter edge
            ArrayList<Point2D.Double> longerEdge;
            
            float castingDirection = 0;
            
            if(leftEdgeLonger){
                leftPoints = ChangeLineResoultion(leftPoints, 2.0f);
                longerEdge = leftPoints;
                castingDirection = -1;
            }
            else{
                rightPoints = ChangeLineResoultion(rightPoints, 2.0f);
                longerEdge = rightPoints;
                castingDirection = 1;
            }

            
            if(longerEdge.size() >= 2){
                double firstPointAngle = -Math.atan2(longerEdge.get(1).y - longerEdge.get(0).y, longerEdge.get(1).x - longerEdge.get(0).x);
                Point2D.Double firstPoint = new Point2D.Double();

                firstPoint.x = castingDirection * roadWidth * Math.sin(firstPointAngle) + longerEdge.get(0).x;
                firstPoint.y = castingDirection * roadWidth * Math.cos(firstPointAngle) + longerEdge.get(0).y;

                
                estimatedSet.add(firstPoint);
            }

            for (int i = 1; i < longerEdge.size()-1; i++) {

                Point2D.Double oppositePoint = new Point2D.Double();

                double bisectorAngle;
                
                double ab = Math.atan2(longerEdge.get(i+1).y - longerEdge.get(i).y, longerEdge.get(i+1).x - longerEdge.get(i).x);
                double bc = Math.atan2(longerEdge.get(i).y - longerEdge.get(i-1).y, longerEdge.get(i).x - longerEdge.get(i-1).x);
                bisectorAngle = (ab + bc) / 2.0f;
                bisectorAngle = -bisectorAngle;

                oppositePoint.x = castingDirection * roadWidth * Math.sin(bisectorAngle) + longerEdge.get(i).x;
                oppositePoint.y = castingDirection * roadWidth * Math.cos(bisectorAngle) + longerEdge.get(i).y;

                estimatedSet.add(oppositePoint);
            }

            if(longerEdge.size() >= 2){
                double lastPointAngle = -Math.atan2(longerEdge.get(longerEdge.size()-1).y - longerEdge.get(longerEdge.size()-2).y,
                longerEdge.get(longerEdge.size()-1).x - longerEdge.get(longerEdge.size()-2).x);
                Point2D.Double lastPoint = new Point2D.Double();
                
                lastPoint.x = castingDirection * roadWidth * Math.sin(lastPointAngle) + longerEdge.get(longerEdge.size()-1).x;
                lastPoint.y = castingDirection * roadWidth * Math.cos(lastPointAngle) + longerEdge.get(longerEdge.size()-1).y;

                estimatedSet.add(lastPoint);
            }

            if(leftEdgeLonger){
                rightPoints = estimatedSet;
            }
            else{
                leftPoints = estimatedSet;
            }
            


            ArrayList<Double> leftPointsX = GetEdgeX(leftPoints, 5, 6);
            ArrayList<Double> rightPointsX = GetEdgeX(rightPoints, 5, 6);


            //putting racing line on the road
            distanceFromStartLine = sensors.getDistanceFromStartLine();

            racingLinePoints.clear();
            centerLinePoints.clear();
            double distanceFromCar = 0.0f;

            double previousCenterOfPointX = 0.0f;
            double previousCenterOfPointY = 0.0f;

            double relativeDistanceFromRacingLine = sensors.getTrackPosition() - racingLine.GetFirstPointAfter(distanceFromStartLine);
            double actualDistanceFromRacingLine = relativeDistanceFromRacingLine * (roadWidth/2.0f);

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
                System.err.println("blad, nierowne krawedzie!");
            }


            if(useVisualisation){
                visualisation.Redraw(leftPoints, rightPoints, racingLinePoints); 
            }
    

            double meanCurve = 0.0f;
            double angle = sensors.getAngleToTrackAxis() * (180.0f/Math.PI);
            double speed = sensors.getSpeed();
            double sideSpeed = sensors.getLateralSpeed();
            double trackPosition = sensors.getTrackPosition();
            int outOfTrack = trackEdge[0] < 0.1f ? 1 : 0;


            ArrayList<Double> racingLineX = GetEdgeX(racingLinePoints, 5, 6);
            /*
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
            */

            int unknownSteeringSituation = 0;

            for (int i = 0; i < racingLineX.size(); i++) {
                steeringFis.setVariable("racingLine" + Integer.toString((i)*5), racingLineX.get(i));
                steeringFis.setVariable("leftWall" + Integer.toString((i)*5), leftPointsX.get(i));
                steeringFis.setVariable("rightWall" + Integer.toString((i)*5), rightPointsX.get(i));
            }

            steeringFis.evaluate();
            
            double rawSteer = steeringFis.getVariable("inputSteering").getValue();

            if (rawSteer > 90.0f){
                unknownSteeringSituation = 1;
                //fuzzyControllerSteering = 0;
            }
            else if(rawSteer > 0.33f){
                fuzzyControllerSteering = 1;
            } else if (rawSteer < -0.33f){
                fuzzyControllerSteering = -1;
            } else {
                fuzzyControllerSteering = 0;
            }
            
            steeringCommand = fuzzyControllerSteering;

            
            double targetSpeed = racingLine.GetSpeedLimitAfter(distanceFromStartLine);
            double speedDifference = speed - targetSpeed;
            if(speedDifference > 10.0f){
                accelerationCommand = -1;
            }
            else if (speedDifference < 10.0f && speedDifference >= 0.0f ){
                accelerationCommand = 1;
            }
            else if (speedDifference < 0.0f){
                accelerationCommand = 2;
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

            //ABS && TCL
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

            
            double distanceRaced = sensors.getDistanceRaced();

            double minTrackEdge = 201.0f;

            for (int i = 0; i < trackEdge.length; i++) {
                if(trackEdge[i] < minTrackEdge){
                    minTrackEdge = trackEdge[i];
                }
            }
            
            lapsDone = (int)Math.floor(distanceRaced/trackLength);

            double trackCompleted = (lapsDone >= (Consts.lapsPerCandidate) ? Consts.trackCompletedBonus : Consts.trackNotCompletedBonus);
            relativeDistancesFromRacingLineSum += Math.abs(relativeDistanceFromRacingLine);
            relativeSquareDistancesFromRacingLineSum += (relativeDistanceFromRacingLine * relativeDistanceFromRacingLine);
            actualDistancesFromRacingLineSum += Math.abs(actualDistanceFromRacingLine);
            actualSquaredDistancesFromRacingLineSum += actualDistanceFromRacingLine * actualDistanceFromRacingLine;
            if(minTrackEdge > Consts.safeDistance){
                safeDistanceFromEdgeSum += 1.0f;
            }


            double racingLineAverageDist = relativeDistancesFromRacingLineSum/framesCounter;            
            double racingLineAverageSquaredDist = relativeSquareDistancesFromRacingLineSum/framesCounter;
            double actualRacingLineAverageDist = actualDistancesFromRacingLineSum/framesCounter;
            double actualRacingLineAverageSquaredDist = actualSquaredDistancesFromRacingLineSum/framesCounter;
            double safeDistanceAverage = safeDistanceFromEdgeSum/framesCounter;

            double score = (distanceRaced + distanceRaced * (1.0 - racingLineAverageSquaredDist) + distanceRaced * Math.pow(safeDistanceAverage, 10.0f)) * trackCompleted;

            if(sensors.getLastLapTime() == 0.0f){
                bestLapTime = 0.0f;
            }
            else if(sensors.getLastLapTime() > 0.0f && (sensors.getLastLapTime() < bestLapTime || bestLapTime == 0.0f)){
                bestLapTime = sensors.getLastLapTime();
            }
            
            if(sensors.getLastLapTime() > 0.0f && sensors.getLastLapTime() < bestLapTime){
                bestLapTime = sensors.getLastLapTime();
            }

            if (sensors.getDamage() > Consts.maxDamage || trackEdge[9] <= 0.0f || lapsDone >= Consts.lapsPerCandidate || framesCounter > Consts.maxTicksPerCandidate ){
                
                    if(!raceFinished){
                        System.err.println("race number " + raceNumber + ":");
                        System.err.println("  score................................... " + doubleFormatter.format(score));
                        System.err.println("  distance raced.......................... " + doubleFormatter.format(distanceRaced));
                        System.err.println("  race end location....................... " + doubleFormatter.format(sensors.getDistanceFromStartLine()));
                        System.err.println("  avg distance from racing line........... " + doubleFormatter.format(actualRacingLineAverageDist));
                        System.err.println("  avg squared distance from racing line... " + doubleFormatter.format(actualRacingLineAverageSquaredDist));
                        System.err.println("  all wheels on the road.................. " + doubleFormatter.format(safeDistanceAverage*100.0f));
                        System.err.println("  laps completed.......................... " + lapsDone);
                        System.err.println("  race completed.......................... " + (lapsDone >= (Consts.lapsPerCandidate) ? "yes" : "no"));
                        System.err.println("  best lap time........................... " + doubleFormatter.format(bestLapTime));
                        System.err.println("");
                    }
                
                
                raceFinished = true;
                
                toReturn.restartRace = true;
            }

            return toReturn;

           
    }
    
    @Override
    public void reset() {
        raceNumber++;
        resetState();
    }
    
    @Override
    public void shutdown() {
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


    
    private void resetState(){
        toReturn = new Action();
        framesCounter = 0.0f;
        relativeDistancesFromRacingLineSum = 0.0f;
        relativeSquareDistancesFromRacingLineSum = 0.0f;
        actualDistancesFromRacingLineSum = 0.0f;
        actualSquaredDistancesFromRacingLineSum = 0.0f;
        safeDistanceFromEdgeSum = 0.0f;
        lapsDone = 0;
        firstFrame = true;
        roadWidth = 0.0f;
        racingLineIndex = 0;
        raceFinished = false;
        bestLapTime = Double.MAX_VALUE;
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
    
    /*
    private int[] convertIntegers(List<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = integers.get(i);
        }
        return ret;
    }
    */

    void setFIS(FIS steeringFis) {
        this.steeringFis = steeringFis;
        variablesInUseSteering = this.steeringFis.getFunctionBlock("fb").getVariables().keySet();
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
    
    ArrayList<Double> GetEdgeX( ArrayList<Point2D.Double> originalPoints, int step, int resultListSize){
        ArrayList<Double> pointsX = new ArrayList<>();
        
        for (int i = 0; i <= resultListSize*step; i+=step) {
                if (i < originalPoints.size() ){

                    boolean found = false;
                    for (int j = 0; j < originalPoints.size(); j++) {
                        if(originalPoints.get(j).y >= i){
                            pointsX.add(originalPoints.get(i).x);
                            found = true;
                            break;
                        }
                    }
                    if (!found){
                        if (pointsX.isEmpty()){
                            pointsX.add(new Double(0.0f));
                        }
                        else{
                            pointsX.add(pointsX.get(pointsX.size()-1));
                        }
                    }

                }
                else if (pointsX.size() >= 2){
                    pointsX.add(pointsX.get(pointsX.size()-1) + (pointsX.get(pointsX.size()-1) - pointsX.get(pointsX.size()-2)));
                }
                else if (pointsX.size() == 1){
                    pointsX.add(pointsX.get(pointsX.size()-1));
                }
                else {
                    pointsX.add(new Double(0.0f));
                }
            }
        return pointsX;
    }

}