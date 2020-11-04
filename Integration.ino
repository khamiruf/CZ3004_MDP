#include <DualVNH5019MotorShield.h>
#include <EnableInterrupt.h>
#include <PID_v1.h>


//Hello Yao Wen, Meow//
/* ===================== Definition/Variables for Encoder ========================== */

#define LEFT_ENCODER 3 //left motor encoder A to pin 5
#define RIGHT_ENCODER 11 //right motor encoder A to pin 13

DualVNH5019MotorShield md;

double leftEncoderValue = 0;
double rightEncoderValue = 0;
double difference;                // Use to find the difference
double Setpoint, Input, Output;
int FP = 0;

/* ===================== Definition/Variables for Sensor ========================== */

#define s0 A0;  // Right Back Sensor  
#define s1 A1;  // Right Front Sensor
#define s2 A2;  // Left Long Sensor
#define s3 A3;  // Front Right Sensor
#define s4 A4   // Front Left Sensor
#define s5 A5;  // Front Center 


/* ==================================== Notes =================================== */
// 562.25 pulse = 1 revolution 
// Diameter = 6cm
// Circumference = 18.8495559215cm
// For 90 degrees turn = 492 ticks.
// Diameter = 21 cm (ROBOT)
// Circumference = 65.9734457254


/* ================================== PID ================================== */
//double Kp=1.7, Ki=0, Kd=0;
//meow
PID straightPID(&leftEncoderValue, &Output, &rightEncoderValue, 2.5, 0.5, 0.0, DIRECT);
PID singleUnitPID(&leftEncoderValue, &Output, &rightEncoderValue, 11.5, 1.0, 0.0, DIRECT);
PID leftPID(&leftEncoderValue, &Output, &rightEncoderValue, 10.0, 0.5, 0.0, DIRECT);
PID rightPID(&leftEncoderValue, &Output, &rightEncoderValue, 13.5, 0.5, 0.0, DIRECT); 
PID leftFPPID(&leftEncoderValue, &Output, &rightEncoderValue, 5.3, 0.0, 0.0, DIRECT); 
PID rightFPPID(&leftEncoderValue, &Output, &rightEncoderValue, 5.3, 0.0, 0.0, DIRECT); 
//PID(&input, &output, &setpoint, Kp, Ki, Kd, Direction)
//Parameters: Input - the variable we are trying to control
//          : Output - the variable that will be adjusted by PID
//          : Setpoint - the value we want the input to maintain
//          : Direction - either DIRECT or REVERSE

/* ============================ Encoder Counter ============================ */
  void leftEncoderInc(void){
    leftEncoderValue++;
  }
  void rightEncoderInc(void){
    rightEncoderValue++;
  }
  void leftEncoderRes(void){
    leftEncoderValue = 0;
  }
  void rightEncoderRes(void){
    rightEncoderValue = 0;
  }
  double rpm_to_speed_1(double RPM){
  if (RPM>0)
    return 2.8356*RPM + 19.531; 
  else if (RPM == 0)
    return 0;
  else
    return -2.91*(-1)*RPM - 16.165; 
  }

/* ============================ Distance to Ticks ============================= */
double distToTicks(double dist){
  double circumference = 18.85;
  double pulse = 522.25;  // Original = 562.25
  double oneCM = circumference / pulse;
  double ticks = dist / oneCM;
  return ticks;
}

double distToTicks_FP(double dist){
  double circumference = 18.85;
  double pulse = 542.25; // OLD: 562.25 NEW SPEED 
  double oneCM = circumference / pulse;
  double ticks = dist / oneCM;
  return ticks;
}

/* ====================== Rotation Ticks Left ============================= */
double rotationTicksLeft(double angle){
  double degree = 90;
  double ticks =439;//446
  double perDegree = ticks / 90; //492
  return angle*perDegree;
}

double rotationFPTicksLeft(double angle){
  double degree = 90;
  double ticks =440;//445
  double perDegree = ticks / 90; //492
  return angle*perDegree;
}
/* ====================== Rotation Ticks Right ============================= */
double rotationTicksRight(double angle){
  double degree = 90;
  double ticks = 437; 
  double perDegree = ticks / 90; 
  return angle*perDegree;
}

