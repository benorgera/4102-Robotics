
package com.qualcomm.ftcrobotcontroller.Autonomous;

import android.graphics.Color;

import com.qualcomm.ftcrobotcontroller.systems.ButtonPusher;
import com.qualcomm.ftcrobotcontroller.systems.ClimberDepositor;
import com.qualcomm.ftcrobotcontroller.systems.MyDirection;
import com.qualcomm.ftcrobotcontroller.systems.Necessities;
import com.qualcomm.ftcrobotcontroller.systems.Wheels;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;


/**
 * Created by benorgera on 11/13/15.
 */
public class AutonomousRED extends LinearOpMode {

    private ButtonPusher pusher;
    ClimberDepositor depositor;
    private Wheels wheels;

    private MyDirection color = MyDirection.RED;

    private Necessities n;


    public void initializeAutonomous() throws InterruptedException {

        n = new Necessities(this.telemetry, this);

        hardwareMap.logDevices();

        n.syso("Initializing Autonomous Began", "Initializing:");

        pusher = new ButtonPusher(hardwareMap.servo.get("BP"), hardwareMap.colorSensor.get("Left Color Sensor"), hardwareMap.colorSensor.get("Right Color Sensor"), hardwareMap.led.get("Left LED"), hardwareMap.led.get("Right LED"), n);

        depositor = new ClimberDepositor(hardwareMap.servo.get("PL Arm"), hardwareMap.servo.get("PL Drop"), n);

        wheels = new Wheels(hardwareMap.dcMotor.get("Left"), hardwareMap.dcMotor.get("Right"), n);

        n.syso("Initializing Autonomous " +
                "Completed", "Initializing:");
    }


    @Override
    public void runOpMode() throws InterruptedException {

        initializeAutonomous();

        waitForStart();

        waitOneFullHardwareCycle();

        AutonomousImplementation implementation = new AutonomousImplementation(n,
                wheels,
                hardwareMap.opticalDistanceSensor.get("ODS"),
                hardwareMap.colorSensor.get("Front Left CS"),
                hardwareMap.colorSensor.get("Front Right CS"),
                pusher,
                depositor,
                color);
        implementation.run();

    }
}
