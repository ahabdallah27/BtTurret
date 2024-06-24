/*

 ************************************************************************************
 * MIT License
 *
 * Copyright (c) 2024 Ali Abdallah

 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 ************************************************************************************
 */


//@ freq 50
//Servo0 250-300 is deadband. 100-400 is good range int vals[6] = {0, 125, 175, 0, 350, 400}; 1015 makes 6 full rotations, 15ms delay is ok for 1 rotation (it is more, but can't get it slower no matter how low I go on the delay)
//Servo1 150-400. This is all angle based. You request the angle and it goes there. int vals[6] = {250, 125, 175, 250, 350, 400};
//Servo2 100-450 250-325ish deadband int vals[6] = {0, 125, 175, 0, 375, 425};

//@ freq 70
//Servo0 175 - 375, 425 - 625
//Servo1 225 - 390 - 550
//Servo2 175 - 375, 425 - 625
#include <Wire.h>
#include <Adafruit_PWMServoDriver.h>

// called this way, it uses the default address 0x40
Adafruit_PWMServoDriver pwm = Adafruit_PWMServoDriver();
// you can also call it with a different address you want
//Adafruit_PWMServoDriver pwm = Adafruit_PWMServoDriver(0x41);
// you can also call it with a different address and I2C interface
//Adafruit_PWMServoDriver pwm = Adafruit_PWMServoDriver(0x40, Wire);

// Depending on your servo make, the pulse width min and max may vary, you 
// want these to be as small/large as possible without hitting the hard stop
// for max range. You'll have to tweak them as necessary to match the servos you
// have!
#define SERVO_MIN  100  // Update this for min pulse length (0 min)
#define SERVO_MAX  700 // Update this for max pulse length (4096 max)
#define STEP_SIZE 25   // Update this to step through SERVO_MIN and SERVO_MAX to determine differences in steps
#define SERVO_FREQ 70  // Update this for a stronger motor (larger values). 

// our servo # counter
uint8_t servonum = 2;
uint16_t pulselen = 0;
int vals[1] = {175};
int iter;

void setup() {
  Serial.begin(9600);
  Serial.println("8 channel Servo test!");

  pwm.begin();
  pwm.setOscillatorFrequency(27000000);
  pwm.setPWMFreq(SERVO_FREQ);  

  delay(10);
}


void loop() {
  Serial.print("Servo number: ");
  Serial.println(servonum);
  for (uint16_t pulselen = SERVO_MIN; pulselen <= SERVO_MAX; pulselen=pulselen+STEP_SIZE) {
    pwm.setPWM(servonum, 0, pulselen);
    Serial.println(pulselen);
    delay(1000);
  }
  Serial.println("Change Direction");
  delay(500);
  for (uint16_t pulselen = SERVO_MAX; pulselen >= SERVO_MIN; pulselen=pulselen-STEP_SIZE) {
    pwm.setPWM(servonum, 0, pulselen);
    Serial.println(pulselen);
    delay(1000);
  }

}