double rotationFPTicksRight(double angle){
  double degree = 90;
  double ticks = 437; //440
  double perDegree = ticks / 90; 
  return angle*perDegree;
}
/* ============================ Rotation  =================================== */
void turnLeft(double ticks){
  leftEncoderRes();
  rightEncoderRes();
  while((leftEncoderValue < ticks) || (rightEncoderValue < ticks)){
     md.setSpeeds(-(250),-(250+Output));
     leftPID.Compute();
  }
  md.setBrakes(400,400);
  delay(100);
  rightEncoderRes();
  leftEncoderRes();
}

void turnRight(double ticks){
  leftEncoderRes();
  rightEncoderRes();
  while((leftEncoderValue < ticks) || (rightEncoderValue < ticks)){
     md.setSpeeds((250),(250+Output));
     rightPID.Compute();
  }
  md.setBrakes(400,400);
  delay(100);
  rightEncoderRes();
  leftEncoderRes();
}

void turnFPLeft(double ticks){
  leftEncoderRes();
  rightEncoderRes();
  while((leftEncoderValue < ticks) || (rightEncoderValue < ticks)){
     md.setSpeeds(-(250),-(250+Output));
     leftFPPID.Compute();
  }
  md.setBrakes(400,400);
  delay(100);
  rightEncoderRes();
  leftEncoderRes();
}

void turnFPRight(double ticks){
  leftEncoderRes();
  rightEncoderRes();
  while((leftEncoderValue < ticks) || (rightEncoderValue < ticks)){
     md.setSpeeds((250),(250+Output));
     rightFPPID.Compute();
  }
  md.setBrakes(400,400);
  delay(100);
  rightEncoderRes();
  leftEncoderRes();
}

void turnFastLeft(double ticks){
  leftEncoderRes();
  rightEncoderRes();
  while((leftEncoderValue < ticks) || (rightEncoderValue < ticks)){
     md.setSpeeds(-(250),-(250+Output));
     straightPID.Compute();
  }
  md.setBrakes(400,400);
  rightEncoderRes();
  leftEncoderRes();
}
//me0w
void turnFastRight(double ticks){
  leftEncoderRes();
  rightEncoderRes();
  while((leftEncoderValue < ticks) || (rightEncoderValue < ticks)){
     md.setSpeeds((250),(250+Output));
     straightPID.Compute();
  }
  md.setBrakes(400,400);
  rightEncoderRes();
  leftEncoderRes();
}

/* =============================== Go Forward ============================= */
void goCustomForward(double ticks){
  rightEncoderRes();
  leftEncoderRes();
  while((leftEncoderValue < ticks) || (rightEncoderValue < ticks)){
     md.setSpeeds(-(350),350+Output); // NEW SPEED
     straightPID.Compute();
  }
  md.setBrakes(400,400);
  rightEncoderRes();
  leftEncoderRes();
  delay(100);
}

void goSingleForward(double ticks){
  rightEncoderRes();
  leftEncoderRes();
  while((leftEncoderValue < ticks) || (rightEncoderValue < ticks)){
     md.setSpeeds(-(250),250+Output); 
     singleUnitPID.Compute();
  }
  md.setBrakes(400,400);
  rightEncoderRes();
  leftEncoderRes();   
  delay(100);
}

void goFastForward(double ticks){
  rightEncoderRes();
  leftEncoderRes();
  while((leftEncoderValue < ticks) || (rightEncoderValue < ticks)){
    md.setSpeeds(-(250),250+Output);
    straightPID.Compute();
  }
  md.setBrakes(400,400);
  rightEncoderRes();
  leftEncoderRes();
}
/* =============================== Go Backwards ============================= */
void goBackwards(double ticks){
  rightEncoderRes();
  leftEncoderRes();
  while((leftEncoderValue < ticks) || (rightEncoderValue < ticks)){
     md.setSpeeds((250),-(250+Output));
     straightPID.Compute();
  }
  md.setBrakes(400,400);
  rightEncoderRes();
  leftEncoderRes();
  delay(100);
}

void goFastBackwards(double ticks){
  rightEncoderRes();
  leftEncoderRes();
  while((leftEncoderValue < ticks) || (rightEncoderValue < ticks)){
     md.setSpeeds((250),-(250+Output));
     straightPID.Compute();
  }
  md.setBrakes(400,400);
  rightEncoderRes();
  leftEncoderRes();
}


