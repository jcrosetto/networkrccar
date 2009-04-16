/* to assure the program reads integers in completely and doesn't split
 * say, 12 into '1' and then '2'
 *
 */

int recv_all(int sockfd, unsigned char *buffer){
#define END "\n" //grab everything before this
#define END_SIZE 1
	unsigned char *ptr;
	int end_match = 0;
	
	ptr = buffer;
	while(recv(sockfd, ptr, 1, 0) == 1) {//read single byte
		if(*ptr == END[end_match]){
			end_match++;
			if(end_match == END_SIZE){
				*(ptr+1-END_SIZE) = '\0';
				return strlen(buffer);
			}
		}
		else{
			end_match = 0;
		}
		ptr++;	//increment pointer
	}
	return 0;	//didn't find new line char
}