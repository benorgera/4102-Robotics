package com.qualcomm.ftcrobotcontroller.autonomous;

import com.qualcomm.ftcrobotcontroller.systems.ClimberDepositor;
import com.qualcomm.ftcrobotcontroller.systems.MyDirection;
import com.qualcomm.ftcrobotcontroller.systems.Necessities;
import com.qualcomm.ftcrobotcontroller.systems.Wheels;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;
import java.util.ArrayList;

/**
 * Created by benorgera on 12/22/15.
 */

public class AutonomousImplementation {

    private Necessities n;
    private LinearOpMode opMode;
    private Wheels wheels;
    private OpticalDistanceSensor ods;
    private ColorSensor leftBottom;
    private ColorSensor rightBottom;
    private MyDirection color;
    private ClimberDepositor depositor;
    private GyroSensor gyro;
    private Stage stage = Stage.SENSOR_CONTACT_AND_DEBRIS_SWEEP;

    private String data = ""; //the data to be printed to the phone

    private int whiteSignalThreshold = 10; //this signal must be read by color sensors for them to be considered as on the white line
    
    private double distanceThreshold = 0.03; //this signal must be read by the ODS for it to be considered as touching the beacon

    private ArrayList<Integer> gyroReadings = new ArrayList<Integer>(); //gyro readings

    private int readingsNum = 100; //number of readings the gyro takes and are averaged

    private double gyroConstantMax = 1.4; //the max the coefficient can be

    private double gyroConstant = 1.0; //coefficient of driving

    private int gyroFreezeThreshold = 95; //the gyro reading needed to signify that the robot isn't moving

    public AutonomousImplementation(Necessities n, Wheels wheels, OpticalDistanceSensor ods, ColorSensor leftBottom, ColorSensor rightBottom, ClimberDepositor depositor, GyroSensor gyro, MyDirection color, LinearOpMode opMode) {
        this.n = n;
        this.ods = ods;
        this.wheels = wheels;
        this.color = color;
        this.gyro = gyro;
        this.depositor = depositor;
        this.leftBottom = leftBottom;
        this.rightBottom = rightBottom;
        this.opMode = opMode;
    }

    public void run() {



        while (ods.getLightDetected() < distanceThreshold && opMode.opModeIsActive()) { //once we have reached the beacon, this condition is no longer true

            //green is used as a measure of the white line because blue and red are found on the field, whereas green is not
            int left = leftBottom.green();
            int right = rightBottom.green();

            //primary: first sensor to cross the white line
            //secondary: second sensor to cross the white line
            int primary = color == MyDirection.BLUE ? left : right;
            int secondary = color == MyDirection.BLUE ? right : left;

            switch (stage) {
                case SENSOR_CONTACT_AND_DEBRIS_SWEEP:
                    processSensorContactAndDebrisSweep(left, right);
                    break;
                case PRIMARY_SENSOR_CONTACT:
                    processPrimarySensorContact(primary);
                    break;
                case SECONDARY_SENSOR_CONTACT:
                    processSecondSensorContact(primary, secondary);
                    break;
                case WAITING_FOR_PRIMARY_TO_SURPASS_SECONDARY:
                    processWaitingForPrimaryToSurpassSecondary(primary, secondary);
                    break;
                case DUAL_SENSOR_CONTACT:
                    processDualSensorContact(left, right);
                    break;
            }

            n.syso(data, "DATA");
            n.syso("" + ods.getLightDetected(), "ODS");
            n.syso("" + left, "LEFT");
            n.syso("" + right, "RIGHT");
            n.syso("" + gyroConstant, "GYRO CONSTANT");

            checkGyro();

            n.waitCycle();
        }

        if (!opMode.opModeIsActive()) { //end execution if autnomomous is active
            wheels.stop();
            return;
        }

        wheels.stop();

        n.syso("Beacon Reached", "DATA");

        depositor.swing(); //swing out the preload arm over the bin

        n.syso("Preload Arm Deployed", "DATA");

        asymptoticTurn(color == MyDirection.RED ? -25 : -13); //compensate for the climber depositor arm being on the right side of the robot

        n.sleep(1500); //wait for the robot to stabilize

        depositor.drop(); //drop the climbers

        n.syso("Climbers Dropped", "DATA");

    }