/* ================================= Setup ================================= */
void setup() {
  Serial.begin(115200);
  /* ======================== For PID ============================= */
  md.init();
  pinMode (LEFT_ENCODER, INPUT); //set digital pin 5 as input
  pinMode (RIGHT_ENCODER, INPUT); //set digital pin 13 as input
  enableInterrupt(LEFT_ENCODER, leftEncoderInc, RISING);  // Reading the Encoder
  enableInterrupt(RIGHT_ENCODER, rightEncoderInc, RISING);// Reading the Encoder
  //m3ow
  leftPID.SetOutputLimits(-50,50);
  leftPID.SetMode(AUTOMATIC); 
  rightPID.SetOutputLimits(-50,50);
  rightPID.SetMode(AUTOMATIC); 
  straightPID.SetOutputLimits(-50,50);
  straightPID.SetMode(AUTOMATIC);
  singleUnitPID.SetOutputLimits(-50,50);
  singleUnitPID.SetMode(AUTOMATIC); 
  leftFPPID.SetOutputLimits(-50,50);
  leftFPPID.SetMode(AUTOMATIC); 
  rightFPPID.SetOutputLimits(-50,50);
  rightFPPID.SetMode(AUTOMATIC); 
 /* ======================== For Sensor ============================= */
  pinMode(A0, INPUT);
  pinMode(A1, INPUT);
  pinMode(A2, INPUT);
  pinMode(A3, INPUT); 
  pinMode(A4, INPUT);
  pinMode(A5, INPUT);
}
/* =============================== Sorting =============================== */
void sort(int a[], int n) {
  for (int i = 1; i < n; i++) {
    int next = a[i];
    int j;
    for (j = i - 1; j >= 0 && a[j] > next; j--) a[j + 1] = a[j];
    a[j + 1] = next;
  }
}
/* ======================= Getting RAW Sensor Value ======================= */
int getRaw(float pin){
  int ir_val[100];
  for (int i = 0; i < 100; i++) ir_val[i] = analogRead(pin);
  sort(ir_val, 100);
  return ir_val[100/2];      // send back the median
}
/* =================== Conversion For Different Sensor =================== */
// Right Back Sensor
int convertS0(int ir_val){
  int x = ir_val;
  double p1 =   -0.005772;
  double p2 =      -1.075;
  double p3 =        3952;
  double q1 =      -19.92;
  double distanceCM = (p1*x*x + p2*x + p3) / (x + q1);
  int distance_mm = round(distanceCM*10);
  int distance_cm = distance_mm/10;
  return distance_cm; 
}

// Right Front Sensor
int convertS1(int ir_val){
int x = ir_val;
  double p1 =      -7.951;
  double p2 =        6047;
  double q1 =       21.32;
  double distanceCM = (p1*x + p2) / (x + q1);
  int distance_mm = round(distanceCM*10);
  int distance_cm = distance_mm/10;
  if(distance_cm == 29){
    distance_cm ++;
  }
  return distance_cm; 
}

// Left Long Sensor
int convertS2(int ir_val){
  int x = ir_val;
  double p1 =    -0.07017;
  double p2 =        44.9;
  double p3 =       216.4;
  double p4 =       1.572;
  double q1 =      -79.82;
  double q2 =       122.7;

  double distanceCM = (p1*pow(x,3) + p2*pow(x,2) + p3*x + p4) / (pow(x,2) + q1*x + q2);
  int distance_mm = round(distanceCM*10);
  int distance_cm = distance_mm/10;
  if(distance_cm >= 10 && distance_cm <=11){
    distance_cm --;
  }else if(distance_cm >= 16 && distance_cm <= 19){
    distance_cm ++;
  }
  return distance_cm; 
}

// Front Right Sensor
int convertS3(int ir_val){
  int x = ir_val;
  double p1 =   5.054e-05;
  double p2 =    -0.06691;
  double p3 =       24.25;
  double p4 =       2.026;
  double p5 =      0.5528;
  double q1 =      -74.42;
  double q2 =      -3.428;


  double distanceCM = (p1*pow(x,4) + p2*pow(x,3) + p3*pow(x,2) + p4*x + p5)/(pow(x,2) + q1*x + q2);
  int distance_mm = round(distanceCM*10);
  int distance_cm = distance_mm/10;

  return distance_cm;
}
// A cat was here
// Front Left Sensor
int convertS4(int ir_val){
  int x = ir_val;
  double p1 =    0.004069;
  double p2 =      -10.47;
  double p3 =        5969;
  double q1 =       14.58;
  double distanceCM =  (p1*pow(x,2) + p2*x + p3) / (x + q1);
  int distance_mm = round(distanceCM*10);
  int distance_cm = distance_mm/10;
  if(distance_cm >= 13 && distance_cm <= 17){
    distance_cm ++;
  }else if(distance_cm >= 21 && distance_cm <=26){
    distance_cm ++;
  }else if (distance_cm >= 19){
    distance_cm ++;
  }
  return distance_cm;
}

