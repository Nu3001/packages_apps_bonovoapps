#include<serail.h>

/**
 * @brief  : set UART speed           
 * @author : wchao
 * @date   :
 * @param  : fd,  nSpeed----
 * @return : int
 * @retval : 0----success; -1----fault
 * @Note   : 
 **/

int set_Speed(int fd, int nSpeed)
{
	int   i;
	int   status;
	struct termios Opt;
	
	tcgetattr(fd, &Opt);
	LOGE("num = %d  %d\n",sizeof(speed_arr),sizeof(int));
/*	for ( i= 0;  i < sizeof(speed_arr) / sizeof(int);  i++)
	{
		if  (nSpeed == name_arr[i])
		{
			printf("nSpeed = %d  %d\n",nSpeed,fd);
		    tcflush(fd, TCIOFLUSH);
			cfsetispeed(&Opt, speed_arr[i]);
			cfsetospeed(&Opt, speed_arr[i]);
			printf("speed = %d \n",speed_arr[i]);
			status = tcsetattr(fd, TCSANOW, &Opt);
			if  (status != 0)
			{
		        printf("tcsetattr fd1\n");
				return(FALSE);
			}
		 	return(TRUE);
	 	}
		tcflush(fd,TCIOFLUSH);
	}
	printf("no this speed!!1\n");
	return(FALSE);*/
	tcflush(fd, TCIOFLUSH);
	switch( nSpeed )
    {
    case 2400:
        cfsetispeed(&Opt, B2400);
        cfsetospeed(&Opt, B2400);
        break;
    case 4800:
        cfsetispeed(&Opt, B4800);
        cfsetospeed(&Opt, B4800);
        break;
    case 9600:
        cfsetispeed(&Opt, B9600);
        cfsetospeed(&Opt, B9600);
        break;
    case 19200:
        cfsetispeed(&Opt, B19200);
        cfsetospeed(&Opt, B19200);
        break;
    case 115200:
        cfsetispeed(&Opt, B115200);
        cfsetospeed(&Opt, B115200);
        break;
    default:
        cfsetispeed(&Opt, B19200);
        cfsetospeed(&Opt, B19200);
        break;
    }

	status = tcsetattr(fd, TCSANOW, &Opt);
	if  (status != 0)
	{
        LOGE("tcsetattr fd1\n");
		return(FALSE);
	}

	return(TRUE);
}

/**
 * @brief  : Configation UART           
 * @author : wchao
 * @date   :
 * @param  : fd,  nBits----, nParity----, nStop----
 * @return : int
 * @retval : 0----success; -1----fault
 * @Note   : 
 **/
