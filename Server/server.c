/*
 * server.c
 *
 *  Created on: Mar 9, 2009
 *      Author: sethschwiethale
 */

#include <stdio.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <netinet/in.h>
//these are for the alarm output
#include <fcntl.h>
#include <sys/ioctl.h>
#include "bufferReceive.h"
#include <asm/arch/gpiodriver.h>

#define MAXPENDING 5    /* Max connection requests */
#define BUFFSIZE 3

int fda;
int inta = 1;
int l = 0;
int a = 1 << 3;

void Crash(char *err) { perror(err); exit(1); }

/**
 * turn alarm on and off specified amount of times
 */
void pulse(int state){
	int i;
	printf("COMMAND RECEIVED: %d\n", state);
	int high, low;
	for(i = 0; i<state; i++){
		//printf("%d - ",i);
		high = ioctl(fda, _IO(GPIO_IOCTYPE, IO_SETBITS), a);
		if(high < 0){
			printf("error high");
		}
		usleep(1);
		//printf("%s","on_");
		low = ioctl(fda, _IO(GPIO_IOCTYPE, IO_CLRBITS), a);
		if(low < 0){
			printf("error low");
		}
		usleep(1);
		//printf("%s\n","off");
	}
	//after the right amount of pulses have been sent
	//sleep for 20 ms for microprocessor to know end of signal
	//printf("%s","END_");
	high = ioctl(fda, _IO(GPIO_IOCTYPE, IO_SETBITS), a);
	if(high < 0){
		printf("error high");
	}
	//printf("%s","on_");
	usleep(20000);
	low = ioctl(fda, _IO(GPIO_IOCTYPE, IO_CLRBITS), a);
	if(low < 0){
		printf("error low");
	}
	//printf("%s","off\n");
}

void HandleClient(int sock) {
	char buffer[BUFFSIZE];
	//int received = -1;
	int state = 0;

	//replaces above commented out code
	while(recv_all(sock, buffer)){
		state = atoi(buffer);
		pulse(state);
		state = 0;
	}

	close(sock);
	printf("closing socket");
}//end HandleClient


int main(int argc, char *argv[]) {
	int serversock, clientsock;
	struct sockaddr_in echoserver, echoclient;

	//file for writing
	fda = open("/dev/gpioa", O_RDWR);
	//ioctl(fda, _IO(GPIO_IOCTYPE, IO_SETGET_OUTPUT), &inta);

	if (argc != 2) {
		fprintf(stderr, "USAGE: server.o <port>\n");
		exit(1);
	}

	/* Create the TCP socket */
	if ((serversock = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP)) < 0) {
		Crash("failed creating socket");
	}
	/* Construct the server sockaddr_in structure */
	memset(&echoserver, 0, sizeof(echoserver));       /* Clear struct */
	echoserver.sin_family = AF_INET;                  /* Internet/IP */
	echoserver.sin_addr.s_addr = htonl(INADDR_ANY);   /* Incoming addr */
	echoserver.sin_port = htons(atoi(argv[1]));       /* server port */

	/* Bind to the local address */
	if (bind(serversock, (struct sockaddr *) &echoserver, sizeof(echoserver)) < 0) {
		Crash("bind() failed");
	}
	/* Mark the socket so it will listen for incoming connections  */
	if (listen(serversock, MAXPENDING) < 0) {
		Crash("listen() failed");
	}
	/* Run until canceled */
	while (1) {
		unsigned int clientlen = sizeof(echoclient);
		/* Wait for client connection */
		if ((clientsock = accept(serversock, (struct sockaddr *) &echoclient, &clientlen)) < 0)
		{
			Crash("failed accepting client connection");
		}
		fprintf(stdout, "Client connected: %s\n", inet_ntoa(echoclient.sin_addr));
		HandleClient(clientsock);
	}
}
