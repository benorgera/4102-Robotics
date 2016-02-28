package com.qualcomm.ftcrobotcontroller.chsopmodes;

import com.qualcomm.ftcrobotcontroller.systems.AllClearSignHitters;
import com.qualcomm.ftcrobotcontroller.systems.Arm;
import com.qualcomm.ftcrobotcontroller.systems.ButtonPusher;
import com.qualcomm.ftcrobotcontroller.systems.ClimberDepositor;
import com.qualcomm.ftcrobotcontroller.systems.Intake;
import com.qualcomm.ftcrobotcontroller.systems.MyDirection;
import com.qualcomm.ftcrobotcontroller.systems.Necessities;
import com.qualcomm.ftcrobotcontroller.systems.Wheels;
import com.qualcomm.ftcrobotcontroller.systems.ZipLineHitters;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import android.content.Context;
import android.os.Vibrator;

import java.net.ConnectException;

/**
 * Created by benorgera on 11/1/15.
 */

public class OpMode4102 extends LinearOpMode {

    //toggle button controls

    boolean lastSwingState = false;
    boolean lastDropState = false;
    boolean lastZipState = false;
    boolean lastZip2State = false;
    boolean lastLatchState = false;


    //robot systems

    boolean isEndGame = false; //if true, print to phone

    Arm arm;
    ButtonPusher buttonPusher;
    ClimberDepositor climberDepositor;
    ZipLineHitters zipLineHitters;
    Intake intake;
    Wheels wheels;
    AllClearSignHitters allClearSignHitters;

    Necessities n;

    public void initialize() {

        n = new Necessities(this.telemetry, this);

        hardwareMap.logDevices();

        n.syso("Initializing 4102 Began", "Initializing:");

        wheels = new Wheels(hardwareMap.dcMotor.get("Left"), hardwareMap.dcMotor.get("Right"), n);

        arm = new Arm(hardwareMap.servo.get("Latch"), hardwareMap.servo.get("ArmHolder"), hardwareMap.dcMotor.get("Arm"));

        allClearSignHitters = new AllClearSignHitters(hardwareMap.dcMotor.get("AllClear"), n);

        climberDepositor = new ClimberDepositor(hardwareMap.servo.get("PL Arm"), hardwareMap.servo.get("PL Drop"));

        zipLineHitters = new ZipLineHitters(hardwareMap.servo.get("Zip"), hardwareMap.servo.get("Zip2"), n);

        n.syso("Initializing 4102 Completed", "Initializing:");

    }

    public void run() throws InterruptedException {

        long startTime = System.currentTimeMillis();

        while (opModeIsActive()) {


            wheels.processMainstream(gamepad1.left_stick_x, gamepad1.left_stick_y * -1);


            boolean temp = gamepad1.a; //use temp variable just in case button state changes mid-operation

            if (temp && !lastSwingState) climberDepositor.swing(); //toggle

            lastSwingState = temp;


            temp = gamepad1.b;

            if (temp && !lastDropState) climberDepositor.drop(); //toggle

            lastDropState = temp;

            if (gamepad1.dpad_down) wheels.toggleBackward(true);

            if (gamepad1.dpad_up) wheels.toggleBackward(false);

            if (gamepad2.a) {
                arm.spool(MyDirection.UP);
            } else if (gamepad2.b) {
                arm.spool(MyDirection.DOWN);
            } else {
                arm.stop();
            }


            //all clear sign hitters

            if (gamepad2.y) {

                allClearSignHitters.swing();

            } else if (gamepad2.dpad_up) {

                allClearSignHitters.raise();

            } else if (gamepad2.dpad_down) {

                allClearSignHitters.lower();

            } else {

                allClearSignHitters.neutralize();

            }

            temp = gamepad2.x;

            if (temp && !lastLatchState) arm.latch(); //toggle

            lastLatchState = temp;


            temp = gamepad1.left_bumper;

            if (temp && !lastZipState) zipLineHitters.toggleLeft(); //toggle

            lastZipState = temp;


            temp = gamepad1.right_bumper;

            if (temp && !lastZip2State) zipLineHitters.toggleRight(); //toggle

            lastZip2State = temp;

            printTime(startTime);
        }

    }

    private void printTime(long startTime) {
        int deltaSeconds = 120 - (int) (System.currentTimeMillis() - startTime) / 1000;

        String seconds = "" + ((int) deltaSeconds % 60);

        String minutes = "" + ((int) deltaSeconds / 60);

        String timeString = minutes + ":" + (seconds.length() == 1 ? "0" + seconds : seconds);

        if (timeString.equals("0:30")) isEndGame = true;



        n.syso(timeString, "TIME");

        if (isEndGame) n.syso("END GAME", "HEY ANSHUL!");
    }

    @Override
    public void runOpMode() throws InterruptedException {
        initialize();

        waitOneFullHardwareCycle();

        waitForStart();

        run();

    }

}