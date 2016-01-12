package com.qualcomm.ftcrobotcontroller.Autonomous;

import com.qualcomm.ftcrobotcontroller.systems.Necessities;
import com.qualcomm.ftcrobotcontroller.systems.Wheels;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;


/**
 * Created by benorgera on 11/13/15.
 */
public class AutonomousStraight extends LinearOpMode {

    private Wheels wheels;

    private Necessities n;


    public void initializeAutonomous() throws InterruptedException {

        n = new Necessities(this.telemetry, this);

        hardwareMap.logDevices();

        n.syso("Initializing Autonomous Began", "Initializing:");

        wheels = new Wheels(hardwareMap.dcMotor.get("Left"), hardwareMap.dcMotor.get("Right"), n);

        n.syso("Initializing Autonomous " +
                "Completed", "Initializing:");
    }


    public void run() {


        n.syso("Straight Drive 1 Beginning", "Autonomous"); //#2 might be the winner, and it co
        wheels.driveStraight(10000, 1);
        n.syso("Straight Drive 1 Done", "Autonomous");




    }


    @Override
    public void runOpMode() throws InterruptedException {

        initializeAutonomous();

        waitForStart();

        waitOneFullHardwareCycle();

        run();

    }

    private void waitALittle() {
        for (int i = 0; i < 20; i ++) {
            n.waitCycle();
            wheels.stop(); //never should be driving while waiting
        }
    }
}