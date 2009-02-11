 #include <mc9s12dg256.h>
 
  int c = 0;
  
  unsigned short temp = 0;
  
  __interrupt void TOC0_Int(void){
 
  //rising edge
   if(!temp) {
      temp = TC0;
   } else {  //falling edge
      //temp = time between rising edge and falling edge in 1/100 secs
      temp = (TC0 + (0xFFFF*(TFLG2 >> 7)) + 1 - temp)/1875;
      //set steering
      if(temp > 11 && temp < 22) {
        PWMDTY1 = temp -2;
      }
      else {
        //set speed
          PWMDTY0 = temp + 9;
      }
      
      temp = 0;
   }
 }
 
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
 
 void init_PWM(void){
  //set up channel 0 for speed and channel 1 for steering 
  PWME = 0x03; //enable PWM channels 0 and 1
  PWMPOL = 0x03; //set polarity to start high/end low (channels 1 and 0)
  PWMCLK = 0x03; //clock SA is the source for channels 1 and 0
  PWMPRCLK = 0x04; //set clock A prescaler to 16 (A = E / 16)
  PWMCAE =0x00; //left align outputs
  PWMSCLA = 0x4B; //SA = A / (75*2)
  PWMPER0 = 0xC8; //Period for channel 0 = SA * 200
  PWMPER1 = 0xC8; //Period for channel 1 = SA * 200
  //clock period for channel 0 and 1 = 24*10^6/(150*200*16) = 1/50 sec = 20Hz
  PWMDTY0 = 0x0F; //Duty cycle set to 15/200 of period = 0.015 ms
  PWMDTY1 = 0x0F; //Duty cycle set to 15/200 of period = 0.015 ms
 }
   