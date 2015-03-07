#define LOG_TAG "com_bonovo_radio.cpp"

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
#define HANDLE_CTRL_DEV_MINOR		0

#define IOCTL_HANDLE_GET_BRIGHTNESS		    _IO(HANDLE_CTRL_DEV_MAJOR, 1)
#define IOCTL_HANDLE_GET_WINCEVOLUME		_IO(HANDLE_CTRL_DEV_MAJOR, 2)
#define IOCTL_HANDLE_GET_RADIO_STATUS       _IO(HANDLE_CTRL_DEV_MAJOR, 3)
#define IOCTL_HANDLE_START_RADIO_SEARCH     _IO(HANDLE_CTRL_DEV_MAJOR, 4)
#define IOCTL_HANDLE_STOP_RADIO_SEARCH      _IO(HANDLE_CTRL_DEV_MAJOR, 5)
#define IOCTL_HANDLE_GET_RADIO_CURR_FREQ    _IO(HANDLE_CTRL_DEV_MAJOR, 6)
#define IOCTL_HANDLE_GET_RADIO_FREQ         _IO(HANDLE_CTRL_DEV_MAJOR, 7)
#define IOCTL_HANDLE_CLEAR_RADIO_BUFF       _IO(HANDLE_CTRL_DEV_MAJOR, 8)

