package com.qualcomm.ftcrobotcontroller.chsopmodes;

import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

public class ODS_TEST extends OpMode {

    OpticalDistanceSensor opticalDistanceSensor;

    @Override
    public void init() {
        opticalDistanceSensor = hardwareMap.opticalDistanceSensor.get("ODS");
    }

    @Override
    public void loop() {

        telemetry.addData("Value", opticalDistanceSensor.getLightDetected());

    }
}