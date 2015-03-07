#define LOG_TAG "com_bonovo_mcu.cpp"

#include <asm/ioctl.h>

#include <assert.h>
#include <limits.h>
#include <utils/threads.h>
#include <main.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <stdlib.h>

#include "jni.h"
#include "JNIHelp.h"
#include "android_runtime/AndroidRuntime.h"

#define HANDLE_CTRL_DEV_MAJOR		230
#define CTRL_DEV_MAJOR		231
#define CTRL_DEV_MINOR		0

#define HANDLE_DEV_NAME              "/dev/bonovo_mcu_update"     			// 设备节点
#define RADIO_DEV_NODE               "/dev/ttyS3"    			 			// 设备节点
#define MCU_CTRL_NODE             "/dev/bonovo_handle"

static int fd_mcu = -1;                             						// MCU的文件描述符
static int fd_tty3 = -1;                             						// 打开串口3的文件描述符
static int fd_mcu_status = -1;												// 打开bonovo_handle文件描述符


#define IOCTL_HANDLE_CLEAR_BUF           _IO(CTRL_DEV_MAJOR, 20)			//清除字符设备缓冲区
#define IOCTL_HANDLE_MCU_LIGHT_STATUS       _IO(HANDLE_CTRL_DEV_MAJOR, 40)
#define IOCTL_HANDLE_MCU_REVERSE_STATUS       _IO(HANDLE_CTRL_DEV_MAJOR, 41)
#define IOCTL_HANDLE_GET_BRIGHTNESS		    _IO(HANDLE_CTRL_DEV_MAJOR, 1)

