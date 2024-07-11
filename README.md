# Folder/File descriptions
* BluetoothTurretControl is the Android code
* BtTurret is the Arduino code
* ServoMotorCalibration is the Arduino Servo Motor calibration code
* Bluetooth Turret Control.apk is the file to download and install the Android app to your phone. Use https://www.androidauthority.com/how-to-install-apks-31494/ for reference to install apps from apk files

# Description
This updates the IR Turret Hackpack box by Mark Rober (https://www.crunchlabs.com/products/ir-turret) to a bluetooth controlled turret for a more reliable connection. It uses an Android phone running an app that connects to a bluetooth module added to control the turret. The Android app is simple and should easily be able to be ported to iOS.

<img width="300" alt="Wiring Table" src="https://github.com/ahabdallah27/BtTurret/assets/37941857/10260ce5-9a0c-49a7-b25d-ea057f9ebbee"> <img width="300" alt="Wiring Table" src="https://github.com/ahabdallah27/BtTurret/assets/37941857/d49f3c10-fa2f-41fc-b408-6fbca5a9d228">

https://github.com/ahabdallah27/BtTurret/assets/37941857/4109c6d9-5079-45fb-b58e-64f49e58501b

https://github.com/ahabdallah27/BtTurret/assets/37941857/de377eff-3b05-455b-b56f-fe549a9f80e5

# Required Materials/Sensors
* 1 MG90S 360 degree servo motor (metal) to replace the plastic yaw servo motor
* 1 HC-06 (or HC-05) Bluetooth module
* 1 PCA9685 PWM/Servo Driver with servo arm and screw
* 9V Battery
* 9V Battery box/clip
* Wire
* 2 kilo-ohm and 1 kilo-ohm Resistor (or any combination where the one resistor has twice the resistance of the other)
* Breadboard

# Required Tools
* Screwdriver (the one included in the IR Turret should work)
* Electrical Tape

# Assembly Instructions
1. Remove the original IR sensor as that is no longer needed.
2. Remove the original yaw motor and servo arm and replace it with the MG90S servo motor and arm. Unfortunately, the motor might not have places to fasten the motor to the base. If that is the case with you, only attach the shaft to the servo arm and screw that came with the motor.
3. Add the HC-06 Bluetooth Module, PCA9685 PWM/Servo Driver, and move the Arduino Nano from the original breadboard to the new breadboard. Should look something like what’s shown in the picture (ignoring the wires, that is shown in the next step).
4. Attach the 9V battery to the 9V battery clip/box.
5. Make the connections using wire based on the table.
6. Fasten the breadboard to the base of the Turret using tape. Alternatively, you can remove the original breadboard and adhere the new breadboard to the original location of the removed breadboard by removing the sticky from the bottom of the new breadboard.
7. Fasten the 9V battery to a convenient location. You will want to remove one of the clips from 9V battery when the turret is not in use to conserve battery.
8. Connect Android to the Bluetooth module (password is likely 0000 or 1234).
9. Open the Bluetooth Module’s connection screen (Open the bluetooth settings and then long press on Bluetooth Module) and find the “Device’s Bluetooth Address” at the bottom of the screen.
10. Update MODULE_MAC line of code to reflect your “Device’s Bluetooth Address”.
11. Install the Android apk to your Android phone and allow bluetooth permissions.
12. Install the new turret code to the Arduino Nano.
13. Power the Arduino Nano (connection to the power bank) and Motor Driver (connection to the 9V battery).
14. Open the Android app and hit “Connect Bluetooth”.
15. The Bluetooth module should show up in a toast message on the bottom of the app if it successfully connected. Once, it’s connected, move the sliders on top to update the speeds of the motors and tap/hold the buttons to actuate the turret.

<img width="604" alt="Wiring Table" src="https://github.com/ahabdallah27/BtTurret/assets/37941857/9c9fd804-a491-49bb-a6ff-8250022dee42">


# Additional Notes
* Some 9V Battery boxes have switches which may be more convenient than removing one of the clips from the 9V battery.
* The max/min of the speed sliders can be adjusted in “activity_main.xml” based on your motors. You can calibrate them to figure out your max/min using “ServoMotorCalibration.ino”. Modify the “servonum”, "SERVO_MIN", and "SERVO_MAX" variables with the Serial Monitor open to see what pwm is currently being actuated to estimate the max/min while watching the motor react. If the motor does not change speed, then it is saturated, if it does not move then that is considered the deadband. Some values are written in the “ServoMotorCalibration.ino” file as reference. The default values may be sufficient.