    private void processSensorContactAndDebrisSweep(int left, int right) { //wait until the white line is reached, then push debris out of the way
        if (left > whiteSignalThreshold || right > whiteSignalThreshold) { //one of the sensors is on the white line
            wheels.stop();
            data = "Made Sensor Contact And Swept Debris";
            n.sleep(500); //wait for robot to stop
            driveByTime(2500, MyDirection.UP); //drive forward to push accumulated debris out of the way
            stage = Stage.PRIMARY_SENSOR_CONTACT;
        } else { //neither sensor is on the white line
            strongForward();
            data = "Waiting For Sensor Contact";
        }

    }

    private void processPrimarySensorContact(int primary) { //back up until primary sensor is on the white line again

        if (primary > whiteSignalThreshold) { //primary sensor is on the white line
            wheels.stop();
            data = "Made Primary Sensor Contact";
            stage = Stage.SECONDARY_SENSOR_CONTACT;
            n.sleep(500); //wait for robot to stop
        } else { //primary sensor is not yet on the white line
            strongReverse();
            data = "Waiting For Primary Sensor Contact";
        }

    }

    private void processSecondSensorContact(int primary, int secondary) { //drive forward until secondary sensor is on the white line

        if (secondary > whiteSignalThreshold && primary < whiteSignalThreshold) { //secondary sensor is on the white line
            wheels.stop();
            data = "Made Secondary Sensor Contact";
            stage = Stage.WAITING_FOR_PRIMARY_TO_SURPASS_SECONDARY;
            n.sleep(500); //wait for robot to stop
        } else { //secondary sensor is not yet on the white line
            weakForward();
            data = "Waiting For Secondary Sensor Contact";
        }

    }

    private void processWaitingForPrimaryToSurpassSecondary(int primary, int secondary) { //turn until primary sensor reading surpasses secondary sensor reading (until the robot straightens out)

        if (primary > (secondary + 6)) { //robot parallel to the line or has surpassed parallel
            wheels.stop();
            data = "Primary Surpassed Secondary";
            stage = Stage.DUAL_SENSOR_CONTACT;
            n.sleep(500); //wait for robot to stop
        } else { //robot not yet parallel to the line
            if (color == MyDirection.BLUE) {
                strongRight();
            } else {
                strongLeft();
            }
            data = "Waiting For Primary To Surpass Secondary";
        }

    }

    private void processDualSensorContact(int left, int right) { //turn towards the stronger sensor signal (in order to inch the rest of thw way to the beacon)

        if (Math.abs(left - right) <= 1) { //both sensors equally on the white line
            strongForward();
            data = "Dual Sensor Contact Close To Equal";
        } else if (left > right) { //left more on the white line
            slightLeft();
            data = "Dual Sensor Contact Left Stronger";
        } else { //right more on the white line
            slightRight();
            data = "Dual Sensor Contact Right Stronger";
        }

    }

    private void driveByTime(long ms, MyDirection direction) { //drives robot forward or backward for a given amount of time

        long startTime = System.currentTimeMillis(); //initial time

        while (System.currentTimeMillis() - startTime < ms && opMode.opModeIsActive()) { //drive until the time reading increases by the specified amount of milliseconds
            if (direction == MyDirection.DOWN) {
                strongReverse();
            } else if (direction == MyDirection.UP){
                strongForward();
            } else if (direction == MyDirection.RIGHT) {
                strongRight();
            } else {
                strongLeft();
            }
            n.waitCycle();
        }

        wheels.stop();
    }

    private double getAverageOdsReading(int cycles) { //get average reading of n ODS cycles

        double res = 0.0;

        for (int i = 0; i < cycles; i++) {
            res += ods.getLightDetected();
            n.waitCycle();
        }

        return res / cycles;
    }