int set_Parity(int fd,int nBits,int nParity,int nStop)
{
	struct termios newtio,oldtio;

	tcflush(fd, TCIOFLUSH);
	if  ( tcgetattr( fd,&oldtio)  !=  0)
	{
		LOGE("SetupSerial 1\n");
		return(FALSE);
	}

	bzero( &newtio, sizeof( newtio ) );
    newtio.c_cflag  |=  CLOCAL | CREAD;
    newtio.c_cflag &= ~CSIZE;
	
	switch (nBits) /*set databit*/
	{
	case 5:
        newtio.c_cflag |= CS5;
        break;
    case 6:
        newtio.c_cflag |= CS6;
        break;
    case 7:
        newtio.c_cflag |= CS7;
        break;
    case 8:
        newtio.c_cflag |= CS8;
        break;
    default:
        newtio.c_cflag |= CS8; //Unsupported data define CS8
        break;
	}
	
	switch (nParity)
	{
	case 'n':
	case 'N':
		newtio.c_cflag &= ~PARENB;			/* Clear parity enable */
		newtio.c_iflag &= ~INPCK;			/* Enable parity checking */
		break;
	case 'o':
	case 'O':
		newtio.c_cflag |= PARENB;
        newtio.c_cflag |= PARODD;			/* set party*/
        newtio.c_iflag |= (INPCK | ISTRIP);	/* Disnable parity checking */            
		break;
	case 'e':
	case 'E':
		newtio.c_iflag |= (INPCK | ISTRIP);	/* Disnable parity checking */
        newtio.c_cflag |= PARENB;			/* Enable parity */
        newtio.c_cflag &= ~PARODD;			/* Set party*/       
		break;
	case 'S':
	case 's':  /*as no parity*/
		newtio.c_cflag &= ~PARENB;
		newtio.c_cflag &= ~CSTOPB;
		break;
	default:
		newtio.c_cflag &= ~PARENB;
		newtio.c_iflag &= ~INPCK;
		break;
	}
	
	/* set stop*/   
	switch (nStop)
	{
	case 1:
		newtio.c_cflag &=  ~CSTOPB;
		break;
	case 2:
		newtio.c_cflag |=  CSTOPB;
		break;
	default:
		LOGE(stderr,"Unsupported stop bits\n");
		return (FALSE);
	}
	
	newtio.c_cc[VTIME] = 0; 
	newtio.c_cc[VMIN] = 0;

	tcflush(fd,TCIFLUSH); /* Update the options and do it NOW */
	if (tcsetattr(fd,TCSANOW,&newtio) != 0)
	{
		LOGE("SetupSerial 3\n");
		return (FALSE);
	}
	
#ifdef TEST_BUG
	LOGE("set done!\n");
#endif
	return (TRUE);
 }

 /**
  * @brief	: open UART device
  * @author : wchao
  * @date	:
  * @param	: fd
  * @return : int
  * @retval : fd----success -1----fault
  * @Note	: 
  **/
 int open_Serial(int* fd, char* dev)
 {
	 *fd = open(dev, O_RDWR|O_NOCTTY|O_NONBLOCK);
	 if (-1 == *fd)
	 {
		 LOGE("Can't Open Serial Port\n");
		 return(FALSE);
	 }
	 else
	 {
#ifdef TEST_BUG
		 LOGE("open tcc-uart secess, fd = %d, &fd = 0x%x\n",*fd,fd);
#endif
	 }
 
	 if(fcntl(*fd, F_SETFL, 0) < 0)
	 {
		 LOGE("fcntl failed!\n");
		 close(*fd);
		 return(FALSE);
	 }
	 else
	 {
#ifdef TEST_BUG
		 LOGE("fcntl=%d\n",fcntl(*fd, F_SETFL,0));
#endif
	 }
 
	 if(isatty(STDIN_FILENO) == 0)
	 {
		 LOGE("standard input is not a terminal device\n");
		// close(*fd);
		 //return(FALSE);
	 }
	 else
	 {
#ifdef TEST_BUG
		 LOGE("isatty success!\n");
#endif
	 }
 
	 return(TRUE);
 }

 /**
  * @brief	: close UART device
  * @author : wchao
  * @date	:
  * @param	: fd
  * @return : int
  * @retval : fd----success -1----fault
  * @Note	: 
  **/

 int close_Serial(int fd)
 {
	 if(close(fd) < 0)
	 {
		 LOGE("close serial error!\n");
		 return (FALSE);
	 }
	 else
	 {
#ifdef TEST_BUG
		 LOGE("close serial secess!\n");
#endif
		 return (TRUE);
	 }
 }

 int  read_Serial(int fd, unsigned char *data, int size)
 {
	 unsigned int dwByteRead;	 //receive Byte
	 if(size > 0)
	 {
		 dwByteRead = read(fd, data, size);
	 }
 
	 return dwByteRead;
 }

 int write_Serial(int fd, unsigned char *data, int size)
 {
	 unsigned int dwByteWrite;	 //transmit Byte
	 if(size > 0)
	 {
		 dwByteWrite = write(fd, data, size);
	 }
	 return dwByteWrite;
 }

