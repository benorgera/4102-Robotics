/* Copyright (c) 2014 Qualcomm Technologies Inc

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Qualcomm Technologies Inc nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

package com.qualcomm.ftcrobotcontroller.opmodes;



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
import com.qualcomm.robotcore.hardware.HardwareMap;

public class OpMode4102 extends LinearOpMode {


    //all toggle button controls below
    int toggleThreashold = 30;

    int toggleCount = 0;
    int lastSwing = 0;
    int lastDrop = 0;
    int lastZip = 0;
    int lastZip2 = 0;
    int lastLatch = 0;
    int lastHammerR = 0;
    int lastHammerL = 0;

    private Arm arm;
    private ButtonPusher buttonPusher;
    private ClimberDepositor climberDepositor;
    private ZipLineHitters zipLineHitters;
    private Intake intake;
    private Wheels wheels;
    private AllClearSignHitters allClearSignHitters;

    private drivingMode mode = OpMode4102.drivingMode.NoAngle;

    private final double maxDcMotorPower = 1.0;
    private final double minDcMotorPower = 0.0;

    private Necessities n;

    public void initialize() {

        n = new Necessities(this.telemetry, this);

        hardwareMap.logDevices();

        n.syso("Initializing 4102 Began", "Initializing:");

        wheels = new Wheels(hardwareMap.dcMotor.get("Left"), hardwareMap.dcMotor.get("Right"), n);

        arm = new Arm(hardwareMap.servo.get("Latch"), hardwareMap.dcMotor.get("Arm"), n);

        allClearSignHitters = new AllClearSignHitters(hardwareMap.servo.get("AllClearL"), hardwareMap.servo.get("AllClearR"), n);

        climberDepositor = new ClimberDepositor(hardwareMap.servo.get("PL Arm"), hardwareMap.servo.get("PL Drop"), n);

        zipLineHitters = new ZipLineHitters(hardwareMap.servo.get("Zip"), hardwareMap.servo.get("Zip2"), n);

        n.syso("Initializing 4102 Completed", "Initializing:");

    }

    public void run() throws InterruptedException {

        while (opModeIsActive()) {

            toggleCount++;

            wheels.processMainstream(gamepad1.left_stick_x, gamepad1.left_stick_y * -1);

            if (gamepad1.a && (toggleCount - lastSwing >= toggleThreashold)) { //toggle
                climberDepositor.swing();
                lastSwing = toggleCount;

            }

            if (gamepad1.b && (toggleCount - lastDrop >= toggleThreashold)) { //toggle
                climberDepositor.drop();
                lastDrop = toggleCount;

            }

            if (gamepad1.dpad_down) wheels.toggleBackward(true);

            if (gamepad1.dpad_up) wheels.toggleBackward(false);

            if (gamepad2.a) {
                arm.spool(MyDirection.UP);
            } else if (gamepad2.b) {
                arm.spool(MyDirection.DOWN);
            } else {
                arm.stop();
            }




            if (gamepad2.x && (toggleCount - lastLatch >= toggleThreashold)) { //toggle
                arm.latch();
                lastLatch = toggleCount;
            }

            if (gamepad2.left_bumper && (toggleCount - lastZip >= toggleThreashold)) { //toggle
                zipLineHitters.toggleLeft();
                lastZip = toggleCount;
            }

            if (gamepad2.right_bumper && (toggleCount - lastZip2 >= toggleThreashold)) { //toggle
                zipLineHitters.toggleRight();
                lastZip2 = toggleCount;
            }

            if (gamepad2.dpad_up) allClearSignHitters.raise();

            if (gamepad2.dpad_down) allClearSignHitters.lower();

            if (gamepad2.dpad_left && toggleCount - lastHammerL >= toggleThreashold) {
                allClearSignHitters.toggleLeft();
                lastHammerL = toggleCount;
            }

            if (allClearSignHitters.isHammeringLeft()) allClearSignHitters.hammerLeft(toggleCount);

            if (gamepad2.dpad_right && toggleCount - lastHammerR >= toggleThreashold) {
                allClearSignHitters.toggleRight();
                lastHammerR = toggleCount;
            }

            if (allClearSignHitters.isHammeringRight()) allClearSignHitters.hammerRight(toggleCount);


            waitOneFullHardwareCycle();
        }

    }

    @Override
    public void runOpMode() throws InterruptedException {
        initialize();

        waitOneFullHardwareCycle();

        waitForStart();

        run();

    }


    public enum drivingMode {
        NoAngle, Angle
    }

}