// Front Center Sensor
int convertS5(int ir_val) {
  int x = ir_val;
  double p1 =    -0.00583;
  double p2 =     -0.4929;
  double p3 =        4860;
  double q1 =      0.3691;
  double distanceCM = (p1*pow(x,2) + p2*x + p3) / (x + q1);
  int distance_mm = round(distanceCM*10);
  int distance_cm = distance_mm/10;
  if(distance_cm >= 3 && distance_cm <= 11){
    distance_cm ++;
  }
  return distance_cm;
}
/* ========================= Get Distance Request ======================== */
// Get Distance of S1
int distanceS0(){
  int ir_val = getRaw(A0);
  return convertS0(ir_val);
}
// Get Distance of S2
int distanceS1(){
  int ir_val = getRaw(A1);
  return convertS1(ir_val);
}
// Get Distance of S3
int distanceS2(){
  int ir_val = getRaw(A2);
  return convertS2(ir_val);
}
// Get Distance of S4
int distanceS3(){
  int ir_val = getRaw(A3);
  return convertS3(ir_val);
}
// Get Distance of S5
int distanceS4(){
  int ir_val = getRaw(A4);
  return convertS4(ir_val);
}
// Get Distance of S6
int distanceS5(){
  int ir_val = getRaw(A5);
  return convertS5(ir_val);
}

/* ============================= Distance to Grid ========================== */
int getGrid(int distance_cm, int sensor){
  int remainder, grid;
  grid = 4;

  if(sensor == 0){ // Right Back
    if(distance_cm <= 0){ // Blindspot
      grid = 3;
    }else if(distance_cm >= 1 && distance_cm <= 8){
      grid = 1; // One cube away
    }
    else if(distance_cm > 8 && distance_cm <= 18){
      grid = 2; // 2 Cube away
    }
    else if(distance_cm > 18)
    {
      grid = 3; // No obstacles 
    }
  }

  if(sensor == 1){ // Right Front
    if(distance_cm <= 0){ // Blindspot
      grid = 3;
    }else if(distance_cm >= 1 && distance_cm <= 8){  //CHANGES
      grid = 1; // One cube away
    }
    else if(distance_cm > 8 && distance_cm <= 18){  // og is 20
      grid = 2; // 2 Cube away
    }
    else if(distance_cm > 18)                       
    {
      grid = 3; // No obstacles 
    }
  }

  if (sensor == 2){ // Left Long
    if(distance_cm < 18){
      grid = 4; // Blindspot
    }
    else if(distance_cm >= 18&& distance_cm < 26){
      grid = 1;
    }
    else if(distance_cm >= 26 && distance_cm < 38){  
      grid = 2;
    }
    else if(distance_cm >= 40){      
      grid = 3;
    }
  }

  if(sensor == 3){ // Front Right
    if(distance_cm <= 0){ // Blindspot
      grid = 3;
    }else if(distance_cm >= 1 && distance_cm <= 8){ 
      grid = 1; // One cube away
    }
    else if(distance_cm > 8 && distance_cm <= 17){ // SPECIAL CASE OLD :20
      grid = 2; // 2 Cube away
    }
    else if(distance_cm > 17)
    {
      grid = 3; // No obstacles 
    }
  }
  
  if(sensor == 4){ // Front Left
    if(distance_cm <= 0){ // Blindspot
      grid = 3;
    }else if(distance_cm >= 1 && distance_cm <= 8){          //SPECIAL CASE   
      grid = 1; // One cube away
    }
    else if(distance_cm > 8 && distance_cm <= 17){           // OLD : 18   
      grid = 2; // 2 Cube away
    }
    else if(distance_cm > 17)                 
    {
      grid = 3; // No obstacles 
    }
  }
  
  if(sensor == 5){ // Front Center
    if(distance_cm <= 0){ // Blindspot
      grid = 3;
    }else if(distance_cm >= 1 && distance_cm <= 8){
      grid = 1; // One cube away
    }
    else if(distance_cm > 8 && distance_cm <= 17){       // OLD: 20
      grid = 2; // 2 Cube away
    }
    else if(distance_cm > 17)
    {
      grid = 3; // No obstacles 
    }
  }
  return grid;
}
/* ========================= Send Sensor in Grid ========================== */
void sendSensors(){
  int S0 = distanceS0();
  int S1 = distanceS1();
  int S2 = distanceS2();
  int S3 = distanceS3();
  int S4 = distanceS4();
  int S5 = distanceS5();
  
  int feedback_0 = getGrid(S0,0);
  int feedback_1 = getGrid(S1,1);
  int feedback_2 = getGrid(S2,2);
  int feedback_3 = getGrid(S3,3);
  int feedback_4 = getGrid(S4,4);
  int feedback_5 = getGrid(S5,5);
  
  Serial.print("@");
  Serial.print("S");
  Serial.print("|");
  Serial.print(feedback_0);  
  Serial.print(feedback_1);  
  Serial.print(feedback_2);  
  Serial.print(feedback_3);  
  Serial.print(feedback_4);
  Serial.println(feedback_5);    
}
/* ========================== Sensor For Alignment ========================= */
void rightAlign(){
  int rightFront;
  int rightBack;
  int difference;
  int count = 0;
  unsigned long starTime;
  
  while(count < 2){
    rightFront = distanceS1();
    rightBack = distanceS0();
    difference = 0;
    if(rightFront < rightBack){
      difference = rightBack - rightFront;
    }else{
      difference = rightFront - rightBack;
    }
    if(difference <= 4){
      starTime = millis();
      
      while ((rightFront != rightBack) && (millis() - starTime <= 5000)){ // Max alignment time = 3 secs
        if(rightFront > rightBack){
          // turn right
          customAlignRight();
          rightFront = distanceS1();
          rightBack = distanceS0();
        }
        if(rightBack > rightFront){
          // turn left
          customAlignLeft();
          rightFront = distanceS1();
          rightBack = distanceS0();
        }   
      }
    }
    count++;  
  }
}

