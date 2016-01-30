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


    //methods only needed by htechnic

//    public void readMode(DcMotorController controller) {
//        controller.setMotorControllerDeviceMode(DcMotorController.DeviceMode.READ_ONLY);
//        int i = 1;
//
//        while (controller.getMotorControllerDeviceMode() != DcMotorController.DeviceMode.READ_ONLY) {
//            syso("Waited " + i + " cycles for mode switch", "Switching to Write Mode");
//            i++;
//            waitCycle();
//        }
//    }
//
//    public void writeMode(DcMotorController controller) {
//        controller.setMotorControllerDeviceMode(DcMotorController.DeviceMode.WRITE_ONLY);
//
//        int i = 1;
//        while (controller.getMotorControllerDeviceMode() != DcMotorController.DeviceMode.WRITE_ONLY) {
//
//      syso("Waited " + i + " cycles for controller mode switch", "Switching to Write Mode");
//            i++;
//            waitCycle();
//        }
//    }
    public void waitCycle() {
        try {
            l.waitOneFullHardwareCycle();
            sleep(20);
        } catch (InterruptedException e) {
            syso("Unable to wait one hardware cycle", "Error");
        }
    }

    public void setMotorMode(DcMotor motor, DcMotorController.RunMode runMode) {
        motor.setMode(runMode);
        int i = 1;
        while (motor.getMode() != runMode) {
            motor.setMode(runMode);
            syso("Waited " + i + " cycles for motor mode switch", "Switching to Write Mode");
            i++;
            waitCycle();
        }

    }

}