    private void checkGyro() { //if the robot isn't moving (its stuck on debris), up gyroConstant (a coefficient of the driving power) in order to drive with more power

        gyroReadings.add(Math.abs(gyro.rawX()) + Math.abs(gyro.rawY()));

        if (gyroReadings.size() == readingsNum) { //we have our 400 readings

            gyroReadings.remove((int) 0); //remove the first reading of the 100

            gyroConstant = getGyroReadingsAverage() < gyroFreezeThreshold ? (gyroConstant + 0.05) : (gyroConstant - 0.05); //up the gyro constant if its moving too slowly, lower it if its moving

            if (gyroConstant < 1.0) gyroConstant = 1.0; //clip the gyro constant so ot doesn't go too slow

            if (gyroConstant > gyroConstantMax) gyroConstant = gyroConstantMax; //clip the gyro constant so we don't set the speed too high

        }
    }

    private void asymptoticTurn(double threshold) { //turns threshold degrees using the gyro, slowing until it reaches the destination, (right is positive, left is negative)

        forceHeadingReset(); //reset the gyro turn value

        double power;

        double minPower = 0.19;

        boolean thresholdIsGreaterThanZero = (threshold > 0.0);

        double startSlowingAt = 4.0 / minPower;

        double holdingPower = -0.3;

        threshold = thresholdIsGreaterThanZero ? threshold + 180 : threshold - 180;  //add 180 to gyro reading so that the reading never jumps from 0 to 360, or vice versa

        while (Math.abs(getClippedGyroReading()) < Math.abs(threshold) && opMode.opModeIsActive()) {

            double degreesRemaining = Math.abs(threshold) - Math.abs(getClippedGyroReading()); //calculate remaining degrees

            power = (degreesRemaining / startSlowingAt); //power is a scale of remaining distance, at startSlowingAt degrees remaining we start slowing down

            if (power > 1.0) power = 1.0; //too high a power calculated

            if (power < minPower) power = minPower; //too low a power calculated

            wheels.drive(thresholdIsGreaterThanZero ? power : holdingPower, thresholdIsGreaterThanZero ? holdingPower : power);

            n.syso("Position: " + getClippedGyroReading() + "\nTarget: " + threshold + "\nPower: " + power, "Autonomous Gyro Turn");

            n.waitCycle();
        }
        wheels.stop();

    }

    private int getClippedGyroReading() {  //add 180 to gyro reading so that the reading never jumps from 0 to 360, or vice versa
        return gyro.getHeading() + 180;
    }


    private void forceHeadingReset() { //wait until the gyro heading is 0

        gyro.resetZAxisIntegrator(); //reset heading

        int count = 0;
        while (gyro.getHeading() != 0 && opMode.opModeIsActive()) {
            gyro.resetZAxisIntegrator();
            count++;
            n.syso("Waited " + count + " cycles to reset z axis integrator", "TURN DATA");
            n.waitCycle();
        }

    }

    private double getGyroReadingsAverage() { //average the past readingsNum readings of the gyro, used to see if the robot is moving too slowly
        int avg = 0;

        for (Integer reading : gyroReadings) avg += reading;

        return avg / readingsNum;
    }

    private void strongRight() {
        wheels.drive(0.3 * gyroConstant, -0.3 * gyroConstant);
    }

    private void slightRight() {
        wheels.drive(0.4 * gyroConstant, 0.0 * gyroConstant);
    }

    private void strongLeft() {
        wheels.drive(-0.15 * gyroConstant, 0.6 * gyroConstant);
    }

    private void slightLeft() {
        wheels.drive(0.0 * gyroConstant, 0.5 * gyroConstant);
    }

    private void strongForward() {
        wheels.drive(0.2 * gyroConstant, 0.2 * gyroConstant);
    }

    private void weakForward() {
        wheels.drive(0.15 * gyroConstant, 0.15 * gyroConstant);
    }
    
    private void strongReverse() {
        wheels.drive(-0.15 * gyroConstant, -0.15 * gyroConstant);
    }

    private enum Stage {
        SENSOR_CONTACT_AND_DEBRIS_SWEEP, PRIMARY_SENSOR_CONTACT, SECONDARY_SENSOR_CONTACT, WAITING_FOR_PRIMARY_TO_SURPASS_SECONDARY, DUAL_SENSOR_CONTACT
    }
}
