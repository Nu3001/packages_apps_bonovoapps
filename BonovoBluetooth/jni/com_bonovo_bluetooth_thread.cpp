#define LOG_TAG "com_bonovo_bluetooth"
#include <stdio.h>
#include <string.h>  
#include <stdlib.h>    
#include <unistd.h>    
#include <sys/types.h>  
#include <sys/stat.h>   
#include <fcntl.h>      
#include <termios.h>    
#include <errno.h>
#include <pthread.h>
#include <utils/Log.h>
#include <asm/ioctl.h>
#include "com_bonovo_bluetooth_cmd.h"

#define BT_WRITE_DEV    "/dev/ttyS3"         	// 蓝牙写命令的设备节点
#define BT_READ_DEV     "/dev/bonovo_handle"    //"/dev/bonovo_bt"     // 蓝牙读数据的设备节点
#define BT_CTRL_DEV  "/dev/bonovo_handle" 	    // 蓝牙控制设备节点
#define BT_BUFF_SIZE    128                   	// bt's buffer size
#define BT_MAX_BUFF_SIZE 4096
#define BT_CMD          0xA2                  	// 设置蓝牙命令码
#define HANDLE_CTRL_DEV_MAJOR		230
#define IOCTL_HANDLE_BT_POWER_ON            _IO(HANDLE_CTRL_DEV_MAJOR, 20)
#define IOCTL_HANDLE_BT_POWER_OFF           _IO(HANDLE_CTRL_DEV_MAJOR, 21)
#define IOCTL_HANDLE_BT_CLEAR_BUF           _IO(HANDLE_CTRL_DEV_MAJOR, 22)
#define IOCTL_HANDLE_BT_SET_CONNECT         _IO(HANDLE_CTRL_DEV_MAJOR, 23)
#define IOCTL_HANDLE_BT_SET_DISCONNECT      _IO(HANDLE_CTRL_DEV_MAJOR, 24)
#define IOCTL_HANDLE_BT_GET_STATUS          _IO(HANDLE_CTRL_DEV_MAJOR, 25)

// codec
#define IOCTL_HANDLE_CODEC_SWITCH_SRC       _IO(HANDLE_CTRL_DEV_MAJOR, 30)
#define IOCTL_HANDLE_CODEC_RECOVER_SRC      _IO(HANDLE_CTRL_DEV_MAJOR, 31)
#define IOCTL_HANDLE_CODEC_INIT             _IO(HANDLE_CTRL_DEV_MAJOR, 32)
#define CODEC_DEFAULT_SOURCE                CODEC_LEVEL_NO_ANALOG

void android_callback(int cmd, char* param, int len);
void android_synchPhoneBook(char *na ,char *num);
int set_bonovo_bluetooth_power(int onOff);

int bonovo_bluetooth_thread_status = 0;
int bonovo_bluetooth_write_fd = -1;
int bonovo_bluetooth_read_fd = -1;
int bonovo_bluetooth_ctrl_fd = -1;
int bonovo_bluetooth_status = 0;
char myIncoingCallNumber[256];
int myIncomingCallLen = 0;
char gname [20];
char gnum [20];

const char *myBonovoBtSolicatedCmdArray[CMD_AT_MAX] = {
		//2免提应用规范指令
		"AT#CA", "AT#CB", "AT#CC", "AT#CD", "AT#CE", "AT#CF", "AT#CG", "AT#CH", "AT#CI", "AT#CJ", "AT#CK", "AT#CL",
		"AT#CM", "AT#CO", "AT#CW", "AT#CX", "AT#CY", "AT#CN", "AT#CP",
		//3语音链路层
		"AT#WI", "AT#MA", "AT#MC", "AT#MD", "AT#ME", "AT#MV", "AT#MO",
		//4电话簿
		"AT#PA", "AT#PB", "AT#PH", "AT#PI", "AT#PJ", "AT#PF", "AT#PE", "AT#PG", "AT#QA", "AT#QB", "AT#QC",
		//5其他功能操作
		"AT#CZ", "AT#CV", "AT#MY", "AT#MG", "AT#MH", "AT#MP", "AT#MQ", "AT#MF", "AT#MM", "AT#MN", "AT#MX", "AT#DA"};

