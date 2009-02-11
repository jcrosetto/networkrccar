#include "modulate.h"

extern void near _Startup(void);       /* Startup routine */

/* declarations of interrupt service routines */
//extern __interrupt void SCI1_RX_isr(void);
//extern __interrupt void RTI_isr(void);


#pragma CODE_SEG __NEAR_SEG NON_BANKED /* Interrupt section for this module. Placement will be in NON_BANKED area. */
__interrupt void UnimplementedISR(void) {

   /* Unimplemented ISRs trap.*/
   asm BGND;
}

typedef void (*near tIsrFunc)(void);
const tIsrFunc _vect[] @0xFF80 = {     /* Interrupt table */
        UnimplementedISR,                 /* vector 63 : (reserved) */
        UnimplementedISR,                 /* vector 62 : (reserved) */
        UnimplementedISR,                 /* vector 61 : (reserved) */
        UnimplementedISR,                 /* vector 60 : (reserved) */
        UnimplementedISR,                 /* vector 59 : (reserved) */
        UnimplementedISR,                 /* vector 58 : (reserved) */
        UnimplementedISR,                 /* vector 57 : PWM emergency shutdown */
        UnimplementedISR,                 /* vector 56 : PORT P */
        UnimplementedISR,                 /* vector 55 : MSCAN4 - transmit */
        UnimplementedISR,                 /* vector 54 : MSCAN4 - receive */
        UnimplementedISR,                 /* vector 53 : MSCAN4 - errors */
        UnimplementedISR,                 /* vector 52 : MSCAN4 - wakeup */
        UnimplementedISR,                 /* vector 51 : MSCAN3 - transmit */
        UnimplementedISR,                 /* vector 50 : MSCAN3 - receive */
        UnimplementedISR,                 /* vector 49 : MSCAN3 - errors */
        UnimplementedISR,                 /* vector 48 : MSCAN3 - wakeup */
        UnimplementedISR,                 /* vector 47 : MSCAN2 - transmit */
        UnimplementedISR,                 /* vector 46 : MSCAN2 - receive */
        UnimplementedISR,                 /* vector 45 : MSCAN2 - errors */
        UnimplementedISR,                 /* vector 44 : MSCAN2 - wakeup */
        UnimplementedISR,                 /* vector 43 : MSCAN1 - transmit */
        UnimplementedISR,                 /* vector 42 : MSCAN1 - receive */
        UnimplementedISR,                 /* vector 41 : MSCAN1 - errors */
        UnimplementedISR,                 /* vector 40 : MSCAN1 - wakeup */
        UnimplementedISR,                 /* vector 39 : MSCAN0 - transmit */
        UnimplementedISR,                 /* vector 38 : MSCAN0 - receive */
        UnimplementedISR,                 /* vector 37 : MSCAN0 - errors */
        UnimplementedISR,                 /* vector 36 : MSCAN0 - wakeup */
        UnimplementedISR,                 /* vector 35 : FLASH */
        UnimplementedISR,                 /* vector 34 : EEPROM */
        UnimplementedISR,                 /* vector 33 : SPI2 */
        UnimplementedISR,                 /* vector 32 : SPI1 */
        UnimplementedISR,                 /* vector 31 : IIC bus */
        UnimplementedISR,                 /* vector 30 : DLC */
        UnimplementedISR,                 /* vector 29 : SCME */
        UnimplementedISR,                 /* vector 28 : CRG lock */
        UnimplementedISR,                 /* vector 27 : Pulse accumulator B overflow */
        UnimplementedISR,                 /* vector 26 : Modulus down counter underflow */
        UnimplementedISR,                 /* vector 25 : PORT H */
        UnimplementedISR,                 /* vector 24 : PORT J */
        UnimplementedISR,                 /* vector 23 : ATD1 */
        UnimplementedISR,                 /* vector 22 : ATD0 */
        UnimplementedISR,                 /* vector 21 : SCI1 (TIE, TCIE, RIE, ILIE) */
        UnimplementedISR,                 /* vector 20 : SCI0 (TIE, TCIE, RIE, ILIE) */
        UnimplementedISR,                 /* vector 19 : SPI0 */
        UnimplementedISR,                 /* vector 18 : Pulse accumulator input edge */
        UnimplementedISR,                 /* vector 17 : Pulse accumulator A overflow */
        UnimplementedISR,                 /* vector 16 : Timer Overflow (TOF) */
        UnimplementedISR,                 /* vector 15 : Timer channel 7 */
        UnimplementedISR,                 /* vector 14 : Timer channel 6 */
        UnimplementedISR,                 /* vector 13 : Timer channel 5 */
        UnimplementedISR,                 /* vector 12 : Timer channel 4 */
        UnimplementedISR,                 /* vector 11 : Timer channel 3 */
        UnimplementedISR,                 /* vector 10 : Timer channel 2 */
        UnimplementedISR,                 /* vector 09 : Timer channel 1 */
        TOC0_Int,                 /* vector 08 : Timer channel 0 */
        UnimplementedISR,                 /* vector 07 : Real-Time Interrupt (RTI) */
        UnimplementedISR,                 /* vector 06 : IRQ */
        UnimplementedISR,                 /* vector 05 : XIRQ */
        UnimplementedISR,                 /* vector 04 : SWI */
        UnimplementedISR,                 /* vector 03 : Unimplemented Instruction trap */
        UnimplementedISR,                 /* vector 02 : COP failure reset*/
        UnimplementedISR,                 /* vector 01 : Clock monitor fail reset */
        _Startup                          /* vector 00 : Reset vector */
   };