//#define DEBUG_INFO
#ifdef DEBUG_INFO
#define debug(format, ...) \
	do{ \
	LOGD(format, ##__VA_ARGS__); \
	}while(0)
#else
#define debug(format, ...) do{}while(0)
#endif

#define RADIO_DEV_NODE              "/dev/ttyS3"    // 设备节点
#define AUDIO_CTRL_NODE             "/dev/bonovo_handle"

///////////////////////////////////////////////////////

// codec
#define IOCTL_HANDLE_CODEC_SWITCH_SRC       _IO(HANDLE_CTRL_DEV_MAJOR, 30)
#define IOCTL_HANDLE_CODEC_RECOVER_SRC      _IO(HANDLE_CTRL_DEV_MAJOR, 31)
#define IOCTL_HANDLE_CODEC_INIT             _IO(HANDLE_CTRL_DEV_MAJOR, 32)

// codec status
typedef enum
{
	CODEC_LEVEL_NO_ANALOG = 0,
    CODEC_LEVEL_BT_MUSIC = 1,
    CODEC_LEVEL_AV_IN = 2,
    CODEC_LEVEL_DVB = 3,
    CODEC_LEVEL_DVD = 4,
    CODEC_LEVEL_RADIO = 5,
    CODEC_LEVEL_BT_TEL = 6,
    CODEC_LEVEL_COUNT
}CODEC_Level;
#define CODEC_DEFAULT_SOURCE         CODEC_LEVEL_NO_ANALOG
///////////////////////////////////////////////////////

// about codec control
#define CMD_CODEC_AUDIO_SRC_SELECT  0               // 外部模拟音频选择开关
#define CMD_CODEC_MUX_SRC_SELECT    1               // MUX输入源选择
#define CMD_CODEC_MIXER_SRC_SELECT  2               // MIXER输入源选择
#define CMD_CODEC_MIXER_DAC_VOLUME  3               // MIXER DAC音量设置
#define CMD_CODEC_MIXER_ANA_VOLUME  4               // MIXER模拟音量设置
#define CMD_CODEC_LOUT1_VOLUME      5               // LOUT1音量设置
#define CMD_CODEC_ROUT1_VOLUME      6               // ROUT1音量设置
#define CMD_CODEC_LOUT2_VOLUME      7               // LOUT2音量设置
#define CMD_CODEC_ROUT2_VOLUME      8               // ROUT2音量设置
#define CMD_CODEC_OUTPUT_VOLUME     9               // 设置所有输出音量值
#define CMD_CODEC_MUTE              10              // 静音设置
#define CMD_CODEC_STEREO_STRENGTHEN 11              // 3D立体声加强设置

// about radio control
#define CMD_RADIO_STANDARD_MODEL    0               // 制式设置
#define CMD_RADIO_BAND_SELECT       1               // 频段选择
#define CMD_RADIO_RDS_ON_OFF        2               // RDS开关控制
#define CMD_RADIO_VOLUME            3               // 音量设置
#define CMD_RADIO_MUTE              4               // 静音设置
#define CMD_RADIO_FREQ              5               // 电台频率设置
#define CMD_RADIO_START_SEARCH      6               // 搜台命令
#define CMD_RADIO_STOP_SEARCH       7               // 停止搜台
#define CMD_RADIO_STEREO_STRENGTHEN 8               // 3D立体声加强设置
#define CMD_RADIO_SHUTDOWN          9               // 关闭收音机
#define CMD_RADIO_MIN_FREQ          10              // 设置可搜索的最小频率
#define CMD_RADIO_MAX_FREQ          11              // 设置可搜索的最大频率
#define CMD_RADIO_STEP_LEN          12              // 设置搜索电台的步长
#define CMD_RADIO_REMOTE            13              // 远程本地切换

/* about standard model */
// FM
#define CHINA_FM_FREQ_MIN           8700         // 中国制式FM的最小频率值（以10KHz为单位）
#define CHINA_FM_FREQ_MAX           10800        // 中国制式FM的最大频率值（以10KHz为单位）
#define CHINA_FM_STEP_LENGTH        10           // 中国制式FM的步长（以10KHz为单位）
#define JNP_FM_FREQ_MIN             7600         // 日本制式FM的最小频率值（以10KHz为单位）
#define JNP_FM_FREQ_MAX             9000         // 日本制式FM的最大频率值（以10KHz为单位）
#define JNP_FM_STEP_LENGTH          10           // 日本制式FM的步长（以10KHz为单位）
#define EUROPE_FM_FREQ_MIN          8700         // 欧洲制式FM的最小频率值（以10KHz为单位）
#define EUROPE_FM_FREQ_MAX          10800        // 欧洲制式FM的最大频率值（以10KHz为单位）
#define EUROPE_FM_STEP_LENGTH       5            // 欧洲制式FM的步长（以10KHz为单位）
// AM
#define CHINA_AM_FREQ_MIN           531          // 中国制式AM的最小频率值（以KHz为单位）
#define CHINA_AM_FREQ_MAX           1602         // 中国制式AM的最大频率值（以KHz为单位）
#define CHINA_AM_STEP_LENGTH        9            // 中国制式AM的步长（以KHz为单位）
#define JNP_AM_FREQ_MIN             522          // 日本制式AM的最小频率值（以KHz为单位）
#define JNP_AM_FREQ_MAX             1620         // 日本制式AM的最大频率值（以KHz为单位）
#define JNP_AM_STEP_LENGTH          9            // 日本制式AM的步长（以KHz为单位）
#define EUROPE_AM_FREQ_MIN          522          // 欧洲制式AM的最小频率值（以KHz为单位）
#define EUROPE_AM_FREQ_MAX          1620         // 欧洲制式AM的最大频率值（以KHz为单位）
#define EUROPE_AM_STEP_LENGTH       9            // 欧洲制式AM的步长（以KHz为单位）
// standard model selectors
#define MODEL_CHINA                 0            // 中国制式
#define MODEL_JNP                   1            // 日本制式
#define MODEL_EUROPE                2            // 欧洲制式

/* band */
#define BAND_FM                     0            // 调频
#define BAND_AM                     1            // 调幅
#define BAND_SW                     2            // 短波
#define BAND_LW                     3            // 长波

struct radio_freq
{
	unsigned char freq[2];
	unsigned char is_valid;
};

static int fd_radio = -1;                           // 打开串口3的文件描述符。
static int fd_bonovo= -1;                           // 读取搜索的电台频率的文件描述符。 
static int cur_model= MODEL_CHINA;                  // 默认的制式
static int cur_band = BAND_FM;                      // 默认的频段
static int freq_min = CHINA_FM_FREQ_MIN;            // 默认的FM可搜索的最小频率值 
static int freq_max = CHINA_FM_FREQ_MAX;            // 默认的FM可搜索的最大频率值
static int step_len = CHINA_FM_STEP_LENGTH;         // 默认的FM搜索步长
static int mtype;                                   // FM AM切换标记

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
	if (fd_bonovo < 0) {
        return -1;
    }
	ret = ioctl(fd_bonovo,IOCTL_HANDLE_CODEC_SWITCH_SRC, codec_mode);
	if(ret){
		ALOGE("[=== BONOVO ===]%s ioctl is error. ret:%d\n", __FUNCTION__, ret);
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

	if (fd_bonovo < 0) {
        return -1;
    }
	ret = ioctl(fd_bonovo, IOCTL_HANDLE_CODEC_RECOVER_SRC, codec_mode);
	if(ret){
		ALOGE("[=== BONOVO ===]%s ioctl is error. ret:%d\n", __FUNCTION__, ret);
	}
	return ret;
}

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

/*@
 *************************************************************************************
 * function: setRangeAndStep
 *    设置搜索频率范围和搜索步长
 * @param[in] mode    选择的制式
 * @param[in] band    选择的频段
 * @return            返回执行结果
 *            - 0     执行成功
 *            - -1    无效的参数或者串口还未正常打开
 *            - -2    串口发送命令失败
 *************************************************************************************
 */
int setRangeAndStep(int mode, int band)
{
	int ret;
	unsigned int sum = 0;
	unsigned char cmdBuf[10] = {0xFA, 0xFA, 0x0A, 0x00, 0xA1};

	if((mode < 0)||(mode > 2)||(band < 0)||(band > 3)||(fd_radio < 0))
		return -1;
	cur_model = mode;
	cur_band = band;
	switch(cur_band)
	{
	case BAND_FM:
		switch(cur_model)
		{
		case MODEL_CHINA:
			freq_min = CHINA_FM_FREQ_MIN;
			freq_max = CHINA_FM_FREQ_MAX;
			step_len = CHINA_FM_STEP_LENGTH;
			break;
		case MODEL_JNP:
			freq_min = JNP_FM_FREQ_MIN;
			freq_max = JNP_FM_FREQ_MAX;
			step_len = JNP_FM_STEP_LENGTH;
			break;
		case MODEL_EUROPE:
			freq_min = EUROPE_FM_FREQ_MIN;
			freq_max = EUROPE_FM_FREQ_MAX;
			step_len = EUROPE_FM_STEP_LENGTH;
			break;
		default:
			debug("Current standard model is error.\n");
			return -1;
			break;
		}
		//send command to set 
		break;
	case BAND_AM:
		switch(cur_model)
		{
		case MODEL_CHINA:
			freq_min = CHINA_AM_FREQ_MIN;
			freq_max = CHINA_AM_FREQ_MAX;
			step_len = CHINA_AM_STEP_LENGTH;
			break;
		case MODEL_JNP:
			freq_min = JNP_AM_FREQ_MIN;
			freq_max = JNP_AM_FREQ_MAX;
			step_len = JNP_AM_STEP_LENGTH;
			break;
		case MODEL_EUROPE:
			freq_min = EUROPE_AM_FREQ_MIN;
			freq_max = EUROPE_AM_FREQ_MAX;
			step_len = EUROPE_AM_STEP_LENGTH;
			break;
		default:
			LOGE("Current standard model is error.");
			return -1;
			break;
		}
		break;
	case BAND_SW:
	case BAND_LW:
		debug("SW or LM is not be realized.");
		return -1;
		break;
	default:
		LOGE("Current band is error. current band is:%d.", cur_model);
		return -1;
		break;
	}

	// set min frequency
	cmdBuf[5] = CMD_RADIO_MIN_FREQ;
	cmdBuf[6] = freq_min & 0x0FF;
	cmdBuf[7] = (freq_min>>8)& 0x0FF;
	sum = checkSum(cmdBuf, cmdBuf[2]-2);
	cmdBuf[8] = sum & 0xFF;
	cmdBuf[9] = (sum>>8)& 0xFF;
	if((ret = write(fd_radio, cmdBuf, cmdBuf[2])) < 0)
	{
		LOGE("write ttyS3 error(%d, %s)\n", errno, strerror(errno));
		return -2;
	}
	debug("setRangeAndStep(%d,%d) set radio min frequence freq_min:%d\n",cur_model, cur_band, freq_min);

	// set max frequency
	cmdBuf[5] = CMD_RADIO_MAX_FREQ;
	cmdBuf[6] = freq_max & 0x0FF;
	cmdBuf[7] = (freq_max>>8)& 0x0FF;
	sum = checkSum(cmdBuf, cmdBuf[2]-2);
	cmdBuf[8] = sum & 0xFF;
	cmdBuf[9] = (sum>>8)& 0xFF;
	if((ret = write(fd_radio, cmdBuf, cmdBuf[2])) < 0)
	{
		LOGE("write ttyS3 error(%d, %s)\n", errno, strerror(errno));
		return -2;
	}
	debug("setRangeAndStep(%d,%d) set radio max frequence freq_max:%d\n",cur_model, cur_band, freq_max);
	// set step length
	cmdBuf[5] = CMD_RADIO_STEP_LEN;
	cmdBuf[6] = step_len & 0x0FF;
	cmdBuf[7] = (step_len>>8)& 0x0FF;
	sum = checkSum(cmdBuf, cmdBuf[2]-2);
	cmdBuf[8] = sum & 0xFF;
	cmdBuf[9] = (sum>>8)& 0xFF;
	if((ret = write(fd_radio, cmdBuf, cmdBuf[2])) < 0)
	{
		LOGE("write ttyS3 error(%d, %s)\n", errno, strerror(errno));
		return -2;
	}
	debug("setRangeAndStep(%d,%d) set radio step length frequence step_len:%d\n",cur_model, cur_band, step_len);
    return 0;
}

/*!
 **************************************************************************************************
 * function: android_radio_PowerOnoff
 *     启动radio函数，主要工作是打开串口并设置收音机的频率freq。
 *
 * @param[in] OnOff  对收音机模块的操作
 *            - !1   关闭收音机
 *            - 1    打开收音机
 * @return           执行结果
 *            - 0    操作执行成功
 *            - -1   打开收音机时，返回该值说明打开串口失败；关闭收音机时，返回该值说明串口之前没有成功打开
 *            - -2   返回该值说明写串口失败；
 **************************************************************************************************
 */
static int android_radio_PowerOnoff(JNIEnv *env, jobject thiz, jint OnOff)
{
	int dwByteWrite, ret;
	unsigned int sum = 0;
	unsigned char cmdBuf[10] = {0xFA, 0xFA, 0x0A, 0x00, 0xA1, CMD_RADIO_FREQ};
	
    if(OnOff == 1)
    {
		if((fd_radio = open(RADIO_DEV_NODE, O_RDWR|O_NOCTTY|O_NONBLOCK)) < 0) {
			LOGE("open %s failed,fd=%d(%d,%s)\n", RADIO_DEV_NODE, fd_radio, errno, strerror(errno));
			return -1;
		}
		fd_bonovo = open(AUDIO_CTRL_NODE, O_RDWR|O_NOCTTY|O_NONBLOCK);
		if (-1 == fd_bonovo)
		{
			LOGE("Can't open /dev/bonovo_handle!\n");
			return -1;
		}
		if(activeAudio(CODEC_LEVEL_RADIO) != 0)
		{
		    LOGE("Can't switch analog audio source to radio.\n");
		}
		//if((ret = setRangeAndStep(model, band)) < 0)
		//{
		//	close(fd_radio);
		//	return ret;
		//}
		//cmdBuf[5] = CMD_RADIO_FREQ;
		//cmdBuf[6] = freq & 0x0FF;
		//cmdBuf[7] = (freq>>8)& 0x0FF;
		//sum = checkSum(cmdBuf, cmdBuf[2]-2);
		//cmdBuf[8] = sum & 0xFF;
		//cmdBuf[9] = (sum>>8)& 0xFF;

		//if((dwByteWrite = write(fd_radio, cmdBuf, cmdBuf[2])) < 0)
		//{
		//	LOGE("write ttyS3 error(%d, %s)\n", errno, strerror(errno));
		//	return -2;
		//}
        debug("android_radio_PowerOnoff on fd_radio='%d'",fd_radio);
    }
    else
    {
        debug("android_radio_PowerOnoff off fd_radio='%d'",fd_radio);
        if(fd_radio < 0)
        {
            LOGE("android_radio_PowerOnoff off fd_radio < 0");
            return -1;
        }
		recoverAudio(CODEC_LEVEL_RADIO);
		cmdBuf[5] = CMD_RADIO_SHUTDOWN;
		cmdBuf[6] = 0;
		cmdBuf[7] = 0;
		sum = checkSum(cmdBuf, cmdBuf[2]-2);
		cmdBuf[8] = sum & 0xFF;
		cmdBuf[9] = (sum>>8)& 0xFF;

		if((dwByteWrite = write(fd_radio, cmdBuf, cmdBuf[2])) < 0)
		{
			LOGE("write ttyS3 error(%d, %s)\n", errno, strerror(errno));
			return -2;
		}
        close(fd_radio);
		close(fd_bonovo);
    }
	return 0;
}

/*!
 *************************************************************************************
 * function: android_radio_SetVolume
 *     设置收音机音量
 * @param[in] volume 收音机的音量（百分百，比如要设置总音量的10%，则volume=10）
 * @return           函数执行的结果
 *            - 0    设置收音机音量命令发送成功。
 *            - -1   串口还没有成功打开，需要执行android_radio_PowerOnoff来打开串口
 *            - -2   设置收音机音量的命令发送失败。
 *************************************************************************************
 */
static int android_radio_SetVolume(JNIEnv *env, jobject thiz, jint volume)
{
	int writeSize;
	unsigned int sum = 0;
	unsigned char cmdBuf[10] = {0xFA, 0xFA, 0x0A, 0x00, 0xA1, CMD_RADIO_VOLUME};

	debug("android_radio_SetVolume(%d)\n", volume);
	if(fd_radio < 0)
	{
		LOGE("The radio was not be opened.\n");
		return -1;
	}
	if(volume > 100)
		volume = 100;
	if(volume < 0)
		volume = 0;
	cmdBuf[6] = volume & 0x0FF;
	cmdBuf[7] = 0;
	sum = checkSum(cmdBuf, cmdBuf[2]-2);
	cmdBuf[8] = sum & 0xFF;
	cmdBuf[9] = (sum>>8)& 0xFF;
	if((writeSize = write(fd_radio, cmdBuf, cmdBuf[2])) < 0)
	{
		LOGE("write ttyS3 error(%d, %s)\n", errno, strerror(errno));
		return -2;
	}
	return 0;
}

/*!
 *************************************************************************************
 * function: android_radio_SetMute
 *     打开或者关闭收音机静音功能
 * @param[in] muteState 设置静音的操作
 *            - 0       取消静音
 *            - 1       左声道静音，右声道非静音
 *            - 2       右声道静音，左声道非静音
 *            - 3       左右声道均静音
 * @return              函数执行的结果
 *            - 0       设置收音机静音命令发送成功。
 *            - -1      串口还没有成功打开，需要执行android_radio_PowerOnoff来打开串口
 *            - -2      设置收音机静音的命令发送失败。
 *            - -3      传入了无效的参数，请注意参数取值。
 *************************************************************************************
 */
static int android_radio_SetMute(JNIEnv *env, jobject thiz, jint muteState)
{
	int writeSize;
	unsigned int sum = 0;
	unsigned char cmdBuf[10] = {0xFA, 0xFA, 0x0A, 0x00, 0xA1, CMD_RADIO_MUTE};
	debug("android_radio_SetMute(%d)\n", muteState);
	if(fd_radio < 0)
	{
		LOGE("The radio was not be opened.\n");
		return -1;
	}
	if((muteState < 0)||(muteState > 3))
	{
		LOGE("The parameter is invalide.\n");
		return -3;
	}

	cmdBuf[6] = muteState;
	cmdBuf[7] = 0;
	sum = checkSum(cmdBuf, cmdBuf[2]-2);
	cmdBuf[8] = sum & 0xFF;
	cmdBuf[9] = (sum>>8)& 0xFF;
	if((writeSize = write(fd_radio, cmdBuf, cmdBuf[2])) < 0)
	{
		LOGE("write ttyS3 error(%d, %s)\n", errno, strerror(errno));
		return -2;
	}
	return 0;
}

/*!
 *************************************************************************************
 * function: android_radio_GetMute
 *     >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>获取收音机静音的状态，该函数未实现，上层没有使用。
 * @return              函数执行的结果
 *            - 0       非静音
 *            - 1       左声道静音，右声道非静音
 *            - 2       右声道静音，左声道非静音
 *            - 3       左右声道均静音
 *            - -1      串口还没有成功打开，需要执行android_radio_PowerOnoff来打开串口
 *            - -2      设置收音机音量的命令发送失败。
 *************************************************************************************
 */
static int android_radio_GetMute(JNIEnv *env, jobject thiz)
{
	debug("android_radio_GetMute(void)\n");
	if(fd_radio < 0)
	{
		LOGE("The radio was not be opened.\n");
		return -1;
	}
	return 0;
}

/*!
 *************************************************************************************
 * function: android_radio_SetFreq
 *     设置电台频率
 * @param[in] freq 电台频率值
 * @return    返回执行结果
 *            - 0  成功设置电台频率
 *            - -1 串口的文件描述符小于零，串口没有成功打开
 *            - >0 写串口出错。返回串口写数据的字节数
 *************************************************************************************
 */
static int android_radio_SetFreq(JNIEnv *env, jobject thiz, jint freq)
{
	int writeSize;
	unsigned int sum = 0;
	unsigned char cmdBuf[10] = {0xFA, 0xFA, 0x0A, 0x00, 0xA1, CMD_RADIO_FREQ};

	debug("android_radio_SetFreq, freq:%d\n", freq);
    if(fd_radio < 0)
	{
		LOGE("The radio was not be opened.\n");
		return -1;
	}

	if (mtype == 0) {
		if (freq < CHINA_FM_FREQ_MIN)
			freq = CHINA_FM_FREQ_MAX;
	} else {
		if (freq < CHINA_AM_FREQ_MIN)
			freq = CHINA_AM_FREQ_MAX;
	}

	if (mtype == 0) {
		if (freq > CHINA_FM_FREQ_MAX)
			freq = CHINA_FM_FREQ_MIN;
	} else {
		if (freq > CHINA_AM_FREQ_MAX)
			freq = CHINA_AM_FREQ_MIN;
	}


	cmdBuf[6] = freq & 0x0FF;
	cmdBuf[7] = (freq>>8)& 0x0FF;
	sum = checkSum(cmdBuf, cmdBuf[2]-2);
	cmdBuf[8] = sum & 0xFF;
	cmdBuf[9] = (sum>>8)& 0xFF;
	
	if((writeSize = write(fd_radio, cmdBuf, cmdBuf[2])) < 0)
	{
		LOGE("write ttyS3 error(%d, %s)\n", errno, strerror(errno));
		return writeSize;
	}
	return 0;
}

/*!
 *************************************************************************************
 * function: android_radio_FineLeft
 *     向左微调函数将电台频率设置为当前频率-100KHz的频率，比如当前电台频率是88.7MHz, 88700
 * 则下一个台就是88.6MHz。
 * @param[in] freq      当前电台频率值(单位是10KHz)
 * @return              函数执行的结果
 *            - >0      返回微调后的频率值
 *            - -1      串口还没有成功打开，需要执行android_radio_PowerOnoff来打开串口
 *            - -2      设置收音机音量的命令发送失败。
 *************************************************************************************
 */
static int android_radio_FineLeft(JNIEnv *env, jobject thiz, jint freq)
{
	int writeSize;
	unsigned int sum = 0;
	unsigned char cmdBuf[10] = {0xFA, 0xFA, 0x0A, 0x00, 0xA1, CMD_RADIO_FREQ};

	debug("android_radio_FineLeft\n");
    if(fd_radio < 0)
	{
		LOGE("The radio was not be opened.\n");
		return -1;
	}

	freq = freq - step_len;

	if (mtype == 0) {
		if (freq < CHINA_FM_FREQ_MIN)
			freq = CHINA_FM_FREQ_MAX;
	} else {
		if (freq < CHINA_AM_FREQ_MIN)
			freq = CHINA_AM_FREQ_MAX;
	}

	if (mtype == 0) {
		if (freq > CHINA_FM_FREQ_MAX)
			freq = CHINA_FM_FREQ_MIN;
	} else {
		if (freq > CHINA_AM_FREQ_MAX)
			freq = CHINA_AM_FREQ_MIN;
	}


//	if(freq < freq_min)
//		freq = freq_max;
	cmdBuf[6] = freq & 0x0FF;
	cmdBuf[7] = (freq>>8)& 0x0FF;
	sum = checkSum(cmdBuf, cmdBuf[2]-2);
	cmdBuf[8] = sum & 0xFF;
	cmdBuf[9] = (sum>>8)& 0xFF;
	if((writeSize = write(fd_radio, cmdBuf, cmdBuf[2])) < 0)
	{
		LOGE("write ttyS3 error(%d, %s)\n", errno, strerror(errno));
		return -2;
	}
	return freq;
}

/*!
 *************************************************************************************
 * function: android_radio_FineRight
 *     向右微调函数将电台频率设置为当前频率-100KHz的频率，比如当前电台频率是88.7MHz,
 * 则下一个台就是88.8MHz。
 * @param[in] freq      当前电台频率值(单位是10KHz)
 * @return              函数执行的结果
 *            - >0      返回微调后的频率值
 *            - -1      串口还没有成功打开，需要执行android_radio_PowerOnoff来打开串口
 *            - -2      设置收音机音量的命令发送失败。
 *************************************************************************************
 */
static int android_radio_FineRight(JNIEnv *env, jobject thiz, jint freq)
{
	int writeSize;
	unsigned int sum = 0;
	unsigned char cmdBuf[10] = {0xFA, 0xFA, 0x0A, 0x00, 0xA1, CMD_RADIO_FREQ};

	debug("android_radio_FineRight\n");
    if(fd_radio < 0)
	{
		LOGE("The radio was not be opened.\n");
		return -1;
	}

	freq = freq + step_len;

	if (mtype == 0) {
		if (freq > CHINA_FM_FREQ_MAX)
			freq = CHINA_FM_FREQ_MIN;
	} else {
		if (freq > CHINA_AM_FREQ_MAX)
			freq = CHINA_AM_FREQ_MIN;
	}

	if (mtype == 0) {
		if (freq < CHINA_FM_FREQ_MIN)
			freq = CHINA_FM_FREQ_MAX;
	} else {
		if (freq < CHINA_AM_FREQ_MIN)
			freq = CHINA_AM_FREQ_MAX;
	}

//	if(freq > freq_max)
//		freq = freq_min;
	cmdBuf[6] = freq & 0x0FF;
	cmdBuf[7] = (freq>>8)& 0x0FF;
	sum = checkSum(cmdBuf, cmdBuf[2]-2);
	cmdBuf[8] = sum & 0xFF;
	cmdBuf[9] = (sum>>8)& 0xFF;
	if((writeSize = write(fd_radio, cmdBuf, cmdBuf[2])) < 0)
	{
		LOGE("write ttyS3 error(%d, %s)\n", errno, strerror(errno));
		return -2;
	}
	return freq;
}

/*!
 *************************************************************************************
 * function: android_radio_StepLeft
 *     向下搜台,搜索到波段边界后跳到波段的另一个边界继续搜台,搜到一个频道就停止。
 * @param[in] freq      当前电台频率值(单位是10KHz)
 * @return              函数执行的结果
 *            - 0       发送搜台命令成功
 *            - -1      串口还没有成功打开，需要执行android_radio_PowerOnoff来打开串口
 *            - -2      设置收音机音量的命令发送失败。
 *************************************************************************************
 */
static int android_radio_StepLeft(JNIEnv *env, jobject thiz, jint freq)
{
	int writeSize;
	unsigned int sum = 0;
	unsigned char cmdBuf[10] = {0xFA, 0xFA, 0x0A, 0x00, 0xA1, CMD_RADIO_START_SEARCH};

	debug("android_radio_StepLeft, freq:%d\n", freq);
    if((fd_radio < 0)||(fd_bonovo < 0))
	{
		LOGE("The radio was not be opened.\n");
		return -1;
	}

	cmdBuf[6] = 2; // bit[3:0]=0010
	cmdBuf[7] = 0;
	sum = checkSum(cmdBuf, cmdBuf[2]-2);
	cmdBuf[8] = sum & 0xFF;
	cmdBuf[9] = (sum>>8)& 0xFF;
	if((writeSize = write(fd_radio, cmdBuf, cmdBuf[2])) < 0)
	{
		LOGE("write ttyS3 error(%d, %s)\n", errno, strerror(errno));
		return -2;
	}
	ioctl(fd_bonovo, IOCTL_HANDLE_START_RADIO_SEARCH);
	return 0;
}

/*!
 *************************************************************************************
 * function: android_radio_StepRight
 *     向上搜台,搜索到波段边界后跳到波段的另一个边界继续搜台,搜到一个频道就停止。
 * @param[in] freq      当前电台频率值(单位是10KHz)
 * @return              函数执行的结果
 *            - 0       发送搜台命令成功
 *            - -1      串口还没有成功打开，需要执行android_radio_PowerOnoff来打开串口
 *            - -2      设置收音机音量的命令发送失败。
 *************************************************************************************
 */
static int android_radio_StepRight(JNIEnv *env, jobject thiz, jint freq)
{
	int writeSize;
	unsigned int sum = 0;
	unsigned char cmdBuf[10] = {0xFA, 0xFA, 0x0A, 0x00, 0xA1, CMD_RADIO_START_SEARCH};

	debug("android_radio_StepRight(%d)\n", freq);
    if((fd_radio < 0)||(fd_bonovo < 0))
	{
		LOGE("The radio was not be opened.\n");
		return -1;
	}

	cmdBuf[6] = 3; // bit[3:0]=0011
	cmdBuf[7] = 0;
	sum = checkSum(cmdBuf, cmdBuf[2]-2);
	cmdBuf[8] = sum & 0xFF;
	cmdBuf[9] = (sum>>8)& 0xFF;
	if((writeSize = write(fd_radio, cmdBuf, cmdBuf[2])) < 0)
	{
		LOGE("write ttyS3 error(%d, %s)\n", errno, strerror(errno));
		return -2;
	}
	ioctl(fd_bonovo, IOCTL_HANDLE_START_RADIO_SEARCH);
	return 0;
}

/*!
 *************************************************************************************
 * function: android_radio_AutoSeek
 *     从低频率到高频率自动搜台,搜索到波段边界后跳到波段的另一个边界继续搜台。
 * @param[in] freq      freq
 * @return              函数执行的结果
 *            - 0       发送搜台命令成功
 *            - -1      串口还没有成功打开，需要执行android_radio_PowerOnoff来打开串口
 *            - -2      设置收音机音量的命令发送失败。
 *************************************************************************************
 */
static int android_radio_AutoSeek(JNIEnv *env, jobject thiz, jint freq)
{
	int writeSize;
	unsigned int sum = 0;
	unsigned char cmdBuf[10] = {0xFA, 0xFA, 0x0A, 0x00, 0xA1, CMD_RADIO_START_SEARCH};

	debug("android_radio_AutoSeek\n");
    if((fd_radio < 0)||(fd_bonovo < 0))
	{
		LOGE("The radio was not be opened.\n");
		return -1;
	}
	android_radio_SetFreq(env, thiz, freq_min);
	cmdBuf[6] = 7; // bit[3:0]=0111
	cmdBuf[7] = 0;
	sum = checkSum(cmdBuf, cmdBuf[2]-2);
	cmdBuf[8] = sum & 0xFF;
	cmdBuf[9] = (sum>>8)& 0xFF;
	if((writeSize = write(fd_radio, cmdBuf, cmdBuf[2])) < 0)
	{
		LOGE("write ttyS3 error(%d, %s)\n", errno, strerror(errno));
		return -2;
	}
	ioctl(fd_bonovo, IOCTL_HANDLE_START_RADIO_SEARCH);
	return 0;
}

/*!
 *************************************************************************************
 * function: android_radio_StopSearch
 *     停止搜台函数
 * @return    返回执行结果
 *            - 0  成功设置电台频率
 *            - -1 串口的文件描述符小于零，串口没有成功打开
 *            - >0 写串口出错。返回串口写数据的字节数
 *************************************************************************************
 */
static int android_radio_StopSearch(JNIEnv *env, jobject thiz)
{
	int writeSize;
	unsigned int sum = 0;
	unsigned char cmdBuf[10] = {0xFA, 0xFA, 0x0A, 0x00, 0xA1, CMD_RADIO_STOP_SEARCH};

	debug("android_radio_StopSearch\n");
	if((fd_radio < 0)||(fd_bonovo < 0))
	{
		LOGE("The radio was not be opened.\n");
		return -1;
	}

	cmdBuf[6] = 0;
	cmdBuf[7] = 0;
	sum = checkSum(cmdBuf, cmdBuf[2]-2);
	cmdBuf[8] = sum & 0xFF;
	cmdBuf[9] = (sum>>8)& 0xFF;
	if((writeSize = write(fd_radio, cmdBuf, cmdBuf[2])) < 0)
	{
		LOGE("write ttyS3 error(%d, %s)\n", errno, strerror(errno));
		return writeSize;
	}
	ioctl(fd_bonovo, IOCTL_HANDLE_STOP_RADIO_SEARCH);
	return 0;
}

/*!
 *************************************************************************************
 * function: android_radio_ReadSeek
 *     读取正在搜索的电台频率值
 * @param[out] freq[]    存储电台频率的缓冲区，该缓冲区是一个至少包含三个int型的数组。
 *             freq[0]   搜索状态（0：没有搜索  1：正在搜索  2：准备开始搜索）
 *             freq[1]   当前频率值是否是有效的电台
 *             freq[2]   当前频率
 * @param[in]  count     缓冲区数组长度
 * @return               函数执行的结果
 *             - 1       缓冲去为空，无法读取到频率
 *             - 0       读取到一次搜索电台频率
 *             - -1      设备/dev/bonovo_handle还没有打开。
 *             - -2      缓冲区太小，不能读取到完整的信息。
 *************************************************************************************
 */
static int android_radio_ReadSeek(JNIEnv *env, jobject thiz, jintArray freq, int count)
{
	int len, res;
	struct radio_freq temp;
	jint temp_freq[3];

	debug("android_radio_ReadSeek(%d)\n", count);

	//if(count < sizeof(struct radio_freq))
	//{
	//	LOGE("The buffer size you input is too small!\n");
	//	return -2;
	//}
	if(fd_bonovo < 0)
	{
		LOGE("The /dev/bonovo_handle was not be opened.\n");
		return -1;
	}

	len = env->GetArrayLength(freq);
	if(len < 3)
	{
		LOGE("The buffer size you input is too small!\n");
		return -2;
	}
	
	temp_freq[0] = ioctl(fd_bonovo, IOCTL_HANDLE_GET_RADIO_STATUS);
	res = ioctl(fd_bonovo, IOCTL_HANDLE_GET_RADIO_FREQ, &temp);
	if (res == -1)
	{
		debug("The buffer where the frequence in is empty！\n");
		temp_freq[1] = 0;
		temp_freq[2] = 0;
		env->SetIntArrayRegion(freq, 0, 3, temp_freq);
		return 1;
	}
	temp_freq[1] = temp.is_valid;
	temp_freq[2] = temp.freq[0] + (temp.freq[1] << 8);
	env->SetIntArrayRegion(freq, 0, 3, temp_freq);
	return 0;
}

/*!
 *************************************************************************************
 * function: android_radio_TurnFmAm
 *     切换收音机频段的函数
 * @param[in] type 频段编号
 *            - 0  FM
 *            - 1  AM
 *            - 2  SW,短波
 *            - 3  LW,长波
 * @return    返回执行结果
 *            - 0  成功设置电台频率
 *            - -1 串口未成功打开或者传入的参数type无效
 *            - -2 写串口出错。返回串口写数据的字节数
 *************************************************************************************
 */
static int android_radio_TurnFmAm(JNIEnv *env, jobject thiz, jint type)
{
	int writeSize;
	unsigned int sum = 0;
	unsigned char cmdBuf[10] = {0xFA, 0xFA, 0x0A, 0x00, 0xA1, CMD_RADIO_BAND_SELECT};

	mtype = type;
	debug("android_radio_TurnFmAm type='%d'", type);
    if(fd_radio < 0)
	{
		LOGE("The radio was not be opened.\n");
		return -1;
	}

	if((type < 0)||(type > 3))
	{
		LOGE("The type you input is invalid.The type value is in [1,3]\n");
		return -1;
	}

	cmdBuf[6] = type;
	cmdBuf[7] = 0;
	sum = checkSum(cmdBuf, cmdBuf[2]-2);
	cmdBuf[8] = sum & 0xFF;
	cmdBuf[9] = (sum>>8)& 0xFF;
	if((writeSize = write(fd_radio, cmdBuf, cmdBuf[2])) < 0)
	{
		LOGE("write ttyS3 error(%d, %s)\n", errno, strerror(errno));
		return -2;
	}
	debug("android_radio_TurnFmAm Select Band Radio.\n");
	
	cur_band = type;
	debug("android_radio_TurnFmAm cur_model:%d cur_band:%d\n", cur_model, cur_band);
	if(setRangeAndStep(cur_model, type) < 0)
	{
		debug("setRangeAndStep error.");
		return -1;
	}
	return 0;
}

/*!
 *************************************************************************************
 * function: android_radio_Remote
 *     切换收音机搜台灵敏度
 * @param[in] remote      灵敏度  0：本地  1：远程
 * @return              函数执行的结果
 *            - >0      设置成功
 *            - -1      串口还没有成功打开，需要执行android_radio_PowerOnoff来打开串口
 *            - -2      设置切换收音机搜台灵敏度的命令发送失败。
 *************************************************************************************
 */
static int android_radio_Remote(JNIEnv *env, jobject thiz, jint remote)
{
	int writeSize;
	unsigned int sum = 0;
	unsigned char cmdBuf[10] = {0xFA, 0xFA, 0x0A, 0x00, 0xA1, CMD_RADIO_REMOTE};

	debug("android_radio_Remote  remote=%d\n",remote);
    if(fd_radio < 0)
	{
		LOGE("The radio was not be opened.\n");
		return -1;
	}


	cmdBuf[6] = remote;
	cmdBuf[7] = 0;
	sum = checkSum(cmdBuf, cmdBuf[2]-2);
	cmdBuf[8] = sum & 0xFF;
	cmdBuf[9] = (sum>>8)& 0xFF;
	if((writeSize = write(fd_radio, cmdBuf, cmdBuf[2])) < 0)
	{
		LOGE("write ttyS3 error(%d, %s)\n", errno, strerror(errno));
		return -2;
	}
	return 0;
}


/*!
 *************************************************************************************
 * function: android_radio_SetModel
 *     选择制式函数
 * @param[in] type 频段编号
 *            - 0  中国制式
 *            - 1  欧洲制式
 *            - 2  日本制式
 * @return    返回执行结果
 *            - -1 串口未成功打开或者传入的参数type无效
 *            - 0  成功设置电台频率
 *            - >0 写串口出错。返回串口写数据的字节数
 *************************************************************************************
 */
static int android_radio_SetModel(JNIEnv *env, jobject thiz, jint type)
{
	if((type < 0) ||(type > 2))
		return -1;
	cur_model = type;
	if(setRangeAndStep(cur_model, cur_band) < 0)
		debug("setRangeAndStep error.");
	return 0;
}

static const char *classPathName = "com/example/radio/RadioService";
static JNINativeMethod methods[] = {
    //{"jniTurnTda7415", "(I)I", (void *)android_radio_TurnTda7415},
   	{"jniSetVolume", "(I)I", (void *)android_radio_SetVolume},
    {"jniSetMute", "(I)I", (void *)android_radio_SetMute},
    {"jniGetMute", "()I", (void *)android_radio_GetMute},
    {"jniPowerOnoff", "(I)I", (void *)android_radio_PowerOnoff},
    {"jniTurnFmAm", "(I)I", (void *)android_radio_TurnFmAm},
    {"jniSetFreq", "(I)I", (void *)android_radio_SetFreq},
    {"jniFineLeft", "(I)I", (void *)android_radio_FineLeft},
    {"jniFineRight", "(I)I", (void *)android_radio_FineRight},
    {"jniStepLeft", "(I)I", (void *)android_radio_StepLeft},
    {"jniStepRight", "(I)I", (void *)android_radio_StepRight},
    {"jniAutoSeek", "(I)I", (void *)android_radio_AutoSeek},
    {"jniReadSeek", "([II)I", (void *)android_radio_ReadSeek},
	{"jniSetModel", "(I)I", (void *)android_radio_SetModel},
	{"jniSetRemote", "(I)I", (void *)android_radio_Remote},
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
