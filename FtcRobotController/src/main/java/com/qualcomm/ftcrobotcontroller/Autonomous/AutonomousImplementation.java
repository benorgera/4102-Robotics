package com.qualcomm.ftcrobotcontroller.Autonomous;

import com.qualcomm.ftcrobotcontroller.systems.ButtonPusher;
import com.qualcomm.ftcrobotcontroller.systems.ClimberDepositor;
import com.qualcomm.ftcrobotcontroller.systems.MyDirection;
import com.qualcomm.ftcrobotcontroller.systems.Necessities;
import com.qualcomm.ftcrobotcontroller.systems.Wheels;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;

/**
 * Created by benorgera on 12/22/15.
 */

public class AutonomousImplementation {

    private Necessities n;
    private Wheels wheels;
    private OpticalDistanceSensor ods;
    private ColorSensor leftBottom;
    private ColorSensor rightBottom;
    private ButtonPusher pusher;
    private MyDirection color;
    private ClimberDepositor depositor;
    private Stage stage = AutonomousImplementation.Stage.PRIMARY_SENSOR_CONTACT;
    
    private String data = "";
    
    private int whiteSignalThreshold = 12; //this signal must be read by color sensors for them to be considered as on the white line
    
    private double distanceThreshold = 0.2; //this signal must be read by the ODS for it to be considered as touching the beacon

    private double backupDistance = 0.5; //the ODS reading differential the robot backs up by to ensure that the climbers are in the bin

    public AutonomousImplementation(Necessities n, Wheels wheels, OpticalDistanceSensor ods, ColorSensor leftBottom, ColorSensor rightBottom, ButtonPusher pusher, ClimberDepositor depositor, MyDirection color) {
        this.n = n;
        this.ods = ods;
        this.pusher = pusher;
        this.wheels = wheels;
        this.color = color;
        this.depositor = depositor;
        this.leftBottom = leftBottom;
        this.rightBottom = rightBottom;
    }

    public void run() {

        while (ods.getLightDetected() < distanceThreshold) { //once we have reached the beacon, this condition is no longer true

            //green is used as a measure of the white line because blue and red are found on the field, whereas green is not
            int left = leftBottom.green();
            int right = rightBottom.green();

            //primary: first sensor to cross the white line
            //secondary: second sensor to cross the white line
            int primary = color == MyDirection.BLUE ? left : right;
            int secondary = color == MyDirection.BLUE ? right : left;

            switch (stage) {
                case PRIMARY_SENSOR_CONTACT:
                    processPrimarySensorContact(primary);
                    break;
                case SECONDARY_SENSOR_CONTACT:
                    processSecondSensorContact(secondary);
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

            n.waitCycle();
        }

        wheels.stop();

        n.syso("Beacon Reached", "DATA");

        depositor.swing(); //swing out the preload arm over the bin

        n.syso("Preload Arm Deployed", "DATA");
        
        driveBack(backupDistance); //back up to ensure the climbers are in the bin and not stuck on the edge

        n.syso("Drove Back", "DATA");

        n.sleep(2000); //wait for the robot to stabilize

        depositor.drop(); //drop the climbers

        n.syso("Climbers Dropped", "DATA");

    }

    private void processPrimarySensorContact(int primary) {
        
        if (primary > whiteSignalThreshold) { //primary sensor is on the white line
            wheels.stop();
            data = "Made Primary Sensor Contact";
            stage = Stage.SECONDARY_SENSOR_CONTACT;
            n.sleep(500); //wait for robot to stop
        } else { //primary sensor is not yet on the white line
            strongForward();
            data = "Waiting For Primary Sensor Contact";
        }

    }

    private void processSecondSensorContact(int secondary) {
        
        if (secondary > whiteSignalThreshold) { //secondary sensor is on the white line
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
        
        if (primary >= secondary) { //robot parallel to the line or has surpassed parallel
            wheels.stop();
            data = "Primary Surpassed Secondary";
            stage = Stage.DUAL_SENSOR_CONTACT;
            depositor.threeQuarterSwing(); //bring out the preload arm
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
    
    private void driveBack(double odsReadingDifferential) { //backs up the robot until the ods reading drops by the inputted value

        double initialReading = getAverageOdsReading(5); //initial reading of ODS over 5 cycles

        long startTime = System.currentTimeMillis();

        while ((initialReading - ods.getLightDetected()) < odsReadingDifferential) { //drive back until the reading drops by the odsReadingDifferential
            if (System.currentTimeMillis() - startTime > 2500) break; //don't get stuck in this loop for more than 3 seconds
            strongReverse();
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

    private void strongRight() {
        wheels.drive(0.3, -0.3);
    }

    private void slightRight() {
        wheels.drive(0.4, 0.0);
    }

    private void strongLeft() {
        wheels.drive(-0.3, 0.3);
    }

    private void slightLeft() {
        wheels.drive(0.0, 0.4);
    }

    private void strongForward() {
        wheels.drive(0.2, 0.2);
    }

    private void weakForward() {
        wheels.drive(0.15, 0.15);
    }
    
    private void strongReverse() {
        wheels.drive(-0.2, -0.2);
    }

    private enum Stage {
        PRIMARY_SENSOR_CONTACT, SECONDARY_SENSOR_CONTACT, WAITING_FOR_PRIMARY_TO_SURPASS_SECONDARY, DUAL_SENSOR_CONTACT
    }
}
