package com.qualcomm.ftcrobotcontroller.Autonomous;

import android.graphics.Color;
import com.qualcomm.ftcrobotcontroller.systems.ButtonPusher;
import com.qualcomm.ftcrobotcontroller.systems.ClimberDepositor;
import com.qualcomm.ftcrobotcontroller.systems.MyDirection;
import com.qualcomm.ftcrobotcontroller.systems.Necessities;
import com.qualcomm.ftcrobotcontroller.systems.Wheels;
import com.qualcomm.robotcore.hardware.GyroSensor;

/**
 * Created by benorgera on 12/22/15.
 */

public class AutonomousImplementation {

    private GyroSensor gyro;
    private Necessities n;
    private Wheels wheels;
    private ButtonPusher pusher;
    private MyDirection color;
    private ClimberDepositor depositor;

    public AutonomousImplementation(Necessities n, GyroSensor gyro, Wheels wheels, ButtonPusher pusher, ClimberDepositor depositor, MyDirection color) {
        this.gyro = gyro;
        this.n = n;
        this.pusher = pusher;
        this.wheels = wheels;
        this.color = color;
        this.depositor = depositor;
    }

    public void run() {

        while (gyro.isCalibrating()) n.syso("Gyro Still Calibrating", "Autonomous");

        double skew = calculateSkew();

        n.syso(Double.toString(skew), "SKEW");

        n.syso("Straight Drive 1 Beginning", "Autonomous");
        wheels.driveStraight(1200, 1);
        n.syso("Straight Drive 1 Done", "Autonomous");


        waitALittle();

        n.syso("Turn 1 Beginning", "Autonomous");
        wheels.turn(45 * (color == MyDirection.BLUE ? 1 : -1), skew, gyro);
        n.syso("Turn 1 Done", "Autonomous");

        waitALittle();

        n.syso("Drive Straight 2 Beginning", "Autonomous");
        wheels.driveStraight(6330, 1);
        n.syso("Drive Straight 2 Finished", "Autonomous");

        waitALittle();

        n.syso("Turn 2 Beginning", "Autonomous");
        wheels.turn(45 * (color == MyDirection.BLUE ? 1 : -1), skew, gyro);
        n.syso("Turn 2 Finished", "Autonomous");

        waitALittle();

        n.syso("Drive Straight 3 Beginning", "Autonomous");
        wheels.driveStraight(509, 1);
        n.syso("Drive Straight 3 Finished", "Autonomous");

        waitALittle();

        depositor.swing();

        for (int i = 0; i < 5; i ++) waitALittle();

        depositor.drop();

        for (int i = 0; i < 3; i ++) waitALittle();

        depositor.swing();

        n.syso("Button Push Beginning", "Autonomous");
        n.syso("Button Push " + (pusher.senseAndPush(color) ? "Was Successful" : "Failed"), "Autonomous");

        waitALittle();

    }

    private void waitALittle() {
        for (int i = 0; i < 13; i ++) {
            n.waitCycle();
            wheels.stop(); //never should be driving while waiting
        }
    }

    private double calculateSkew() {
        int checks = 5;

        double voltage = 0;

        for (int i = 0; i < checks; i ++) { //get avg voltage
            voltage += n.getVoltage();
            n.sleep(100);
        }

        voltage = voltage/ checks;

        return (voltage - 11.5) * 5;

    }
}