#define DEBUG_INFO
#ifdef DEBUG_INFO
#define debug(format, ...) \
	do{ \
	LOGD(format, ##__VA_ARGS__); \
	}while(0)
#else
#define debug(format, ...) do{}while(0)
#endif

/*!
 *************************************************************************************
 * function: checkSum
 *     计算校验和函数
 * @param[in] cmdBuf 要计算校验和的数据存放的缓冲区指针
 * @param[in] size   缓冲区中有效数据的字节数
 * @return           计算出来的校验和
 *************************************************************************************
 */
unsigned int checkSum(unsigned char* cmdBuf, int size)
{
	unsigned int temp = 0;
	int i;
	for(i=0; i < size; i++)
	{
		temp += cmdBuf[i];
	}
	return temp;
}

/*!
 *************************************************************************************
 * function: checkMcuVersion
 *     查询MCU版本信息
 * @return           返回执行结果
 *            - 0     执行成功
 *            - -1	  打开tty3串口失败
 *            - -2    打开fd_mcu串口失败
 *            - -3    向tty3串口写失败
 *            - mversion  返回从fd_mcu读到的数据
 *************************************************************************************
 */
static jbyteArray android_mcu_checkMcuVersion(JNIEnv *env, jobject thiz)
{
	int writeSize,ReadSize;
	jbyte version[4];
	unsigned int sum = 0;
	unsigned char cmdBuf[7] = {0xFA, 0xFA, 0x07, 0x00, 0x84};
	LOGE("checkMcuVersion()\n");
	jbyteArray mversion = env->NewByteArray(4);

	sum = checkSum(cmdBuf, cmdBuf[2]-2);
	cmdBuf[5] = sum & 0xFF;
	cmdBuf[6] = (sum>>8)& 0xFF;

	memset(version, 0, 4);

	if((fd_tty3 = open(RADIO_DEV_NODE, O_RDWR|O_NOCTTY)) < 0)
	{
		LOGE("open %s failed,fd=%d(%d,%s)\n", RADIO_DEV_NODE, fd_tty3, errno, strerror(errno));
		return (jbyteArray)-1;
	}

	if((fd_mcu = open(HANDLE_DEV_NAME, O_RDWR|O_NOCTTY)) < 0)
	{
		LOGE("Can't open /dev/bonovo_mcu_update\n");
		return (jbyteArray)-2;
	}

//	if((fd_mcu_status = open(MCU_CTRL_NODE, O_RDWR|O_NOCTTY)) < 0)
//		{
//			LOGE("Can't open /dev/bonovo_handle\n");
//			return (jbyteArray)-2;
//		}

	ioctl(fd_mcu, IOCTL_HANDLE_CLEAR_BUF);

	if((writeSize = write(fd_tty3, cmdBuf, cmdBuf[2])) < 0)
	{
		LOGE("write ttyS3 error(%d, %s)\n", errno, strerror(errno));
		return (jbyteArray)-3;
	}
	LOGE("checkMcuVersion()--->writeSize = %d\n",writeSize);

	ReadSize = read(fd_mcu,version,4);
	LOGE("checkMcuVersion()--->ReadSize=%d\n",ReadSize);
//	if(ReadSize < 0){
//		read(fd_mcu,version,4);
//	}
//	for(int i=0;i<4;i++){
//		LOGE("version[%d]=%x!\n",i,version[i]);
//	}

	env->SetByteArrayRegion(mversion, 0, 4, version);

	return mversion;
}

/*!
 *************************************************************************************
 * function: wipeMcuAPP
 *     擦除MCU APP数据
 * @return           返回执行结果
 *            - 0     执行成功
 *            - -1	  串口没有打开
 *            - -2    串口发送命令失败
 *            - -3   读串口失败
 *            -  1   擦除失败
 *            -  2   擦除成功
 *************************************************************************************
 */
static int android_mcu_wipeMcuAPP(JNIEnv *env, jobject thiz) {
	int writeSize;
	int ReadSize;
	unsigned int sum = 0;
	unsigned char cmdBuf[7] = { 0xFA, 0xFA, 0x07, 0x00, 0x86 };
	char buffer[2];
	const char *message[2][3] = {
			{"earse error\n", "earse ok\n", "earse .....\n",},
			{"write error\n", "write ok\n", "write .....\n",}
	};
	memset(buffer, 0, 2);
	LOGE("wipeMcuAPP()\n");

	if (fd_mcu < 0) {
		LOGE("The mcu was not be opened.  fd_mcu = %d\n",fd_mcu);
		return -1;
	}
	sum = checkSum(cmdBuf, cmdBuf[2] - 2);
	cmdBuf[5] = sum & 0xFF;
	cmdBuf[6] = (sum >> 8) & 0xFF;

	ioctl(fd_mcu, IOCTL_HANDLE_CLEAR_BUF);

	if ((writeSize = write(fd_tty3, cmdBuf, cmdBuf[2])) < 0) {
		LOGE("write ttyS3 error(%d, %s)\n", errno, strerror(errno));
		return -2;
	}

	while (1) {
		read(fd_mcu, buffer, 2);
		if (buffer[0] == 0 && buffer[1] == 1)
		{
			LOGE("%s",message[buffer[0]][buffer[1]]);
			return 2;
		}
	}

//	while (1) {
//		read(fd_mcu, buffer, 2);
//		if (buffer[0] < 2 && buffer[1] < 3)
//		{
//			LOGE("%s",message[buffer[0]][buffer[1]]);
//		}
//	}

	return 0;
}

/*!
 *************************************************************************************
 * function: updateMcuAPP
 *     更新MCU Flash数据
 * @return           返回执行结果
 *            - 0     执行成功
 *            - -1	  串口没有打开
 *            - -2    串口发送命令失败
 *            -  1   写入失败
 *            -  2   写入成功
 *************************************************************************************
 */
static int android_mcu_updateMcu(JNIEnv *env, jobject thiz, jbyteArray mbuf,jint offset)
{
	int writeSize,readsize;
	unsigned int sum = 0;
	unsigned char cmdBuf[137] = {0xFA, 0xFA, 0x89, 0x00, 0x87};
	const char *message[2][3] = {
			{"earse error\n", "earse ok\n", "earse .....\n",},
			{"write error\n", "write ok\n", "write .....\n",}
	};
	char mbuffer[2]  ;
	memset(mbuffer, 0, 2);

	jint len = env->GetArrayLength(mbuf);
	jbyte *point = env->GetByteArrayElements(mbuf, 0);
	debug("updateMcu()\n");
	//LOGE("offset = %d mbuf len= %d\n",offset,len);


	if(fd_mcu < 0)
	{
		LOGE("The mcu was not be opened.\n");
		return -1;
	}

	cmdBuf[5] = offset & 0xFF;
	cmdBuf[6] = (offset >> 8) & 0xFF;

	for(int idex=7,currid =0; idex < 135 ; idex++,currid++){
		cmdBuf[idex] = point[currid];
	}

	sum = checkSum(cmdBuf, cmdBuf[2]-2);
	cmdBuf[135] = sum & 0xFF;
	cmdBuf[136] = (sum>>8)& 0xFF;
//	for(int i=0;i<137;i++){
//		LOGE("cmdBuf[%d]=%2x",i,cmdBuf[i]);
//	}

	int ret = ioctl(fd_mcu, IOCTL_HANDLE_CLEAR_BUF);

	if((writeSize = write(fd_tty3, cmdBuf, cmdBuf[2])) <= 0)
	{
		LOGE("write ttyS3 error(%d, %s)\n", errno, strerror(errno));
		return -2;
	}

	debug("============ writeSize= %d ==============\n", writeSize);

	while (1) {
		readsize = read(fd_mcu, mbuffer, 2);
		debug("readsize  = %d \n",readsize);
		if(readsize > 0){
			//LOGE("readsize:%d\n", readsize);
			if(readsize == 2){
				if (mbuffer[0] == 1 && mbuffer[1] == 1)
				{
					debug("%s",message[mbuffer[0]][mbuffer[1]]);
					env->ReleaseByteArrayElements(mbuf, point, 0);
					return 2;
				}
				else if (mbuffer[0] == 1 && mbuffer[1] == 0) {
					debug("%s",message[mbuffer[0]][mbuffer[1]]);
					env->ReleaseByteArrayElements(mbuf, point, 0);
					return 1;
				}else if(mbuffer[0] == 1 && mbuffer[1] == 2){
					continue;
				}else{
					//LOGE("buffer[0] = %02x  buffer[1] = %02x\n",mbuffer[0],mbuffer[1]);
				}
			}else{
				//LOGE("readsize:%d\n", readsize);
				env->ReleaseByteArrayElements(mbuf, point, 0);
				return 1;
			}
		}
		//sleep(1);
	}

	env->ReleaseByteArrayElements(mbuf, point, 0);

	return 0;
}

/*!
 *************************************************************************************
 * function: resetMcu
 *     重启MCU
 * @return           返回执行结果
 *            - 1     执行成功
 *            - -1	  串口没有打开
 *            - -2    串口发送命令失败
 *************************************************************************************
 */
static int android_mcu_rebootMcu(JNIEnv *env, jobject thiz)
{
	int writeSize;
	unsigned int sum = 0;
	unsigned char cmdBuf[7] = {0xFA, 0xFA, 0x07, 0x00, 0x85};
	LOGE("resetMcu()\n");

	 if(fd_mcu < 0)
		{
			LOGE("The mcu was not be opened.\n");
			return -1;
		}
	sum = checkSum(cmdBuf, cmdBuf[2]-2);
	cmdBuf[5] = sum & 0xFF;
	cmdBuf[6] = (sum>>8)& 0xFF;

	ioctl(fd_mcu, IOCTL_HANDLE_CLEAR_BUF);

	if((writeSize = write(fd_tty3, cmdBuf, cmdBuf[2])) < 0)
		{
			LOGE("write ttyS3 error(%d, %s)\n", errno, strerror(errno));
			return -2;
		}

	close(fd_mcu);
	close(fd_tty3);

	return 1;
}

/*!
 *************************************************************************************
 * function: requestMcuFlash
 *     请求获取MCU Flash数据
 * @return           返回执行结果
 *            - rebyt     执行成功
 *            - -1	  串口没有打开
 *            - -2    串口发送写命令失败
 *            - -3    串口读失败
 *************************************************************************************
 */
static jbyteArray android_mcu_requestMcuFlash(JNIEnv *env, jobject thiz, jint offset)
{
	int writeSize;
	int ReadSize = 0 ;


	jbyte buffer[128];
	unsigned int sum = 0;
	unsigned char cmdBuf[9] = {0xFA, 0xFA, 0x09, 0x00, 0x88};
	jbyteArray rebyt = env->NewByteArray(128);
	debug("requestMcuFlash()--->offset == %d\n",offset);

	 if(fd_mcu < 0)
		{
			LOGE("The mcu was not be opened.\n");
			return (jbyteArray)-1;
		}

	cmdBuf[5] = offset & 0xFF;
	cmdBuf[6] = (offset >> 8)& 0xFF;

	sum = checkSum(cmdBuf, cmdBuf[2]-2);
	cmdBuf[7] = sum & 0xFF;
	cmdBuf[8] = (sum>>8)& 0xFF;

//	for(int i=0;i<9;i++){
//		LOGE("cmdBuf[%d]=%x",i,cmdBuf[i]);
//	}

	ioctl(fd_mcu, IOCTL_HANDLE_CLEAR_BUF);

	if((writeSize = write(fd_tty3, cmdBuf, cmdBuf[2])) < 0)
	{
		LOGE("write ttyS3 error(%d, %s)\n", errno, strerror(errno));
		return (jbyteArray)-2;
	}

//	do{
//		ReadSize = read(fd_mcu,buffer+ReadSize,128-ReadSize);
//	}while(ReadSize < 128);
	ReadSize = read(fd_mcu,buffer+ReadSize,128-ReadSize);
	if(ReadSize<0){
		return (jbyteArray)-3;
	}
	env->SetByteArrayRegion(rebyt, 0, 128, buffer);

	return rebyt;
}

/*!
 *************************************************************************************
 * function: asternMute
 *     倒车静音
 * @return           返回执行结果
 *            - 0     执行成功
 *            - -1	  串口没有打开
 *************************************************************************************
 */
static int android_mcu_asternMute(JNIEnv *env, jobject thiz, jint onOff)
{
	int writeSize,fd;
	unsigned int sum = 0;
	unsigned char cmdBuf[10] = {0xFA, 0xFA, 0x0A, 0x00, 0x83, 0x05};
	LOGE("asternMute() onOff = %d\n",onOff);

//	 if(fd_mcu < 0)
//		{
//			LOGE("The mcu was not be opened.\n");
//			return -1;
//		}

	cmdBuf[6] = onOff & 0xFF;
	cmdBuf[7] = (onOff>>8)& 0xFF;
	sum = checkSum(cmdBuf, cmdBuf[2]-2);
	cmdBuf[8] = sum & 0xFF;
	cmdBuf[9] = (sum>>8)& 0xFF;

//		for(int i=0;i<10;i++){
//			LOGE("cmdBuf[%d]=%x",i,cmdBuf[i]);
//		}

	if((fd = open(RADIO_DEV_NODE, O_RDWR|O_NOCTTY|O_NONBLOCK)) < 0) {
			LOGI("======bonovo dev/ttyS3 SetMute open result = %d\n",fd);
			return -1;
		} else {
			writeSize = write(fd, cmdBuf, cmdBuf[2]);
			LOGE("asternMute writeSize = %d ",writeSize);
			close(fd);
			return 0;
		}

//	if((writeSize = write(fd_tty3, cmdBuf, cmdBuf[2])) < 0)
//		{
//			LOGE("write ttyS3 error(%d, %s)\n", errno, strerror(errno));
//			return -2;
//		}
//	return 1;
}

/*!
 *************************************************************************************
 * function: queryHeadlight
 *     查询大灯状态
 * @return           返回执行结果
 *            - state     执行成功
 *            - JNI_FALSE	  串口没有打开
 *************************************************************************************
 */
static int android_mcu_queryHeadlight(JNIEnv *env, jobject thiz)
{
//	int state ;
//	LOGE("queryHeadlight()\n",state);
//	 if(fd_mcu_status < 0)
//		{
//			LOGE("The handle was not be opened.\n");
//			return -1;
//		}
//
//	state = ioctl(fd_mcu_status, IOCTL_HANDLE_MCU_LIGHT_STATUS);
//	LOGE("Light State = %d !!!!!!\n",state);

	int fd, state;
	fd = open(MCU_CTRL_NODE, O_RDWR|O_NOCTTY|O_NONBLOCK);
		if (-1 == fd)
		{
			LOGE("Can't Open bonovo_handle\n");
			return(JNI_FALSE);
		}
		else
		{
			LOGE("open bonovo_handle secess, fd = 0x%x\n",fd);
			state = ioctl(fd, IOCTL_HANDLE_MCU_LIGHT_STATUS);
			close(fd);
		}


	return state;
}

/*@
 * function : android_mcu_queryReverseStatus
 * 
 * @return the car's reverse status
 */
static int android_mcu_queryReverseStatus(JNIEnv *env, jobject thiz)
{
    int fd, state;
    fd = open(MCU_CTRL_NODE, O_RDWR|O_NOCTTY|O_NONBLOCK);
    if (-1 == fd)
    {
        LOGE("Can't Open bonovo_handle\n");
        return(JNI_FALSE);
    }else{
        LOGE("open bonovo_handle secess, fd = 0x%x\n",fd);
        state = ioctl(fd, IOCTL_HANDLE_MCU_REVERSE_STATUS);
        close(fd);
    }

    return state;
}

/*@
 * function : android_mcu_ctrl_screen()
 *
 * @return  int  0 successful 
 *              -1 failed
 */
static int android_mcu_ctrl_screen(JNIEnv *env, jobject thiz,
    jboolean isShowAndroid)
{
    int fd, len, sum, ret = 0;
    unsigned char cmd[10] = {0xFA, 0xFA, 0x0A, 0x00, 0x83, 0x09, 0x00, 0x00, 0x00, 0x00};
    if((fd = open(RADIO_DEV_NODE, O_RDWR|O_NOCTTY|O_NONBLOCK)) < 0) {
		LOGI("open dev/ttyS3 failed in android_mcu_ctrl_screen. result = %d\n",fd);
		return -1;
	}

    if(isShowAndroid){
        cmd[6] = 0x01;
    }else{
        cmd[6] = 0x00;
    }
    cmd[7] = 0x00;
    sum = checkSum(cmd, sizeof(cmd) - 2);
    cmd[8] = sum & 0xFF;
	cmd[9] = (sum>>8)& 0xFF;
	len = write(fd, cmd, sizeof(cmd));
    if(len <= 0){
        LOGE("android_mcu_ctrl_screen  write ttyS3 error. len:%d\n", len);
        ret = -1;
    }
	close(fd);
	return ret;
}


/*!
 *************************************************************************************
 * function: rearviewCamera
 *     倒车摄像头
 * @return           返回执行结果
 *            - 1     执行成功
 *            - -1	  串口没有打开
 *            - -2    串口发送命令失败
 *************************************************************************************
 */
static int android_mcu_rearviewCamera(JNIEnv *env, jobject thiz, jint camera)
{
	int writeSize,fd;
	unsigned int sum = 0;
	unsigned char cmdBuf[10] = {0xFA, 0xFA, 0x0A, 0x00, 0x83, 0x04};
	LOGE("rearviewCamera() Camera = %d\n",camera);

	cmdBuf[6] = camera & 0xFF;
	cmdBuf[7] = (camera>>8)& 0xFF;
	sum = checkSum(cmdBuf, cmdBuf[2]-2);
	cmdBuf[8] = sum & 0xFF;
	cmdBuf[9] = (sum>>8)& 0xFF;

//		for(int i=0;i<10;i++){
//			LOGE("cmdBuf[%d]=%x",i,cmdBuf[i]);
//		}

	if((fd = open(RADIO_DEV_NODE, O_RDWR|O_NOCTTY|O_NONBLOCK)) < 0) {
			LOGI("======bonovo dev/ttyS3 SetMute open result = %d\n",fd);
			return -1;
		} else {
			writeSize = write(fd, cmdBuf, cmdBuf[2]);
			LOGE("rearviewCamera writeSize = %d ",writeSize);
			close(fd);
			return 0;
		}

//	if((writeSize = write(fd_tty3, cmdBuf, cmdBuf[2])) < 0)
//		{
//			LOGE("write ttyS3 error(%d, %s)\n", errno, strerror(errno));
//			return -2;
//		}
//
//	return 1;
}

/*!
 *************************************************************************************
 * function: getBrightness
 *     获取背光值
 * @return           返回执行结果
 *            - brightness     执行成功
 *            - -1	  串口没有打开
 *************************************************************************************
 */
static int android_mcu_getBrightness(JNIEnv *env, jobject thiz)
{
//	int brightness ;
//	LOGE("getBrightness()\n",brightness);
//	 if(fd_mcu_status < 0)
//		{
//			LOGE("The handle was not be opened.\n");
//			return -1;
//		}
//
//	 brightness = ioctl(fd_mcu_status, IOCTL_HANDLE_GET_BRIGHTNESS);
//	 LOGE("getBrightness = %d !!!!!!\n",brightness);


	int fd, brightness;
	fd = open(MCU_CTRL_NODE, O_RDWR|O_NOCTTY|O_NONBLOCK);
		if (-1 == fd)
		{
			LOGE("Can't Open bonovo_handle\n");
			return(JNI_FALSE);
		}
		else
		{
			LOGE("open bonovo_handle secess, fd = 0x%x\n",fd);
			brightness = ioctl(fd, IOCTL_HANDLE_GET_BRIGHTNESS);
			close(fd);
		}

	return brightness;
}

/*!
 *************************************************************************************
 * function: setBrightness
 *     设置背光值
 * @return           返回执行结果
 *            - 0     执行成功
 *            - -1	  串口没有打开
 *************************************************************************************
 */
static int android_mcu_setBrightness(JNIEnv *env, jobject thiz, jint brightness)
{
	int writeSize,fd;
	unsigned int sum = 0;
	unsigned char cmdBuf[7] = {0xFA, 0xFA, 0x08, 0x00, 0x82};
	LOGE("setBrightness() brightness = %d\n",brightness);

	cmdBuf[5] = brightness;
	sum = checkSum(cmdBuf, cmdBuf[2]-2);
	cmdBuf[6] = sum & 0xFF;
	cmdBuf[7] = (sum>>8)& 0xFF;

//		for(int i=0;i<7;i++){
//			LOGE("cmdBuf[%d]=%x",i,cmdBuf[i]);
//		}

	if((fd = open(RADIO_DEV_NODE, O_RDWR|O_NOCTTY|O_NONBLOCK)) < 0) {
			LOGI("======bonovo dev/ttyS3 SetMute open result = %d\n",fd);
			return -1;
		} else {
			writeSize = write(fd, cmdBuf, cmdBuf[2]);
			close(fd);
			return 0;
		}

//	if((writeSize = write(fd_tty3, cmdBuf, cmdBuf[2])) < 0)
//		{
//			LOGE("write ttyS3 error(%d, %s)\n", errno, strerror(errno));
//			return -2;
//		}
//
//	return 1;
}

/*!
 *************************************************************************************
 * function: lowBrigthness
 *     大灯时降低背光
 * @parameter		- 0 自动背光功能关闭 ， 非零 背光比例值，从10到100，表示原背光值的10%到100%
 * @return           返回执行结果
 *            - 0     执行成功
 *            - -1	  串口没有打开
 *************************************************************************************
 */
static int android_mcu_lowBrigthness(JNIEnv *env, jobject thiz, jint low)
{
	int writeSize,fd;
	unsigned int sum = 0;
	unsigned char cmdBuf[10] = {0xFA, 0xFA, 0x0A, 0x00, 0x83, 0x06};
	LOGE("lowBrigthness() low = %d\n",low);

	cmdBuf[6] = low & 0xFF;
	cmdBuf[7] = (low>>8)& 0xFF;
	sum = checkSum(cmdBuf, cmdBuf[2]-2);
	cmdBuf[8] = sum & 0xFF;
	cmdBuf[9] = (sum>>8)& 0xFF;

	if((fd = open(RADIO_DEV_NODE, O_RDWR|O_NOCTTY|O_NONBLOCK)) < 0) {
			LOGI("======bonovo dev/ttyS3 SetMute open result = %d\n",fd);
			return -1;
		} else {
			writeSize = write(fd, cmdBuf, cmdBuf[2]);
			LOGE("lowBrigthness writeSize = %d ",writeSize);
			close(fd);
			return 0;
		}
}

/*!
 *************************************************************************************
 * function: lowBrigthness
 *     大灯时降低背光
 * @parameter		- 0 自动背光功能关闭 ， 非零 背光比例值，从10到100，表示原背光值的10%到100%
 * @return           返回执行结果
 *            - 0     执行成功
 *            - -1	  串口没有打开
 *************************************************************************************
 */
static int android_mcu_autoVolume(JNIEnv *env, jobject thiz, jint percent)
{
	int writeSize,fd;
	unsigned int sum = 0;
	unsigned char cmdBuf[10] = {0xFA, 0xFA, 0x0A, 0x00, 0x83, 0x07};
	LOGE("autoVolume() percent = %d\n",percent);

	cmdBuf[6] = percent & 0xFF;
	cmdBuf[7] = (percent>>8)& 0xFF;
	sum = checkSum(cmdBuf, cmdBuf[2]-2);
	cmdBuf[8] = sum & 0xFF;
	cmdBuf[9] = (sum>>8)& 0xFF;

	if((fd = open(RADIO_DEV_NODE, O_RDWR|O_NOCTTY|O_NONBLOCK)) < 0) {
			LOGI("======bonovo dev/ttyS3 SetMute open result = %d\n",fd);
			return -1;
		} else {
			writeSize = write(fd, cmdBuf, cmdBuf[2]);
			LOGE("lowBrigthness writeSize = %d ",writeSize);
			close(fd);
			return 0;
		}
}

static const char *classPathName = "com/example/bonovomcu/McuServicer";
static JNINativeMethod methods[] = {
   	{"jniCheckMcuVersion","()[B",(void *)android_mcu_checkMcuVersion},
   	{"jniWipeMcuAPP","()I",(void *)android_mcu_wipeMcuAPP},
   	{"jniUpdateMcu","([BI)I",(void *)android_mcu_updateMcu},
   	{"jniRebootMcu","()I",(void *)android_mcu_rebootMcu},
   	{"jnirequestMcuFlash","(I)[B",(void *)android_mcu_requestMcuFlash},
   	{"jniAsternMute","(I)I",(void *)android_mcu_asternMute},
   	{"jniqueryHeadlight","()I",(void *)android_mcu_queryHeadlight},
   	{"jnirearviewCamera","(I)I",(void *)android_mcu_rearviewCamera},
   	{"jnigetBrightness","()I",(void *)android_mcu_getBrightness},
   	{"jnisetBrightness","(I)I",(void *)android_mcu_setBrightness},
   	{"jnilowBrigthness","(I)I",(void *)android_mcu_lowBrigthness},
   	{"jniautoVolume","(I)I",(void *)android_mcu_autoVolume},
   	{"jniQueryReverse", "()I", (void *)android_mcu_queryReverseStatus},
   	{"jniControlScreen", "(Z)I", (void *)android_mcu_ctrl_screen},
};

/*
 * Register several native methods for one class.
 */
static int registerNativeMethods(JNIEnv* env, const char* className,
    JNINativeMethod* methods, int numMethods)
{
    jclass clazz;

    clazz = env->FindClass(className);
    if (clazz == NULL) {
        LOGE("Native registration unable to find class '%s'", className);
        return JNI_FALSE;
    }

    if (env->RegisterNatives(clazz, methods, numMethods) < 0) {
        LOGE("RegisterNatives failed for '%s'", className);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 * Register native methods for all classes we know about.
 *
 * returns JNI_TRUE on success.
 */
static int registerNatives(JNIEnv* env)
{
  if (!registerNativeMethods(env, classPathName,
          methods, sizeof(methods) / sizeof(methods[0]))) {
    return JNI_FALSE;
  }

  return JNI_TRUE;
}

/*
 * This is called by the VM when the shared library is first loaded.
 */

typedef union {
    JNIEnv* env;
    void* venv;
} UnionJNIEnvToVoid;

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    UnionJNIEnvToVoid uenv;
    uenv.venv = NULL;
    jint result = -1;
    JNIEnv* env = NULL;

    LOGI("JNI_OnLoad");

    if (vm->GetEnv(&uenv.venv, JNI_VERSION_1_6) != JNI_OK) {
        LOGE("ERROR: GetEnv failed");
        goto bail;
    }
    env = uenv.env;

    if (registerNatives(env) != JNI_TRUE) {
        LOGE("ERROR: registerNatives failed");
        goto bail;
    }

    result = JNI_VERSION_1_6;

bail:
    return result;
}
