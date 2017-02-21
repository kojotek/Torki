/*
    Copyright (C) 2016 Patryk Żywica

    This file is part of Torcs FCL Client for Java.

    Torcs FCL Client for Java is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Torcs FCL Client for Java is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package pl.edu.amu.wmi.min.torcs.fcl;

import java.util.List;
import net.sourceforge.cig.torcs.Controller;
import net.sourceforge.cig.torcs.Action;
import net.sourceforge.cig.torcs.SensorModel;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.RuleBlock;
import net.sourceforge.jFuzzyLogic.rule.Variable;


public class FuzzyDriver extends Controller {
    
    private final RuleBlock functionBlock;
    private final float[] angles;
    /* Gear Changing Constants*/
    private  int[] gearUp = {9000, 8000, 8000, 8000, 8000, 0};
    private  int[] gearDown = {0, 2500, 3000, 3000, 3500, 3500};
    boolean pulse = true;
    
        public FuzzyDriver(RuleBlock fBlock) {
        this.functionBlock = fBlock;
        
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
    }
    
    @Override
    public Action control(SensorModel sensors) {
        Action toReturn = new Action();
        
        // Set the input
        // Track [0,200] (m)
        // Vector of 19 range finder sensors: each sensors returns the
        // distance between the track edge and the car within a range
        // of 200 meters. When noisy option is enabled (see Section
        // 7), sensors are affected by i.i.d. normal noises with a
        // standard deviation equal to the 10% of sensors range. By
        // default, the sensors sample the space in front of the car every
        // 10 degrees, spanning clockwise from -90 degrees up to
        // +90 degrees with respect to the car axis. However, the con-
        // figuration of the range finder sensors (i.e., the angle w.r.t.
        // to the car axis) can be set by the client once during initialization,
        // i.e., before the beginning of each race. When the
        // car is outside of the track (i.e., pos is less than -1 or greater
        // than 1), the returned values are not reliable (typically -1 is
        // returned).
        double[] trackEdge = sensors.getTrackEdgeSensors();
        for (int i = 0; i < angles.length; i++) {
            functionBlock.setVariable("track" + i, trackEdge[i]);
        }

        double[] curvePredictions = new double[trackEdge.length/2];
        for (int i = 10; i < trackEdge.length; i++) {
            double curvePrediction = Math.log10(trackEdge[i]/trackEdge[18-i]);
            curvePredictions[i-10] = curvePrediction;
            functionBlock.setVariable("curvePred" + (i-10), curvePrediction);
        }
        
        // Track pos (−∞,+∞)
        // Distance between the car and the track axis. The value is
        // normalized w.r.t to the track width: it is 0 when car is on
        // the axis, -1 when the car is on the right edge of the track
        // and +1 when it is on the left edge of the car. Values greater
        // than 1 or smaller than -1 mean that the car is outside of
        // the track.
        
        double trackPosition = sensors.getTrackPosition();
        functionBlock.setVariable("trackPos", trackPosition);

        // Speed (x) (−∞,+∞) (km/h) 
        // Speed of the car along the longitudinal axis of the car.
        double speed = sensors.getSpeed();
        functionBlock.setVariable("speed", speed);

        double distanceRaced = sensors.getDistanceRaced();
        functionBlock.setVariable("distanceRaced", distanceRaced);
        
        // Opponents [0,200] (m)
        // Vector of 36 opponent sensors: each sensor covers a span
        // of 10 degrees within a range of 200 meters and returns the
        // distance of the closest opponent in the covered area. When
        // noisy option is enabled (see Section 7), sensors are affected
        // by i.i.d. normal noises with a standard deviation equal to
        // the 2% of sensors range. The 36 sensors cover all the space
        // around the car, spanning clockwise from -180 degrees up to
        // +180 degrees with respect to the car axis.
        double[] opponents = sensors.getOpponentSensors();
        for (int i = 0; i < opponents.length; i++) {
            functionBlock.setVariable("opponent" + i, opponents[i]);
        }

        // angle [-π,+π] (rad) 
        // Angle between the car direction and the direction of the
        // track axis.
        double angle = sensors.getAngleToTrackAxis();
        
        functionBlock.setVariable("angle", angle);

        functionBlock.evaluate();

        // set the output
        // Steering value: -1 and +1 means respectively full right and
        // left, that corresponds to an angle of 0.366519 rad. Range [-1,1]
        Variable steering = functionBlock.getVariable("steering");
        // Virtual gas pedal (0 means no gas, 1 full gas). Range [0,1] 
        Variable accelerate = functionBlock.getVariable("accelerate");
        // Virtual brake pedal (0 means no brake, 1 full brake). Range [0,1]
        Variable brake = functionBlock.getVariable("brake");
        
        toReturn.steering = steering.getValue();
        toReturn.accelerate = accelerate.getValue();
        //toReturn.brake = brake.getValue();
        double brakeRaw = brake.getValue();
        toReturn.gear = getGear(sensors);
        
        //add pulstaion
        if (pulse){
            toReturn.brake = brakeRaw;
            pulse = false;
        }
        else{
            toReturn.brake = 0.0f;
            pulse = true;
        }
        
        return toReturn;
    }
    
    @Override
    public void reset() {
        System.out.println("Restarting the race!");
        
    }
    
    @Override
    public void shutdown() {
        System.out.println("Bye bye!");
    }
    
    @Override
    public float[] initAngles() {
        return angles;
    }
    
    private int getGear(SensorModel sensors) {
        int gear = sensors.getGear();
        double rpm = sensors.getRPM();

        // if gear is 0 (N) or -1 (R) just return 1 
        if (gear < 1) {
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