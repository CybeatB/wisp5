/**
 * @file       usr.c
 * @brief      WISP application-specific code set
 * @details    The WISP application developer's implementation goes here.
 *
 * @author     Aaron Parks, UW Sensor Systems Lab
 *
 */

#include "wisp-base.h"
#include "bloc/AES/aes-naive.h"

WISP_dataStructInterface_t wispData;
uint8_t authd;
uint16_t password = 0xCAFE;
/* 7A65 C0DE ACCE 55E5 DA7A FA7E CAFE F00D */
uint8_t key[] = { 0x7A, 0x65, 0xC0, 0xDE, 0xAC, 0xCE, 0x55, 0xE5, 0xDA, 0x7A, 0xFA, 0x7E, 0xCA, 0xFE, 0xF0, 0x0D };
uint8_t rkey[16] = {0};

/*
 * Pulse LED at a given rate for a given number of times
 * @param count number of times to pulse
 * @param delay btwn pulses and after last pulse
*/
void ledBlinks (uint8_t count, uint16_t duration) {

	while(count--) {
		// Stay on for ~1ms, then wait for specified duration
		BITSET(PLED1OUT,PIN_LED1);
		Timer_LooseDelay(32);
		BITCLR(PLED1OUT,PIN_LED1);
		Timer_LooseDelay(duration);
	}
	return;
}

/** 
 * This function is called by WISP FW after a successful ACK reply
 *
 */
void my_ackCallback (void) {
  asm(" NOP");
}

/**
 * This function is called by WISP FW after a successful read command
 *  reception
 *
 */
void my_readCallback (void) {
  //asm(" NOP");
	if (RWData.memBank == 0) {
		if (RWData.wordPtr == 0) {
			//generate RN16
			//encrypt RN16
			//store RN16, ready for Fetch
		}
		if (RWData.wordPtr == 1) {
			//RN16 sent
		}
	}
}

/**
 * This function is called by WISP FW after a successful write command
 *  reception
 *
 */
void my_writeCallback (void) {
  //asm(" NOP");
	/* Authorization Attempt */
	if (RWData.memBank == 0) {
		if (RWData.wordPtr == 0) {
			// Reset Auth
			authd = 0;

			// Generate RN16
			rkey[0] = *((uint8_t *) rfid.rn8_ind);
			rfid.rn8_ind++;
			rkey[1] = *((uint8_t *) rfid.rn8_ind);
			rfid.rn8_ind++;
			wispData.epcBuf[0] = rkey[0];
			wispData.epcBuf[1] = rkey[1];

			// Encrypt RN16
			AES_CTX ctx;
			START_ENCRYPT();
			AES_init(&ctx, &key[0]);
			AES_encrypt(&ctx, &rkey[0], &(RWData.RESBankPtr[4]));
			END_EXPE();
		} else {
			// Check Password
			uint16_t keychk = rkey[0];
			keychk = (keychk << 8) | rkey[1];
			uint16_t pwd = RWData.wrData ^ keychk;
			if (pwd == password) {
				authd = RWData.wordPtr;
			}
		}
	}

	/* Check Authorization & Write Data */
	if (authd > 0 && RWData.memBank == 3) {
		((uint16_t*)RWData.USRBankPtr)[RWData.wordPtr] = RWData.wrData;
		authd--;
	}

	/* Remaining Writes Transmitted in EPC */
	wispData.epcBuf[8] = authd;
}

/** 
 * This function is called by WISP FW after a successful BlockWrite
 *  command decode

 */
void my_blockWriteCallback  (void) {
  asm(" NOP");
}


/**
 * This implements the user application and should never return
 *
 * Must call WISP_init() in the first line of main()
 * Must call WISP_doRFID() at some point to start interacting with a reader
 */
void main(void) {

  WISP_init();
  
  // Register callback functions with WISP comm routines
  WISP_registerCallback_ACK(&my_ackCallback);
  WISP_registerCallback_READ(&my_readCallback);
  WISP_registerCallback_WRITE(&my_writeCallback);
  WISP_registerCallback_BLOCKWRITE(&my_blockWriteCallback);
  
  // Initialize BlockWrite data buffer.
  uint16_t bwr_array[6] = {0};
  RWData.bwrBufPtr = bwr_array;
  
  // Get access to EPC, READ, and WRITE data buffers
  WISP_getDataBuffers(&wispData);

  // Set up Reserved Bank for Authentication
  uint8_t resmem[20];
  uint8_t i;
  for (i = 0; i < 20; i++) {
	  resmem[i] = i;
  }
  RWData.RESBankPtr = &resmem[0];
  
  // Set up operating parameters for WISP comm routines
  WISP_setMode( MODE_READ | MODE_WRITE | MODE_USES_SEL); 
  WISP_setAbortConditions(CMD_ID_READ | CMD_ID_WRITE /*| CMD_ID_ACK*/);
  
  // Set up EPC
  /*
  wispData.epcBuf[0] = 0x00; 		// Tag type
  wispData.epcBuf[1] = 0;			// Unused data field
  wispData.epcBuf[2] = 0;			// Unused data field
  wispData.epcBuf[3] = 0;			// Unused data field
  wispData.epcBuf[4] = 0;			// Unused data field
  wispData.epcBuf[5] = 0;			// Unused data field
  wispData.epcBuf[6] = 0;			// Unused data field
  wispData.epcBuf[7] = 0x00;		// Unused data field
  wispData.epcBuf[8] = 0x00;		// Unused data field
  wispData.epcBuf[9] = 0x51;		// Tag hardware revision (5.1)
  wispData.epcBuf[10] = *((uint8_t*)INFO_WISP_TAGID+1); // WISP ID MSB: Pull from INFO seg
  wispData.epcBuf[11] = *((uint8_t*)INFO_WISP_TAGID); // WISP ID LSB: Pull from INFO seg
  */
  wispData.epcBuf[0] = 0x00; 		// Tag type
  wispData.epcBuf[1] = 0;			// Unused data field
  wispData.epcBuf[2] = 0;			// Unused data field
  wispData.epcBuf[3] = 0;			// Unused data field
  wispData.epcBuf[4] = 0;			// Unused data field
  wispData.epcBuf[5] = 0;			// Unused data field
  wispData.epcBuf[6] = 0;			// Unused data field
  wispData.epcBuf[7] = 0x00;		// Unused data field
  wispData.epcBuf[8] = authd;		// Number of Authorized Writes Remaining
  wispData.epcBuf[9] = 0x51;		// Tag hardware revision (5.1)
  wispData.epcBuf[10] = *((uint8_t*)INFO_WISP_TAGID+1); // WISP ID MSB: Pull from INFO seg
  wispData.epcBuf[11] = *((uint8_t*)INFO_WISP_TAGID); // WISP ID LSB: Pull from INFO seg
  
  // Talk to the RFID reader.
  while (FOREVER) {
    WISP_doRFID();
  }
}
