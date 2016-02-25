package com.qualcomm.ftcrobotcontroller.autonomous;

import com.qualcomm.ftcrobotcontroller.systems.ButtonPusher;
import com.qualcomm.ftcrobotcontroller.systems.ClimberDepositor;
import com.qualcomm.ftcrobotcontroller.systems.MyDirection;
import com.qualcomm.ftcrobotcontroller.systems.Necessities;
import com.qualcomm.ftcrobotcontroller.systems.Wheels;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
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
//    private ButtonPusher pusher;
    private MyDirection color;
    private ClimberDepositor depositor;
    private GyroSensor gyro;
    private Stage stage = Stage.SENSOR_CONTACT_AND_DEBRIS_SWEEP;

    private String data = "";
    
    private int whiteSignalThreshold = 12; //this signal must be read by color sensors for them to be considered as on the white line
    
    private double distanceThreshold = 0.13; //this signal must be read by the ODS for it to be considered as touching the beacon

    private double backupDistance = 0.07; //the ODS reading differential the robot backs up by to ensure that the climbers are in the bin

    private ArrayList<Integer> gyroReadings = new ArrayList<Integer>(); //gyro readings

    private int readingsNum = 100; //number of readings the gyro takes and are averaged

    private double gyroConstantMax = 1.4; //the max the coefficient can be

    private double gyroConstant = 1.0; //coefficient of driving

    private int gyroFreezeThreshold = 95; //the gyro reading needed to signify that the robot isn't moving

    public AutonomousImplementation(Necessities n, Wheels wheels, OpticalDistanceSensor ods, ColorSensor leftBottom, ColorSensor rightBottom, ClimberDepositor depositor, GyroSensor gyro, Servo zipLineServo, MyDirection color, LinearOpMode opMode) {
        this.n = n;
        this.ods = ods;
//        this.pusher = pusher;
        this.wheels = wheels;
        this.color = color;
        this.gyro = gyro;
        this.depositor = depositor;
        this.leftBottom = leftBottom;
        this.rightBottom = rightBottom;
        this.opMode = opMode;

        zipLineServo.setPosition(0.7); //move the zip line hitter inside the robot
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

        driveBackOds(backupDistance); //back up to ensure the climbers are in the bin and not stuck on the edge

        n.syso("Drove Back", "DATA");

        if (color == MyDirection.RED) driveByTime(650, MyDirection.LEFT); //compensate for the climber depositor arm being on the right side of the robot

        n.sleep(1500); //wait for the robot to stabilize

        depositor.drop(); //drop the climbers

        n.syso("Climbers Dropped", "DATA");

    }

    private void processSensorContactAndDebrisSweep(int left, int right) {
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

    private void processPrimarySensorContact(int primary) {

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

    private void processSecondSensorContact(int primary, int secondary) {
        
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

    private void processWaitingForPrimaryToSurpassSecondary(int primary, int secondary) {
        
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

    private void processDualSensorContact(int left, int right) {
        
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
    
    private void driveBackOds(double odsReadingDifferential) { //backs up the robot until the ods reading drops by the inputted value

        double initialReading = getAverageOdsReading(5); //initial reading of ODS over 5 cycles

        long startTime = System.currentTimeMillis();

        while (initialReading - ods.getLightDetected() < odsReadingDifferential &&  opMode.opModeIsActive()) { //drive back until the reading drops by the odsReadingDifferential
            if (System.currentTimeMillis() - startTime > 2500) break; //don't get stuck in this loop for more than 3 seconds
            strongReverse();
            checkGyro();
            n.waitCycle();
        }
        
        wheels.stop();
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

    private void checkGyro() { //if the robot isn't moving (its stuck on debris), up gyroConstant in order to drive with more power

        gyroReadings.add(Math.abs(gyro.rawX()) + Math.abs(gyro.rawY()));

        if (gyroReadings.size() == readingsNum) { //we have our 400 readings

            gyroReadings.remove((int) 0); //remove the first reading of the 100

            gyroConstant = getGyroReadingsAverage() < gyroFreezeThreshold ? (gyroConstant + 0.05) : (gyroConstant - 0.05); //up the gyro constant if its moving too slowly, lower it if its moving

            if (gyroConstant < 1.0) gyroConstant = 1.0; //clip the gyro constant so ot doesn't go too slow

            if (gyroConstant > gyroConstantMax) gyroConstant = gyroConstantMax; //clip the gyro constant so we don't set the speed too high

        }
    }

    private double getGyroReadingsAverage() { //average the past readingsNum readings of the gyro, to see if the robot is moving too slowly
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