void frontAlign(){
  int count = 0;
  int frontLeft;
  int frontRight;
  int frontCenter;
  int difference;
  unsigned long startingTime;
  
  while(count < 2){
    frontLeft = distanceS4();
    frontRight = distanceS3();
    frontCenter = distanceS5();
    difference = 0;
    if(frontLeft < frontRight){
      difference = frontRight - frontLeft;
    }else{
      difference = frontLeft - frontRight;
    }
    if(difference <= 4){
      startingTime = millis();
      while ((frontLeft != frontRight) && (millis() - startingTime <= 5000)){ // Max alignment time = 3 secs
        if(frontLeft > frontRight){
          customAlignRight();
          frontLeft = distanceS4();
          frontRight = distanceS3();
        }
        if(frontRight > frontLeft){
          customAlignLeft();
          frontLeft = distanceS4();
          frontRight = distanceS3();
        }
      }
    }
    count++;
  }
  
}

void wallAvoidance(){
  int frontLeft = distanceS4();
  int frontRight = distanceS3();
  int frontCenter = distanceS5();
  frontAlign();
  while((frontLeft < 5) || (frontRight < 5)){ //changed from 6
      customFastBackward(0.01);
      frontLeft = distanceS4();
      frontRight = distanceS3();
      frontCenter = distanceS5();
    }
    while((frontLeft > 5 && frontLeft < 10) || (frontRight > 5  && frontRight < 10)){ //CHANGES GOOD //the bigger number change from 10.
      customFastForward(0.01);
      frontLeft = distanceS4();
      frontRight = distanceS3();
      frontCenter = distanceS5();
    }
  frontAlign();
}

void wallKissing(){
  //int frontLeft = distanceS4();
  //int frontRight = distanceS3();
  int frontCenter = distanceS5();
  //frontAlign();
  while((frontCenter > 5)){ //CHANGES GOOD //the bigger number change from 10.
  customFastForward(0.01);
  //frontLeft = distanceS4();
  //frontRight = distanceS3();
  frontCenter = distanceS5();
  }
  frontAlign();
}


