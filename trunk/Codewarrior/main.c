/* Example program for the Wytec Dragon 12 Revision E (MC9S12DG256C) */

#include <mc9s12dg256.h>        /* derivative information */
#include "pll.h"
#include "modulate.h"								/* defines _BUSCLOCK, sets bus frequency to _BUSCLOCK MHz */


void main(void) {

  /* set system clock frequency to _BUSCLOCK MHz (24 or 4) */
  PLL_Init();

  /* set port B as output (LEDs) */
  DDRJ |= 0x02;       // Port J1 as an output
  PTJ &= ~0x02;       // Pull J1 low (enable LEDs)
  DDRB  = 0xff;       // Port B is output
  PORTB = 0x55;       // switch on every other LED
  
  
  init_PWM();
  
  init_Timer();
  
  

  asm cli

  /* forever */
  for(;;){}
}