unsigned int checkSum(unsigned char* cmdBuf, int size) {
	unsigned int temp = 0;
	int i;
	for (i = 0; i < size; i++) {
		temp += cmdBuf[i];
	}
	return temp;
}

int findFrameEnd(char *cmdBuf, int cmdBufLen) {
	for (int i = 1; i < cmdBufLen; i++) {
		if (cmdBuf[i - 1] == 0x0d && cmdBuf[i] == 0x0A)
			return (i + 1);
	}
	return -1;
}

/*!
 *************************************************************************************
 * function: activeAudio
 *     模拟音频切换函数
 * @param[in] CODEC_Level 要切换的模拟音频输入源
 * @return    int         0:设置成功  !0:设置失败
 *************************************************************************************
 */
int activeAudio(CODEC_Level codec_mode)
{
	int ret = 0;

	if (bonovo_bluetooth_ctrl_fd < 0) {
        return -1;
    }
	ret = ioctl(bonovo_bluetooth_ctrl_fd,IOCTL_HANDLE_CODEC_SWITCH_SRC, codec_mode);
	if(ret){
		ALOGE("%s ioctl is error. ret:%d\n", __FUNCTION__, ret);
	}
	return ret;
}

/*!
 *************************************************************************************
 * function: recoverAudio
 *     恢复切换音频前的模拟输入源
 * @return    int  0:设置成功  !0:设置失败
 *************************************************************************************
 */
int recoverAudio(CODEC_Level codec_mode)
{
    int ret = 0;

	if (bonovo_bluetooth_ctrl_fd < 0) {
        return -1;
    }
	ret = ioctl(bonovo_bluetooth_ctrl_fd, IOCTL_HANDLE_CODEC_RECOVER_SRC, codec_mode);
	if(ret){
		ALOGE("%s ioctl is error. ret:%d\n", __FUNCTION__, ret);
	}
	return ret;
}

/*!
 *********************************************
 * 解析后的姓名中不含空格字符的
 *********************************************
 */
int explainContactsWithoutSapce(char *data, int dataLen)
{
	char name[BT_BUFF_SIZE] = {'\0'};
	char telephone[BT_BUFF_SIZE] = {'\0'};
	int i, idxName, idxTel, status;

    if(dataLen > BT_BUFF_SIZE){
        ALOGE("ERROR. The data length is too large!");
        return -1;
    }

	for (i = 0, idxName = 0, idxTel = 0, status = 0; i < dataLen; i++) {
		if (((0 == status) || (2 == status))&&(' ' == data[i])) {
			continue;
		}
		if ((i+1 < dataLen) && ('P' == data[i]) && ('B' == data[i + 1])) {
			i++;
			continue;
		} else if (0xFF == data[i]) {
			status = 2;
		} else {
			if ((0x0D == data[i]) && (i + 1 < dataLen) && (0x0A == data[i + 1]))
				break;
			if (('P' == data[i]) && (i + 1 < dataLen) && ('B' == data[i + 1])){
				memset(name, 0, BT_BUFF_SIZE);
				memset(telephone, 0, BT_BUFF_SIZE);
				idxName = idxTel = 0;
				status = 0;
				i++;
				continue;
			}

			if (0 == status) {
				status = 1;
				name[idxName] = data[i];
				idxName++;
				if(idxName >= BT_BUFF_SIZE){
					ALOGE("ERROR.Name: %s, idxName >= BT_BUFF_SIZE!  status:%d.", name, status);
					return -1;
				}
			} else if (1 == status) {
				name[idxName] = data[i];
				idxName++;
				if(idxName >= BT_BUFF_SIZE){
					ALOGE("ERROR.Name: %s error. idxName >= BT_BUFF_SIZE!  status:%d.", name, status);
					return -1;
				}
			} else if (2 == status) {
				telephone[idxTel] = data[i];
				idxTel++;
				if(idxTel >= BT_BUFF_SIZE){
					ALOGE("ERROR.telephone: %s error. idxTel >= BT_BUFF_SIZE! status:%d.", telephone, status);
					return -1;
				}
			}
		}
	}
	name[idxName] = '\0';
	telephone[idxTel] = '\0';

	LOGD("idxName:%d\tName: %s", idxName, name);
	LOGD("idxTele:%d\tTele: %s", idxTel, telephone);
    if((idxName > 0) && (idxTel > 0)){

        // remove the space in the end of name
        for(i = idxName-1; i>=0; i--){
            if(name[i] == ' '){
                name[i] = '\0';
                idxName--;
            }else{
                break;
            }
        }
        android_synchPhoneBook(name,telephone);
    }
	return 0;
}

