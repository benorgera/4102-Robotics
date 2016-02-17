package com.qualcomm.ftcrobotcontroller.systems;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.robocol.Telemetry;

/**
 * Created by benorgera on 12/8/15.
 */
public class Necessities {

    private VoltageSensor v;

    private Telemetry t;

    private LinearOpMode l;

    public Necessities(Telemetry t, LinearOpMode l) {
        this.t = t;
        this.l = l;
    }

    public void syso(String s, String title) { //prints data from components to screen
        t.addData(title, s);
    }

    public void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            syso("Sleep Interrupted", "Error");
        }
    }


    public void waitCycle() {
        try {
            l.waitOneFullHardwareCycle();
            sleep(20);
        } catch (InterruptedException e) {
            syso("Unable to wait one hardware cycle", "Error");
        }
    }


}