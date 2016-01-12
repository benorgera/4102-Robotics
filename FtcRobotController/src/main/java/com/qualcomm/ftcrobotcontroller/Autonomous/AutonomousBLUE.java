package com.qualcomm.ftcrobotcontroller.Autonomous;


import com.qualcomm.ftcrobotcontroller.systems.ButtonPusher;
import com.qualcomm.ftcrobotcontroller.systems.ClimberDepositor;
import com.qualcomm.ftcrobotcontroller.systems.MyDirection;
import com.qualcomm.ftcrobotcontroller.systems.Necessities;
import com.qualcomm.ftcrobotcontroller.systems.Wheels;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.GyroSensor;


/**
 * Created by benorgera on 11/13/15.
 */
public class AutonomousBLUE extends LinearOpMode {

    private ButtonPusher pusher;
    private ClimberDepositor depositor;
    private Wheels wheels;

    private MyDirection color = MyDirection.BLUE;

    private GyroSensor gyro;

    private Necessities n;


    public void initializeAutonomous() throws InterruptedException {

        n = new Necessities(this.telemetry, this);

        hardwareMap.logDevices();

        n.syso("Initializing Autonomous Began", "Initializing:");

        gyro = hardwareMap.gyroSensor.get("gyro");

        pusher = new ButtonPusher(hardwareMap.servo.get("BP"), hardwareMap.colorSensor.get("Left Color Sensor"), hardwareMap.colorSensor.get("Right Color Sensor"), hardwareMap.led.get("Left LED"), hardwareMap.led.get("Right LED"), n);
        depositor = new ClimberDepositor(hardwareMap.servo.get("PL Arm"), hardwareMap.servo.get("PL Drop"), n);

        wheels = new Wheels(hardwareMap.dcMotor.get("Left"), hardwareMap.dcMotor.get("Right"), n);

        gyro.calibrate();

        n.syso("Initializing Autonomous " +
                "Completed", "Initializing:");
    }


    @Override
    public void runOpMode() throws InterruptedException {

        initializeAutonomous();

        waitForStart();

        waitOneFullHardwareCycle();

        AutonomousImplementation implementation = new AutonomousImplementation(n, gyro, wheels, pusher, depositor, color);
        implementation.run();

    }

}