void centerAlign(){ // turn right, align, move back, move forward if the distance not more than 4.
  rightAlign();
  customRight(90);
  frontAlign();
  int frontLeft = distanceS4();
  int frontRight = distanceS3();
  int frontCenter = distanceS5();

  if(frontRight < frontLeft){
    difference = frontLeft - frontRight;
  }else{
    difference = frontRight - frontLeft;
  }
  if (difference <= 3){
    while((frontLeft < 5) || (frontRight < 5)){ //changed from 6
      customFastBackward(0.1);
      frontLeft = distanceS4();
      frontRight = distanceS3();
      frontCenter = distanceS5();
    }
    while((frontLeft > 5 && frontLeft < 10) || (frontRight > 5  && frontRight < 10)){  //CHANGES GOOD //the bigger number change from 10.
      customFastForward(0.1);
      frontLeft = distanceS4();
      frontRight = distanceS3();
      frontCenter = distanceS5();
    }
  }
  frontAlign();
  left_90();
  rightAlign();
}

/* ============================= Directional Code ========================== */
void left_90(){
  double leftTicks;
  leftTicks = rotationTicksLeft(90.0);  // Provide the angle
  turnLeft(leftTicks);
}
void right_90(){
  double rightTicks;
  rightTicks = rotationTicksRight(90.0);  // Provide the angle
  turnRight(rightTicks);
}
void FPleft_90(){
  double leftTicks;
  leftTicks = rotationFPTicksLeft(90.0);  // Provide the angle
  turnFPLeft(leftTicks);
}
void FPright_90(){
  double rightTicks;
  rightTicks = rotationFPTicksRight(90.0);  // Provide the angle
  turnFPRight(rightTicks);
}
void customAlignLeft(){
  double leftTicks;
  leftTicks = rotationTicksLeft(0.005);
  turnFastLeft(leftTicks);
}
void customAlignRight(){
  double rightTicks;
  rightTicks = rotationTicksRight(0.005);
  turnFastRight(rightTicks);
}
void customForward(double distances){
  double ticks;
  ticks = distToTicks_FP(distances);
  goCustomForward(ticks);
}

void customFastForward(double distances){
  double ticks;
  ticks = distToTicks(distances);
  goFastForward(ticks);
}

void singleForward(double distances){
  double ticks;
  ticks = distToTicks(distances);
  goSingleForward(ticks);
}

void customFastBackward(double distances){
  double ticks;
  ticks = distToTicks(distances);
  goFastBackwards(ticks);
}

void customRight(double degree){
  double rightTicks;
  rightTicks = rotationTicksRight(degree);
  turnRight(rightTicks);
}

void customLeft(double degree){
  double leftTicks;
  leftTicks = rotationTicksLeft(degree);
  turnLeft(leftTicks);
}
void customBack(double distances){
  double ticks;
  ticks = distToTicks(distances);
  goBackwards(ticks);
}

/* ================================= Loop ================================= */
void loop() {
/* =========================== For Communication =========================== */ 
bool FP = false;
if (Serial.available() > 0){
  int num = 0;
  String command = Serial.readStringUntil('\n'); 
  //delay(1000);
  //Serial.setTimeout(500);
  Serial.println("msg:"+ command);
  int len = command.length();
  char commandArray[len];
  // Converting the string to a char array
  command.toCharArray(commandArray, len);
  if(len > 1){
    for(int i=0; i< len;){
      char dir = commandArray[i];
      int dist ;
      if (dir == 'F'){  // IF FP meow
        
        char l = command[i+1];
        if(l == 'P'){ //IF FP go here
          dist = 0;
          FP = true;
          i+=2;
          }
        else{  //IF NOT FP
          int l = int(command[i+1]) - 48;
          int m = int(command[i+2])- 48;
          dist = (l*10) + m;
          i +=3;

        }
      }else{
        char l = command[i+1];
        dist = int(l)-48; 
        i+=2;
       }
    switch (dir){
    case 'L':
        // For turning left
        if(dist ==0){     // Just turning left
          if(FP == true){
            FPleft_90();
          }else{
            left_90();
          }
           sendSensors();
        }else{
           if(FP == true){
            FPleft_90();
          }else{
            left_90();
          }
           sendSensors();
           singleForward(dist*10);
           sendSensors();
          }     
        break;
      case 'R':
        // For turning right
        if(dist ==0){
          if(FP == true){
            FPright_90();
          }else{
            right_90();
          }
          sendSensors();
         }else{
          if(FP == true){
            FPright_90();
          }else{
            right_90();
          }
          sendSensors();
          singleForward(dist*10);
          sendSensors();
        }
        break;
      case 'F':
        // For straight
        if(dist == 1){
          singleForward(10);
          sendSensors();
        }else{
          dist = dist * 10;
          customForward(dist);
          sendSensors();
        }
        break;    
      case 'S':
        // Send sensors S0
        sendSensors();
        break; 
      case 'B':
        customRight(90);
        sendSensors();
        customRight(90);
        sendSensors();
        singleForward(10);
        sendSensors();
        break;
      case 'N':
          Serial.println("INSIDE N case:");

          while(true){
            
          // For exploration completion
          //Serial.println("Complete Exploration");
            if (Serial.available() > 0){
              String instruction = Serial.readStringUntil('\n'); 
              Serial.println("msg INSIDE N :"+ instruction);
              int instlen = instruction.length();
              char instArray[instlen];
              // Converting the string to a char array
              instruction.toCharArray(instArray, instlen);
              //Serial.println(len);
              if(instlen > 1){
                char instruct = instArray[0];
                if (instruct == 'F'){ 
                  char nextinstruction = instArray[1];
                    if (nextinstruction == 'P'){ // run fastest path
                      break; // break from the while loop and start reading the fastest path.
                    }
                }
              }
            } 
        }    
        break;
      case 'P': // Making sure that the robot is in the center.
        centerAlign();
        break;
      case 'Q':
        wallAvoidance(); // making sure it is not too close to the wall
        break;
      case 'V': // Make sure that it is not too near to the wall infront and center align itself. 
        wallAvoidance(); 
        centerAlign();
        break;
      case 'E': // Only right align meow
        rightAlign();
        break;
      case 'G': 
        frontAlign();
        break;
      case 'D':
        wallKissing();
      default: 
        shutdown();
  }
    }
  }
 }
}