void *thread_func_bluetooth_read(void *argv) {
	char myCmdBuffer[BT_MAX_BUFF_SIZE+1];
	char myLineBuf[BT_BUFF_SIZE+1];
	char myTempBuffer[BT_BUFF_SIZE+1];
	int myCmdBufferLen = 0;
	int frameEnd;

	LOGD("thread_func_bluetooth_read start");
	if ((bonovo_bluetooth_read_fd = open(BT_READ_DEV, O_RDWR | O_NOCTTY /*| O_NONBLOCK*/)) < 0) {
		ALOGD("open %s failed! error:%d(%s)\n", BT_READ_DEV, errno, strerror(errno));
		return ((void*) 0);
	}
	bonovo_bluetooth_ctrl_fd = bonovo_bluetooth_read_fd;

	if(bonovo_bluetooth_status){
		set_bonovo_bluetooth_power(1);
	}
/*	if( (bonovo_bluetooth_ctrl_fd = open(BT_CTRL_DEV, O_RDWR | O_NOCTTY | O_NONBLOCK)) < 0) {
		ALOGD("open %s failed! error:%d(%s)\n", BT_CTRL_DEV, errno, strerror(errno));
		return ((void*) 0);
	}
*/
	if( (bonovo_bluetooth_write_fd = open(BT_WRITE_DEV, O_RDWR | O_NOCTTY | O_NONBLOCK)) < 0) {
		ALOGE("open %s failed! error:%d(%s)\n", BT_WRITE_DEV, errno, strerror(errno));
		return ((void*) 0);
	}

	while (bonovo_bluetooth_thread_status == 0) {
		int tmpLen =read(bonovo_bluetooth_read_fd, myCmdBuffer+myCmdBufferLen, BT_BUFF_SIZE-myCmdBufferLen);
		if(tmpLen >=0 ) {
			myCmdBufferLen += tmpLen;
			myCmdBuffer[myCmdBufferLen]=0;
		}
		LOGD("=============== myCmdBuffer =%s myCmdBufferLen =%d tmpLen=%d",myCmdBuffer,myCmdBufferLen, tmpLen);
		
		while(myCmdBufferLen > 0){
			frameEnd = findFrameEnd(myCmdBuffer, myCmdBufferLen);
			if (frameEnd > 0) {
				memcpy(myLineBuf,myCmdBuffer,frameEnd);
				myLineBuf[frameEnd] = 0;
				myCmdBufferLen -= frameEnd;
				if(myCmdBufferLen > 0){
					memcpy(myTempBuffer,myCmdBuffer+frameEnd,myCmdBufferLen);
					memcpy(myCmdBuffer,myTempBuffer,myCmdBufferLen);
					memset(myCmdBuffer+myCmdBufferLen, 0, myCmdBufferLen);
					//myCmdBuffer[myCmdBufferLen] = 0x00;
				}
			}else{
				if(myCmdBufferLen >= BT_BUFF_SIZE) myCmdBufferLen = 0;
				break;
			}
			
			int k;
			for(k=0; k < frameEnd; k++){
				if(myLineBuf[k] >= 'A' && myLineBuf[k]<= 'Z'){
					break;
				}
			}

			if(k >= frameEnd-1){
				//myCmdBufferLen = 0;
				LOGD("========== 0004  k:%d  frameEnd:%d", k, frameEnd);
				continue;
			}

			LOGD("+++ myLineBuf[%d]:%s", k, &myLineBuf[k]);
			if(strncmp(&myLineBuf[k], "IA\r\n", 4) == 0){
				android_callback(CMD_UNSOLICATED_IA, NULL, 0);
				// recoverAudio(CODEC_LEVEL_BT_TEL);
			}
			else if(strncmp(&myLineBuf[k], "IB\r\n", 4) == 0){
				android_callback(CMD_UNSOLICATED_IB, NULL, 0);
			}
			else if(myLineBuf[k] == 'M' && myLineBuf[k+1] == 'A'){
				android_callback(CMD_UNSOLICATED_MA, NULL, 0);
				// recoverAudio(CODEC_LEVEL_BT_MUSIC);
			}
			else if(myLineBuf[k] == 'M' && myLineBuf[k+1] == 'B'){
				android_callback(CMD_UNSOLICATED_MB, NULL, 0);
				// activeAudio(CODEC_LEVEL_BT_MUSIC);
			}
			else if(myLineBuf[k] == 'P' && myLineBuf[k+1] == 'N'){
				android_callback(CMD_UNSOLICATED_PN, NULL, 0);

            }else if(myLineBuf[k] == 'P' && myLineBuf[k+1] == 'A'){
                android_callback(CMD_UNSOLICATED_PA, &myLineBuf[k+2], frameEnd-2);
			}
			else if(myLineBuf[k] == 'P' && myLineBuf[k+1] == 'B'){
				LOGD("{myLineBuf =%s frameEnd =%d}",myLineBuf,frameEnd);
				explainContactsWithoutSapce(myLineBuf,frameEnd);
				//LOGD("gname=%s ,gnum=%s",gname,gnum);
				//android_synchPhoneBook(gname,gnum);
			}
			else if(myLineBuf[k] == 'P' && myLineBuf[k+1] == 'C'){
				android_callback(CMD_UNSOLICATED_PC, NULL, 0);
			}
			else if(myLineBuf[k] == 'P' && myLineBuf[k+1] == 'F'){
				android_callback(CMD_UNSOLICATED_PF, NULL, 0);
			}
			else if(myLineBuf[k] == 'Q' && myLineBuf[k+1] == 'B'){
				android_callback(CMD_UNSOLICATED_QB, NULL, 0);
			}
			else if(myLineBuf[k] == 'Q' && myLineBuf[k+1] == 'A'){
				android_callback(CMD_UNSOLICATED_QA, NULL, 0);
			}
			else if(myLineBuf[k] == 'C' && myLineBuf[k+1] == 'Z'){
				android_callback(CMD_UNSOLICATED_CZ, NULL, 0);
			}
			//*************** add by bonovo zbiao
			else if(myLineBuf[k] == 'I' && myLineBuf[k+1] =='C'){     // calling
//				LOGD("======zbiao CMD_UNSOLICATED_IC 0");
				android_callback(CMD_UNSOLICATED_IC, NULL, 0);
				// activeAudio(CODEC_LEVEL_BT_TEL);

			}else if(myLineBuf[k] == 'I' && myLineBuf[k+1] =='D'){    // call number
//				LOGD("======zbiao CMD_UNSOLICATED_ID 0");
				android_callback(CMD_UNSOLICATED_ID, &myLineBuf[k+2], frameEnd-2);
				// activeAudio(CODEC_LEVEL_BT_TEL);

			}else if(myLineBuf[k] == 'I' && myLineBuf[k+1] =='F'){    // call hung up
				android_callback(CMD_UNSOLICATED_IF, NULL, 0);
				// recoverAudio(CODEC_LEVEL_BT_TEL);

			}else if(myLineBuf[k] == 'I' && myLineBuf[k+1] =='G'){    // pick up the call
				android_callback(CMD_UNSOLICATED_IG, NULL, 0);
				// activeAudio(CODEC_LEVEL_BT_TEL);
			}else if(myLineBuf[k] == 'I' && myLineBuf[k+1] =='I'){    // enter pairing
				android_callback(CMD_UNSOLICATED_II, NULL, 0);
			}else if(myLineBuf[k] == 'I' && myLineBuf[k+1] =='J'){    // end pairing
				android_callback(CMD_UNSOLICATED_IJ, &myLineBuf[k+2], frameEnd-2);
			}else if(myLineBuf[k] == 'I' && myLineBuf[k+1] =='R'){    // current call number
				android_callback(CMD_UNSOLICATED_IR, &myLineBuf[k+2], frameEnd-2);
				// activeAudio(CODEC_LEVEL_BT_TEL);
			}else if(myLineBuf[k] == 'I' && myLineBuf[k+1] =='V'){    // connecting
				android_callback(CMD_UNSOLICATED_IV, NULL, 0);
			}else if(myLineBuf[k] == 'I' && myLineBuf[k+1] =='U'){    // signal strength
				android_callback(CMD_UNSOLICATED_IU, &myLineBuf[k+2], frameEnd-2);
			}else if(myLineBuf[k] == 'M' && myLineBuf[k+1] =='C'){    // audio connect
				android_callback(CMD_UNSOLICATED_MC, NULL, 0);
			}else if(myLineBuf[k] == 'M' && myLineBuf[k+1] =='D'){    // audio disconnect
				android_callback(CMD_UNSOLICATED_MD, NULL, 0);
			}else if(myLineBuf[k] == 'M' && myLineBuf[k+1] =='F'){    // auto response && auto connect status
				android_callback(CMD_UNSOLICATED_MF, &myLineBuf[k+2], frameEnd-2);
			}else if(myLineBuf[k] == 'M' && myLineBuf[k+1] =='G'){    // current HFP status
				android_callback(CMD_UNSOLICATED_MG, &myLineBuf[k+2], frameEnd-2);
			}else if(myLineBuf[k] == 'M' && myLineBuf[k+1] =='L'){    // current AVRCP status
				android_callback(CMD_UNSOLICATED_ML, &myLineBuf[k+2], frameEnd-2);
			}else if(myLineBuf[k] == 'M' && myLineBuf[k+1] =='M'){    // model name
				android_callback(CMD_UNSOLICATED_MM, &myLineBuf[k+2], frameEnd-2);
			}else if(myLineBuf[k] == 'M' && myLineBuf[k+1] =='N'){    // model pair code
				android_callback(CMD_UNSOLICATED_MN, &myLineBuf[k+2], frameEnd-2);
			}else if(myLineBuf[k] == 'M' && myLineBuf[k+1] =='U'){    // current A2DP status
				android_callback(CMD_UNSOLICATED_MU, &myLineBuf[k+2], frameEnd-2);
			}else if(myLineBuf[k] == 'M' && myLineBuf[k+1] =='W'){    // model program version
				android_callback(CMD_UNSOLICATED_MW, &myLineBuf[k+2], frameEnd-2);
			}else if(myLineBuf[k] == 'M' && myLineBuf[k+1] =='X'){    // model pair history
				android_callback(CMD_UNSOLICATED_MX, &myLineBuf[k+2], frameEnd-2);
			}else if(myLineBuf[k] == 'M' && myLineBuf[k+1] =='Y'){    // A2DP disconnect
				android_callback(CMD_UNSOLICATED_MY, NULL, 0);
				// recoverAudio(CODEC_LEVEL_BT_TEL);

			}else if(myLineBuf[k] == 'P' && myLineBuf[k+1] =='E'){    // voice dialing start
				android_callback(CMD_UNSOLICATED_PE, NULL, 0);
			}else if(myLineBuf[k] == 'P' && myLineBuf[k+1] =='F'){    // voice dialing stop
				android_callback(CMD_UNSOLICATED_PF, NULL, 0);
			}else if(myLineBuf[k] == 'O' && myLineBuf[k+1] =='K'){    // execute command successfully
				android_callback(CMD_UNSOLICATED_OK, NULL, 0);
			}else if(!strncmp(myLineBuf, "ERROR", 5)){                // execute command failed
				android_callback(CMD_UNSOLICATED_ERROR, NULL, 0);
			}else if(!strncmp(myLineBuf, "IO0", 3)){
				android_callback(CMD_UNSOLICATED_IO0, NULL, 0);          // mute false
			}else if(!strncmp(myLineBuf, "IO1", 3)){
				android_callback(CMD_UNSOLICATED_IO1, NULL, 0);          // mute true
			}
		}
	}

	bonovo_bluetooth_thread_status = 0;
	close(bonovo_bluetooth_read_fd);
	close(bonovo_bluetooth_write_fd);
	bonovo_bluetooth_read_fd = -1;
	bonovo_bluetooth_write_fd = -1;
	return NULL;
}

