 #include <mc9s12dg256.h>
 
  int c = 0;
  
  unsigned int length = 0;
  unsigned int begin = 0;
  
  
  /*This sets the correct duty cycle for the current input signal
  according to the following table (all times in ms):
  Input Signal Length     Hz      Output Signal Length     
  (Speed)
  10                      100     1.0
  20                      50      1.1
  30                      33      1.2
  40                      25      1.3
  50                      20      1.4
  60                      17      1.5
  70                      14      1.6
  80                      12.5    1.7
  90                      11.1    1.8
  100                     10      1.9
  110                     9.09    2.0
  (Steering)
  120                     8.3     1.0
  130                     7.69    1.1
  140                     7.14    1.2
  150                     6.66    1.3
  160                     6.25    1.4
  170                     5.88    1.5
  180                     5.55    1.6
  190                     5.26    1.7
  200                     5       1.8
  210                     4.76    1.9
  220                     4.54    2.0
  */
  __interrupt void TOC0_Int(void){
 
  //rising edge
   if(!begin) {
      begin = TC0;
   } else {  //falling edge
      //temp = time between rising edge and falling edge in 1/100 secs
      length = (TC0 + (int)(0xFFFF*(TFLG2 >> 7)) + 900 - begin)/1875;
      //set steering
      if(length > 11 && length < 21) {
        PWMDTY2 = length - 1;
        PORTB = PWMDTY2;
      }
      else if(length < 12 && length > 0) {
        //set speed
          PWMDTY1 = ((length - 1) << 1) + 150;
          PORTB = PWMDTY1;
      }
      
      begin = 0;
   }
 }
 
 
 //This uses pin PT0 for input
 void init_Timer(void){
 
 asm sei
 
  //uses PT0 for input
  TIOS = 0x00; //input capture on all ports (including PT0)
  TCTL4 = 0x03; //input capture on both rising and falling edges (PT0)
  TCTL3 = 0x00; //clear input control for control logic on other ports 
  TIE = 0x01; //enable interrupt for PT0
  TSCR2 = 0x07; //set prescaler value to 128 (clock freq = 187.5 KHz)
  TSCR1 = 0x90; //enables timer/fast clear
  
  asm cli
 }
 
 
 //This uses pins PP1 for speed and PP2 for steering
 void init_PWM(void){
  //set up channel 0 and 1 for speed and channel 2 for steering
  PWMCTL = 0x10; //concatenate channels 0 and 1 into 16 bit PWM channel
  
  //channel 1 is low order bits, channel 0 is high order bits
  //all options for the 16 bit PWM channel are determined by channel 1 options 
  PWME = 0x06; //enable PWM channels 1 and 2
  PWMPOL = 0x06; //set polarity to start high/end low (channels 1 and 2)
  PWMCLK = 0x06; //clock SA is the source for channel 1 and SB for channel 2
  
  //set clock B prescaler to 16 (B = E/16)  E=24,000,000 Hz  B=1,500,000 Hz
  //and clock A prescaler to 8 (A = E/8)    A=3,000,000 Hz
  PWMPRCLK = 0x43; 
  PWMCAE =0x00; //left align outputs
  PWMSCLA = 0x0F; //SA = A/(15*2) = 100,000 Hz
  PWMSCLB = 0x4B; //SB = B/(75*2) = 10,000  Hz
  
  //The combined periods of channel 0 and 1 represent the period 
  //for the 16 bit channel (channel 0 is high order, channel 1 low order)
  //Period for 16 bit channel = (period of SA)*2000 = (1/100,000)*2000 = 0.02 seconds (50Hz)
  PWMPER0 = 0x07; //high order
  PWMPER1 = 0xD0; //low order
  PWMPER2 = 0xC8; //Period for channel 2 = (period of SA)*200 = (1/10,000)*200 = 0.02 seconds (50Hz)
  
  //clock period for channel 0 and 1 = 24*10^6/(150*200*16) = 1/50 sec = 50Hz
  //Duty cycle for 16 bit channel = (150/2000)*0.02 = 0.0015 seconds
  PWMDTY0 = 0x00;  //high order
  PWMDTY1 = 0x96;  //low order
  PWMDTY2 = 0x0F; //Duty cycle for channel 2 = (15/200)*0.02 = 0.0015 seconds
 }
   