/* ================================= Local Testing ================================ */
/*
int dist = 0;
switch ('Z'){
    case 'L':
        // For turning left
        if(dist ==0){     // Just turning left
          if(FP == true){
            FPleft_90();
          }else{
            left_90();
          }
           sendSensors();
        }else{
           if(FP == true){
            FPleft_90();
          }else{
            left_90();
          }
           sendSensors();
           singleForward(dist*10);
           sendSensors();
          }     
        break;
      case 'R':
        // For turning right
        if(dist ==0){
          if(FP == true){
            FPright_90();
          }else{
            right_90();
          }
          sendSensors();
         }else{
          if(FP == true){
            FPright_90();
          }else{
            right_90();
          }
          sendSensors();
          singleForward(dist*10);
          sendSensors();
        }
        break;
      case 'F':
        // For straight
        if(dist == 1){
          singleForward(10);
          sendSensors();
        }else{
          dist = dist * 10;
          customForward(dist);
          sendSensors();
        }
        break;    
      case 'S':
        // Send sensors S0
        sendSensors();
        break; 
      case 'B':
        customRight(90);
        sendSensors();
        customRight(90);
        sendSensors();
        singleForward(10);
        sendSensors();
        break;
      case 'N':
          Serial.println("INSIDE N case:");

          while(true){
            
          // For exploration completion
          //Serial.println("Complete Exploration");
            if (Serial.available() > 0){
              String instruction = Serial.readStringUntil('\n'); 
              Serial.println("msg INSIDE N :"+ instruction);
              int instlen = instruction.length();
              char instArray[instlen];
              // Converting the string to a char array
              instruction.toCharArray(instArray, instlen);
              //Serial.println(len);
              if(instlen > 1){
                char instruct = instArray[0];
                if (instruct == 'F'){ 
                  char nextinstruction = instArray[1];
                    if (nextinstruction == 'P'){ // run fastest path
                      break; // break from the while loop and start reading the fastest path.
                    }
                }
              }
            } 
        }    
        break;
      case 'P': // Making sure that the robot is in the center.
        centerAlign();
        break;
      case 'Q':
        wallAvoidance(); // making sure it is not too close to the wall
        break;
      case 'V': // Make sure that it is not too near to the wall infront and center align itself. 
        wallAvoidance(); 
        centerAlign();
        break;
      case 'E': // Only right align meow
        rightAlign();
        break;
      case 'G': 
        frontAlign();
        break;
      case 'D':
        wallKissing();
      case 'Z':
        singleForward(10);
        singleForward(10);
        singleForward(10);
        centerAlign();
        break;
      default: 
        shutdown();
  }
}*/


void shutdown(){
  while(1){
    md.setBrakes(400,400);
    delay (300);
  }
}