void start_bonovo_bluetooth(void) {
	pthread_t timerid;
	int err = 0;
	bonovo_bluetooth_thread_status = 0;
	err = pthread_create(&timerid, NULL, thread_func_bluetooth_read, NULL); //创建定时器线程
	if (err) {
		ALOGE("cant creat thread_func_bluetooth thread\n");
		return;
	}
}

void stop_bonovo_bluetooth(void) {
	bonovo_bluetooth_thread_status = 1;
}

void set_bonovo_bluetooth(int cmd, char *byteData, int byteLen) {
	unsigned char btCmd[256];
	char *btBuff = (char*) myBonovoBtSolicatedCmdArray[cmd];
	int senddata = strlen(btBuff);
	int cmdLen = senddata + 9 + byteLen;
	int retLen;
	//Head
	btCmd[0] = 0xFA;
	btCmd[1] = 0xFA;

	//Length
	btCmd[2] = cmdLen & 0x00FF;
	btCmd[3] = (cmdLen >> 8) & 0x00FF;
	//type
	btCmd[4] = BT_CMD;
	//data
	memcpy(&(btCmd[5]), btBuff, senddata);
//	if (cmd == CMD_AT_CW) {
	if((byteData != NULL) &&(byteLen > 0)){
		memcpy(&(btCmd[5 + senddata]), byteData, byteLen);
	}
	btCmd[cmdLen - 4] = '\r';
	btCmd[cmdLen - 3] = '\n';
	int sum = checkSum((unsigned char*) btCmd, cmdLen - 2);
	btCmd[cmdLen - 2] = sum & 0x00FF;
	btCmd[cmdLen - 1] = (sum >> 8) & 0x00FF;
	retLen = write(bonovo_bluetooth_write_fd, btCmd, cmdLen);
	LOGD("setCmd cmd=%d cmdBuf=%s,cmdLen=%d", cmd, &btCmd[5], cmdLen);
}

int set_bonovo_bluetooth_power(int onOff)
{
	int ret = -1;
	if (bonovo_bluetooth_read_fd < 0)
	{
		if(onOff){
			bonovo_bluetooth_status = 1;
		}else{
			bonovo_bluetooth_status = 0;
		}
		return ret;
	}
	if (onOff)
	{
		//ret = ioctl(bonovo_bluetooth_ctrl_fd, IOCTL_HANDLE_BT_POWER_OFF);
		ret = ioctl(bonovo_bluetooth_ctrl_fd, IOCTL_HANDLE_BT_POWER_ON);
	}
	else
	{
		ret = ioctl(bonovo_bluetooth_ctrl_fd, IOCTL_HANDLE_BT_POWER_OFF);
	}
	return ret;
}

