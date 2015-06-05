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

#define IOCTL_HANDLE_GET_BRIGHTNESS	    _IO(HANDLE_CTRL_DEV_MAJOR, 1)
#define IOCTL_HANDLE_GET_WINCEVOLUME	    _IO(HANDLE_CTRL_DEV_MAJOR, 2)
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

#define RADIO_DEV_NODE              "/dev/ttyS3"    // �豸�ڵ�
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
#define CMD_CODEC_AUDIO_SRC_SELECT  0               // �ⲿģ����Ƶѡ�񿪹�
#define CMD_CODEC_MUX_SRC_SELECT    1               // MUX����Դѡ��
#define CMD_CODEC_MIXER_SRC_SELECT  2               // MIXER����Դѡ��
#define CMD_CODEC_MIXER_DAC_VOLUME  3               // MIXER DAC��������
#define CMD_CODEC_MIXER_ANA_VOLUME  4               // MIXERģ����������
#define CMD_CODEC_LOUT1_VOLUME      5               // LOUT1��������
#define CMD_CODEC_ROUT1_VOLUME      6               // ROUT1��������
#define CMD_CODEC_LOUT2_VOLUME      7               // LOUT2��������
#define CMD_CODEC_ROUT2_VOLUME      8               // ROUT2��������
#define CMD_CODEC_OUTPUT_VOLUME     9               // ���������������ֵ
#define CMD_CODEC_MUTE              10              // ��������
#define CMD_CODEC_STEREO_STRENGTHEN 11              // 3D�������ǿ����

// about radio control
#define CMD_RADIO_STANDARD_MODEL    0               // ��ʽ����
#define CMD_RADIO_BAND_SELECT       1               // Ƶ��ѡ��
#define CMD_RADIO_RDS_ON_OFF        2               // RDS���ؿ���
#define CMD_RADIO_VOLUME            3               // ��������
#define CMD_RADIO_MUTE              4               // ��������
#define CMD_RADIO_FREQ              5               // ��̨Ƶ������
#define CMD_RADIO_START_SEARCH      6               // ��̨����
#define CMD_RADIO_STOP_SEARCH       7               // ֹͣ��̨
#define CMD_RADIO_STEREO_STRENGTHEN 8               // 3D�������ǿ����
#define CMD_RADIO_SHUTDOWN          9               // �ر�������
#define CMD_RADIO_MIN_FREQ          10              // ���ÿ���������СƵ��
#define CMD_RADIO_MAX_FREQ          11              // ���ÿ����������Ƶ��
#define CMD_RADIO_STEP_LEN          12              // ����������̨�Ĳ���
#define CMD_RADIO_REMOTE            13              // Զ�̱����л�

/* about standard model */
// FM
#define CHINA_FM_FREQ_MIN           8700         // �й���ʽFM����СƵ��ֵ����10KHzΪ��λ��
#define CHINA_FM_FREQ_MAX           10800        // �й���ʽFM�����Ƶ��ֵ����10KHzΪ��λ��
#define CHINA_FM_STEP_LENGTH        10           // �й���ʽFM�Ĳ�������10KHzΪ��λ��
#define JNP_FM_FREQ_MIN             7600         // �ձ���ʽFM����СƵ��ֵ����10KHzΪ��λ��
#define JNP_FM_FREQ_MAX             9000         // �ձ���ʽFM�����Ƶ��ֵ����10KHzΪ��λ��
#define JNP_FM_STEP_LENGTH          10           // �ձ���ʽFM�Ĳ�������10KHzΪ��λ��
#define EUROPE_FM_FREQ_MIN          8700         // ŷ����ʽFM����СƵ��ֵ����10KHzΪ��λ��
#define EUROPE_FM_FREQ_MAX          10800        // ŷ����ʽFM�����Ƶ��ֵ����10KHzΪ��λ��
#define EUROPE_FM_STEP_LENGTH       10 //5            // ŷ����ʽFM�Ĳ�������10KHzΪ��λ��
#define ITUREGION1_FM_FREQ_MIN      8700         // �й���ʽFM����СƵ��ֵ����10KHzΪ��λ��
#define ITUREGION1_FM_FREQ_MAX      10800        // �й���ʽFM�����Ƶ��ֵ����10KHzΪ��λ��
#define ITUREGION1_FM_STEP_LENGTH   10           // �й���ʽFM�Ĳ�������10KHzΪ��λ��
#define ITUREGION2_FM_FREQ_MIN      7600         // �ձ���ʽFM����СƵ��ֵ����10KHzΪ��λ��
#define ITUREGION2_FM_FREQ_MAX      9000         // �ձ���ʽFM�����Ƶ��ֵ����10KHzΪ��λ��
#define ITUREGION2_FM_STEP_LENGTH   10           // �ձ���ʽFM�Ĳ�������10KHzΪ��λ��
#define ITUREGION3_FM_FREQ_MIN      8700         // ŷ����ʽFM����СƵ��ֵ����10KHzΪ��λ��
#define ITUREGION3_FM_FREQ_MAX      10800        // ŷ����ʽFM�����Ƶ��ֵ����10KHzΪ��λ��
#define ITUREGION3_FM_STEP_LENGTH   10            // ŷ����ʽFM�Ĳ�������10KHzΪ��λ��

// AM
#define CHINA_AM_FREQ_MIN           531          // �й���ʽAM����СƵ��ֵ����KHzΪ��λ��
#define CHINA_AM_FREQ_MAX           1602         // �й���ʽAM�����Ƶ��ֵ����KHzΪ��λ��
#define CHINA_AM_STEP_LENGTH        9            // �й���ʽAM�Ĳ�������KHzΪ��λ��
#define JNP_AM_FREQ_MIN             522          // �ձ���ʽAM����СƵ��ֵ����KHzΪ��λ��
#define JNP_AM_FREQ_MAX             1620         // �ձ���ʽAM�����Ƶ��ֵ����KHzΪ��λ��
#define JNP_AM_STEP_LENGTH          9            // �ձ���ʽAM�Ĳ�������KHzΪ��λ��
#define EUROPE_AM_FREQ_MIN          522          // ŷ����ʽAM����СƵ��ֵ����KHzΪ��λ��
#define EUROPE_AM_FREQ_MAX          1620         // ŷ����ʽAM�����Ƶ��ֵ����KHzΪ��λ��
#define EUROPE_AM_STEP_LENGTH       9            // ŷ����ʽAM�Ĳ�������KHzΪ��λ��
#define ITUREGION1_AM_FREQ_MIN      531          // �й���ʽAM����СƵ��ֵ����KHzΪ��λ��
#define ITUREGION1_AM_FREQ_MAX      1602         // �й���ʽAM�����Ƶ��ֵ����KHzΪ��λ��
#define ITUREGION1_AM_STEP_LENGTH   9            // �й���ʽAM�Ĳ�������KHzΪ��λ��
#define ITUREGION2_AM_FREQ_MIN      522          // �ձ���ʽAM����СƵ��ֵ����KHzΪ��λ��
#define ITUREGION2_AM_FREQ_MAX      1620         // �ձ���ʽAM�����Ƶ��ֵ����KHzΪ��λ��
#define ITUREGION2_AM_STEP_LENGTH   9            // �ձ���ʽAM�Ĳ�������KHzΪ��λ��
#define ITUREGION3_AM_FREQ_MIN      522          // ŷ����ʽAM����СƵ��ֵ����KHzΪ��λ��
#define ITUREGION3_AM_FREQ_MAX      1620         // ŷ����ʽAM�����Ƶ��ֵ����KHzΪ��λ��
#define ITUREGION3_AM_STEP_LENGTH   9            // ŷ����ʽAM�Ĳ�������KHzΪ��λ��

// standard model selectors
#define MODEL_CHINA                 0            // �й���ʽ
#define MODEL_JNP                   1            // �ձ���ʽ
#define MODEL_EUROPE                2            // ŷ����ʽ
#define MODEL_ITUREGION1            3            // ITU Region 1 Frequencies
#define MODEL_ITUREGION2            4            // ITU Region 2 Frequencies
#define MODEL_ITUREGION3            5            // ITU Region 3 Frequencies

/* band */
#define BAND_FM                     0            // ��Ƶ
#define BAND_AM                     1            // ����
#define BAND_SW                     2            // �̲�
#define BAND_LW                     3            // ����

struct radio_freq
{
	unsigned char freq[2];
	unsigned char is_valid;
};

static int fd_radio = -1;                           // �򿪴���3���ļ�������
static int fd_bonovo= -1;                           // ��ȡ�����ĵ�̨Ƶ�ʵ��ļ�������
static int cur_model= MODEL_CHINA;                  // Ĭ�ϵ���ʽ
static int cur_band = BAND_FM;                      // Ĭ�ϵ�Ƶ��
static int freq_min = CHINA_FM_FREQ_MIN;            // Ĭ�ϵ�FM����������СƵ��ֵ
static int freq_max = CHINA_FM_FREQ_MAX;            // Ĭ�ϵ�FM�����������Ƶ��ֵ
static int step_len = CHINA_FM_STEP_LENGTH;         // Ĭ�ϵ�FM��������
static int mtype;                                   // FM AM�л����

/*!
 *************************************************************************************
 * function: activeAudio
 *     ģ����Ƶ�л�����
 * @param[in] CODEC_Level Ҫ�л���ģ����Ƶ����Դ
 * @return    int         0:���óɹ�  !0:����ʧ��
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
		ALOGE(

"[=== BONOVO ===]%s ioctl is error. ret:%d\n", __FUNCTION__, ret);
	}
	return ret;
}

/*!
 *************************************************************************************
 * function: recoverAudio
 *     �ָ��л���Ƶǰ��ģ������Դ
 * @return    int  0:���óɹ�  !0:����ʧ��
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
		ALOGE(

"[=== BONOVO ===]%s ioctl is error. ret:%d\n", __FUNCTION__, ret);
	}
	return ret;
}

/*!
 *************************************************************************************
 * function: checkSum
 *     ����У��ͺ���
 * @param[in] cmdBuf Ҫ����У��͵���ݴ�ŵĻ�����ָ��
 * @param[in] size   ����������Ч��ݵ��ֽ���
 * @return           ���������У���
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
 *    ��������Ƶ�ʷ�Χ����������
 * @param[in] mode    ѡ�����ʽ
 * @param[in] band    ѡ���Ƶ��
 * @return            ����ִ�н��
 *            - 0     ִ�гɹ�
 *            - -1    ��Ч�Ĳ�����ߴ��ڻ�δ���
 *            - -2    ���ڷ�������ʧ��
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
	LOGE("setRangeAndStep cur_model =%d\n",cur_model);
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
		case MODEL_ITUREGION1:
			freq_min = ITUREGION1_FM_FREQ_MIN;
			freq_max = ITUREGION1_FM_FREQ_MAX;
			step_len = ITUREGION1_FM_STEP_LENGTH;
			break;
		case MODEL_ITUREGION2:
			freq_min = ITUREGION2_FM_FREQ_MIN;
			freq_max = ITUREGION2_FM_FREQ_MAX;
			step_len = ITUREGION2_FM_STEP_LENGTH;
			break;
		case MODEL_ITUREGION3:
			freq_min = ITUREGION3_FM_FREQ_MIN;
			freq_max = ITUREGION3_FM_FREQ_MAX;
			step_len = ITUREGION3_FM_STEP_LENGTH;
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
		case MODEL_ITUREGION1:
			freq_min = ITUREGION1_AM_FREQ_MIN;
			freq_max = ITUREGION1_AM_FREQ_MAX;
			step_len = ITUREGION1_AM_STEP_LENGTH;
			break;
		case MODEL_ITUREGION2:
			freq_min = ITUREGION2_AM_FREQ_MIN;
			freq_max = ITUREGION2_AM_FREQ_MAX;
			step_len = ITUREGION2_AM_STEP_LENGTH;
			break;
		case MODEL_ITUREGION3:
			freq_min = ITUREGION3_AM_FREQ_MIN;
			freq_max = ITUREGION3_AM_FREQ_MAX;
			step_len = ITUREGION3_AM_STEP_LENGTH;
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
 *     ����radio������Ҫ�����Ǵ򿪴��ڲ������������Ƶ��freq��
 *
 * @param[in] OnOff  ��������ģ��Ĳ���
 *            - !1   �ر�������
 *            - 1    ��������
 * @return           ִ�н��
 *            - 0    ����ִ�гɹ�
 *            - -1   ��������ʱ�����ظ�ֵ˵���򿪴���ʧ�ܣ��ر�������ʱ�����ظ�ֵ˵������֮ǰû�гɹ���
 *            - -2   ���ظ�ֵ˵��д����ʧ�ܣ�
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
 *     ��������������
 * @param[in] volume ��������������ٷְ٣�����Ҫ������������10%����volume=10��
 * @return           ����ִ�еĽ��
 *            - 0    ������������������ͳɹ���
 *            - -1   ���ڻ�û�гɹ��򿪣���Ҫִ��android_radio_PowerOnoff���򿪴���
 *            - -2   ���������������������ʧ�ܡ�
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
 *     �򿪻��߹ر�������������
 * @param[in] muteState ���þ����Ĳ���
 *            - 0       ȡ����
 *            - 1       �����������������Ǿ���
 *            - 2       �����������������Ǿ���
 *            - 3       �����������
 * @return              ����ִ�еĽ��
 *            - 0       ����������������ͳɹ���
 *            - -1      ���ڻ�û�гɹ��򿪣���Ҫִ��android_radio_PowerOnoff���򿪴���
 *            - -2      �������������������ʧ�ܡ�
 *            - -3      ��������Ч�Ĳ�����ע�����ȡֵ��
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
 *     >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>��ȡ����������״̬���ú���δʵ�֣��ϲ�û��ʹ�á�
 * @return              ����ִ�еĽ��
 *            - 0       �Ǿ���
 *            - 1       �����������������Ǿ���
 *            - 2       �����������������Ǿ���
 *            - 3       �����������
 *            - -1      ���ڻ�û�гɹ��򿪣���Ҫִ��android_radio_PowerOnoff���򿪴���
 *            - -2      ���������������������ʧ�ܡ�
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
 *     ���õ�̨Ƶ��
 * @param[in] freq ��̨Ƶ��ֵ
 * @return    ����ִ�н��
 *            - 0  �ɹ����õ�̨Ƶ��
 *            - -1 ���ڵ��ļ�������С���㣬����û�гɹ���
 *            - >0 д���ڳ��?���ش���д��ݵ��ֽ���
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
		if (cur_model == MODEL_CHINA) {
			if (freq < CHINA_FM_FREQ_MIN)
				freq = CHINA_FM_FREQ_MAX;
		} else if (cur_model == MODEL_JNP) {
			if (freq < JNP_FM_FREQ_MIN)
				freq = JNP_FM_FREQ_MAX;
		} else if (cur_model == MODEL_EUROPE) {
			if (freq < EUROPE_FM_FREQ_MIN)
				freq = EUROPE_FM_FREQ_MAX;
		} else if (cur_model == MODEL_ITUREGION1) {
			if (freq < ITUREGION1_FM_FREQ_MIN)
				freq = ITUREGION1_FM_FREQ_MAX;
		} else if (cur_model == MODEL_ITUREGION2) {
			if (freq < ITUREGION2_FM_FREQ_MIN)
				freq = ITUREGION2_FM_FREQ_MAX;
		} else if (cur_model == MODEL_ITUREGION3) {
			if (freq < ITUREGION3_FM_FREQ_MIN)
				freq = ITUREGION3_FM_FREQ_MAX;
		}

	} else {
		if (cur_model == MODEL_CHINA) {
			if (freq < CHINA_AM_FREQ_MIN)
				freq = CHINA_AM_FREQ_MAX;
		} else if (cur_model == MODEL_JNP) {
			if (freq < JNP_AM_FREQ_MIN)
				freq = JNP_AM_FREQ_MAX;
		} else if (cur_model == MODEL_EUROPE) {
			if (freq < EUROPE_AM_FREQ_MIN)
				freq = EUROPE_AM_FREQ_MAX;
		} else if (cur_model == MODEL_ITUREGION1) {
			if (freq < ITUREGION1_AM_FREQ_MIN)
				freq = ITUREGION1_AM_FREQ_MAX;
		} else if (cur_model == MODEL_ITUREGION2) {
			if (freq < ITUREGION2_AM_FREQ_MIN)
				freq = ITUREGION2_AM_FREQ_MAX;
		} else if (cur_model == MODEL_ITUREGION3) {
			if (freq < ITUREGION3_AM_FREQ_MIN)
				freq = ITUREGION3_AM_FREQ_MAX;
		}

	}

	if (mtype == 0) {
		if (cur_model == MODEL_CHINA) {
			if (freq > CHINA_FM_FREQ_MAX)
				freq = CHINA_FM_FREQ_MIN;
		} else if (cur_model == MODEL_JNP) {
			if (freq > JNP_FM_FREQ_MAX)
				freq = JNP_FM_FREQ_MIN;
		} else if (cur_model == MODEL_EUROPE) {
			if (freq > EUROPE_FM_FREQ_MAX)
				freq = EUROPE_FM_FREQ_MIN;
		} else if (cur_model == MODEL_ITUREGION1) {
			if (freq > ITUREGION1_FM_FREQ_MAX)
				freq = ITUREGION1_FM_FREQ_MIN;
		} else if (cur_model == MODEL_ITUREGION2) {
			if (freq > ITUREGION2_FM_FREQ_MAX)
				freq = ITUREGION2_FM_FREQ_MIN;
		} else if (cur_model == MODEL_ITUREGION3) {
			if (freq > ITUREGION3_FM_FREQ_MAX)
				freq = ITUREGION3_FM_FREQ_MIN;
		}

	} else {
		if(cur_model == MODEL_CHINA){
			if (freq > CHINA_AM_FREQ_MAX)
				freq = CHINA_AM_FREQ_MIN;
		}else if(cur_model == MODEL_JNP){
			if (freq > JNP_AM_FREQ_MAX)
				freq = JNP_AM_FREQ_MIN;
		}else if(cur_model == MODEL_EUROPE){
			if (freq > EUROPE_AM_FREQ_MAX)
				freq = EUROPE_AM_FREQ_MIN;
		}else if(cur_model == MODEL_ITUREGION1){
			if (freq > ITUREGION1_AM_FREQ_MAX)
				freq = ITUREGION1_AM_FREQ_MIN;
		}else if(cur_model == MODEL_ITUREGION2){
			if (freq > ITUREGION2_AM_FREQ_MAX)
				freq = ITUREGION2_AM_FREQ_MIN;
		}else if(cur_model == MODEL_ITUREGION3){
			if (freq > ITUREGION3_AM_FREQ_MAX)
				freq = ITUREGION3_AM_FREQ_MIN;
		}

	}


	cmdBuf[6] = freq & 0x0FF;
	cmdBuf[7] = (freq>>8)& 0x0FF;
	sum = checkSum(cmdBuf, cmdBuf[2]-2);
	cmdBuf[8] = sum & 0xFF;
	cmdBuf[9] = (sum>>8)& 0xFF;
	
//	int i = 0;
//	for( ; i<10; i++){
//		debug("setFreq-->cmdBuf[%d]=0x%2x",i,cmdBuf[i]);
//	}
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
 *     ����΢�������̨Ƶ������Ϊ��ǰƵ��-100KHz��Ƶ�ʣ����統ǰ��̨Ƶ����88.7MHz, 88700
 * ����һ��̨����88.6MHz��
 * @param[in] freq      ��ǰ��̨Ƶ��ֵ(��λ��10KHz)
 * @return              ����ִ�еĽ��
 *            - >0      ����΢�����Ƶ��ֵ
 *            - -1      ���ڻ�û�гɹ��򿪣���Ҫִ��android_radio_PowerOnoff���򿪴���
 *            - -2      ���������������������ʧ�ܡ�
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
		if (cur_model == MODEL_CHINA) {
			if (freq < CHINA_FM_FREQ_MIN)
				freq = CHINA_FM_FREQ_MAX;
		} else if (cur_model == MODEL_JNP) {
			if (freq < JNP_FM_FREQ_MIN)
				freq = JNP_FM_FREQ_MAX;
		} else if (cur_model == MODEL_EUROPE) {
			if (freq < EUROPE_FM_FREQ_MIN)
				freq = EUROPE_FM_FREQ_MAX;
		} else if (cur_model == MODEL_ITUREGION1) {
			if (freq < ITUREGION1_FM_FREQ_MIN)
				freq = ITUREGION1_FM_FREQ_MAX;
		} else if (cur_model == MODEL_ITUREGION2) {
			if (freq < ITUREGION2_FM_FREQ_MIN)
				freq = ITUREGION2_FM_FREQ_MAX;
		} else if (cur_model == MODEL_ITUREGION3) {
			if (freq < ITUREGION3_FM_FREQ_MIN)
				freq = ITUREGION3_FM_FREQ_MAX;
		}

	} else {
		if (cur_model == MODEL_CHINA) {
			if (freq < CHINA_AM_FREQ_MIN)
				freq = CHINA_AM_FREQ_MAX;
		} else if (cur_model == MODEL_JNP) {
			if (freq < JNP_AM_FREQ_MIN)
				freq = JNP_AM_FREQ_MAX;
		} else if (cur_model == MODEL_EUROPE) {
			if (freq < EUROPE_AM_FREQ_MIN)
				freq = EUROPE_AM_FREQ_MAX;
		} else if (cur_model == MODEL_ITUREGION1) {
			if (freq < ITUREGION1_AM_FREQ_MIN)
				freq = ITUREGION1_AM_FREQ_MAX;
		} else if (cur_model == MODEL_ITUREGION2) {
			if (freq < ITUREGION2_AM_FREQ_MIN)
				freq = ITUREGION2_AM_FREQ_MAX;
		} else if (cur_model == MODEL_ITUREGION3) {
			if (freq < ITUREGION3_AM_FREQ_MIN)
				freq = ITUREGION3_AM_FREQ_MAX;
		}
	}

	if (mtype == 0) {
		if (cur_model == MODEL_CHINA) {
			if (freq > CHINA_FM_FREQ_MAX)
				freq = CHINA_FM_FREQ_MIN;
		} else if (cur_model == MODEL_JNP) {
			if (freq > JNP_FM_FREQ_MAX)
				freq = JNP_FM_FREQ_MIN;
		} else if (cur_model == MODEL_EUROPE) {
			if (freq > EUROPE_FM_FREQ_MAX)
				freq = EUROPE_FM_FREQ_MIN;
		} else if (cur_model == MODEL_ITUREGION1) {
			if (freq > ITUREGION1_FM_FREQ_MAX)
				freq = ITUREGION1_FM_FREQ_MIN;
		} else if (cur_model == MODEL_ITUREGION2) {
			if (freq > ITUREGION2_FM_FREQ_MAX)
				freq = ITUREGION2_FM_FREQ_MIN;
		} else if (cur_model == MODEL_ITUREGION3) {
			if (freq > ITUREGION3_FM_FREQ_MAX)
				freq = ITUREGION3_FM_FREQ_MIN;
		}

	} else {
		if (cur_model == MODEL_CHINA) {
			if (freq > CHINA_AM_FREQ_MAX)
				freq = CHINA_AM_FREQ_MIN;
		} else if (cur_model == MODEL_JNP) {
			if (freq > JNP_AM_FREQ_MAX)
				freq = JNP_AM_FREQ_MIN;
		} else if (cur_model == MODEL_EUROPE) {
			if (freq > EUROPE_AM_FREQ_MAX)
				freq = EUROPE_AM_FREQ_MIN;
		} else if (cur_model == MODEL_ITUREGION1) {
			if (freq > ITUREGION1_AM_FREQ_MAX)
				freq = ITUREGION1_AM_FREQ_MIN;
		} else if (cur_model == MODEL_ITUREGION2) {
			if (freq > ITUREGION2_AM_FREQ_MAX)
				freq = ITUREGION2_AM_FREQ_MIN;
		} else if (cur_model == MODEL_ITUREGION3) {
			if (freq > ITUREGION3_AM_FREQ_MAX)
				freq = ITUREGION3_AM_FREQ_MIN;
		}
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
 *     ����΢�������̨Ƶ������Ϊ��ǰƵ��-100KHz��Ƶ�ʣ����統ǰ��̨Ƶ����88.7MHz,
 * ����һ��̨����88.8MHz��
 * @param[in] freq      ��ǰ��̨Ƶ��ֵ(��λ��10KHz)
 * @return              ����ִ�еĽ��
 *            - >0      ����΢�����Ƶ��ֵ
 *            - -1      ���ڻ�û�гɹ��򿪣���Ҫִ��android_radio_PowerOnoff���򿪴���
 *            - -2      ���������������������ʧ�ܡ�
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
		if (cur_model == MODEL_CHINA) {
			if (freq > CHINA_FM_FREQ_MAX)
				freq = CHINA_FM_FREQ_MIN;
		} else if (cur_model == MODEL_JNP) {
			if (freq > JNP_FM_FREQ_MAX)
				freq = JNP_FM_FREQ_MIN;
		} else if (cur_model == MODEL_EUROPE) {
			if (freq > EUROPE_FM_FREQ_MAX)
				freq = EUROPE_FM_FREQ_MIN;
		} else if (cur_model == MODEL_ITUREGION1) {
			if (freq > ITUREGION1_FM_FREQ_MAX)
				freq = ITUREGION1_FM_FREQ_MIN;
		} else if (cur_model == MODEL_ITUREGION2) {
			if (freq > ITUREGION2_FM_FREQ_MAX)
				freq = ITUREGION2_FM_FREQ_MIN;
		} else if (cur_model == MODEL_ITUREGION3) {
			if (freq > ITUREGION3_FM_FREQ_MAX)
				freq = ITUREGION3_FM_FREQ_MIN;
		}
	} else {
		if (cur_model == MODEL_CHINA) {
			if (freq > CHINA_AM_FREQ_MAX)
				freq = CHINA_AM_FREQ_MIN;
		} else if (cur_model == MODEL_JNP) {
			if (freq > JNP_AM_FREQ_MAX)
				freq = JNP_AM_FREQ_MIN;
		} else if (cur_model == MODEL_EUROPE) {
			if (freq > EUROPE_AM_FREQ_MAX)
				freq = EUROPE_AM_FREQ_MIN;
		} else if (cur_model == MODEL_ITUREGION1) {
			if (freq > ITUREGION1_AM_FREQ_MAX)
				freq = ITUREGION1_AM_FREQ_MIN;
		} else if (cur_model == MODEL_ITUREGION2) {
			if (freq > ITUREGION2_AM_FREQ_MAX)
				freq = ITUREGION2_AM_FREQ_MIN;
		} else if (cur_model == MODEL_ITUREGION3) {
			if (freq > ITUREGION3_AM_FREQ_MAX)
				freq = ITUREGION3_AM_FREQ_MIN;
		}

	}

	if (mtype == 0) {
		if (cur_model == MODEL_CHINA) {
			if (freq < CHINA_FM_FREQ_MIN)
				freq = CHINA_FM_FREQ_MAX;
		} else if (cur_model == MODEL_JNP) {
			if (freq < JNP_FM_FREQ_MIN)
				freq = JNP_FM_FREQ_MAX;
		} else if (cur_model == MODEL_EUROPE) {
			if (freq < EUROPE_FM_FREQ_MIN)
				freq = EUROPE_FM_FREQ_MAX;
		} else if (cur_model == MODEL_ITUREGION1) {
			if (freq < ITUREGION1_FM_FREQ_MIN)
				freq = ITUREGION1_FM_FREQ_MAX;
		} else if (cur_model == MODEL_ITUREGION2) {
			if (freq < ITUREGION2_FM_FREQ_MIN)
				freq = ITUREGION2_FM_FREQ_MAX;
		} else if (cur_model == MODEL_ITUREGION3) {
			if (freq < ITUREGION3_FM_FREQ_MIN)
				freq = ITUREGION3_FM_FREQ_MAX;
		}
	} else {
		if (cur_model == MODEL_CHINA) {
			if (freq < CHINA_AM_FREQ_MIN)
				freq = CHINA_AM_FREQ_MAX;
		} else if (cur_model == MODEL_JNP) {
			if (freq < JNP_AM_FREQ_MIN)
				freq = JNP_AM_FREQ_MAX;
		} else if (cur_model == MODEL_EUROPE) {
			if (freq < EUROPE_AM_FREQ_MIN)
				freq = EUROPE_AM_FREQ_MAX;
		} else if (cur_model == MODEL_ITUREGION1) {
			if (freq < ITUREGION1_AM_FREQ_MIN)
				freq = ITUREGION1_AM_FREQ_MAX;
		} else if (cur_model == MODEL_ITUREGION2) {
			if (freq < ITUREGION2_AM_FREQ_MIN)
				freq = ITUREGION2_AM_FREQ_MAX;
		} else if (cur_model == MODEL_ITUREGION3) {
			if (freq < ITUREGION3_AM_FREQ_MIN)
				freq = ITUREGION3_AM_FREQ_MAX;
		}

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
 *     ������̨,���������α߽�����ε���һ���߽������̨,�ѵ�һ��Ƶ����ֹͣ��
 * @param[in] freq      ��ǰ��̨Ƶ��ֵ(��λ��10KHz)
 * @return              ����ִ�еĽ��
 *            - 0       ������̨����ɹ�
 *            - -1      ���ڻ�û�гɹ��򿪣���Ҫִ��android_radio_PowerOnoff���򿪴���
 *            - -2      ���������������������ʧ�ܡ�
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
 *     ������̨,���������α߽�����ε���һ���߽������̨,�ѵ�һ��Ƶ����ֹͣ��
 * @param[in] freq      ��ǰ��̨Ƶ��ֵ(��λ��10KHz)
 * @return              ����ִ�еĽ��
 *            - 0       ������̨����ɹ�
 *            - -1      ���ڻ�û�гɹ��򿪣���Ҫִ��android_radio_PowerOnoff���򿪴���
 *            - -2      ���������������������ʧ�ܡ�
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
 *     �ӵ�Ƶ�ʵ���Ƶ���Զ���̨,���������α߽�����ε���һ���߽������̨��
 * @param[in] freq      freq
 * @return              ����ִ�еĽ��
 *            - 0       ������̨����ɹ�
 *            - -1      ���ڻ�û�гɹ��򿪣���Ҫִ��android_radio_PowerOnoff���򿪴���
 *            - -2      ���������������������ʧ�ܡ�
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
 *     ֹͣ��̨����
 * @return    ����ִ�н��
 *            - 0  �ɹ����õ�̨Ƶ��
 *            - -1 ���ڵ��ļ�������С���㣬����û�гɹ���
 *            - >0 д���ڳ��?���ش���д��ݵ��ֽ���
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
 *     ��ȡ���������ĵ�̨Ƶ��ֵ
 * @param[out] freq[]    �洢��̨Ƶ�ʵĻ�����û�������һ�����ٰ����int�͵����顣
 *             freq[0]   ����״̬��0��û������  1����������  2��׼����ʼ������
 *             freq[1]   ��ǰƵ��ֵ�Ƿ�����Ч�ĵ�̨
 *             freq[2]   ��ǰƵ��
 * @param[in]  count     ���������鳤��
 * @return               ����ִ�еĽ��
 *             - 1       ����ȥΪ�գ��޷���ȡ��Ƶ��
 *             - 0       ��ȡ��һ��������̨Ƶ��
 *             - -1      �豸/dev/bonovo_handle��û�д򿪡�
 *             - -2      ������̫С�����ܶ�ȡ���������Ϣ��
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
		debug("The buffer where the frequence in is empty��\n");
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
 *     �л�������Ƶ�εĺ���
 * @param[in] type Ƶ�α��
 *            - 0  FM
 *            - 1  AM
 *            - 2  SW,�̲�
 *            - 3  LW,����
 * @return    ����ִ�н��
 *            - 0  �ɹ����õ�̨Ƶ��
 *            - -1 ����δ�ɹ��򿪻��ߴ���Ĳ���type��Ч
 *            - -2 д���ڳ��?���ش���д��ݵ��ֽ���
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
 *     �л���������̨������
 * @param[in] remote      ������  0������  1��Զ��
 * @return              ����ִ�еĽ��
 *            - >0      ���óɹ�
 *            - -1      ���ڻ�û�гɹ��򿪣���Ҫִ��android_radio_PowerOnoff���򿪴���
 *            - -2      �����л���������̨�����ȵ������ʧ�ܡ�
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
 *     ѡ����ʽ����
 * @param[in] type Ƶ�α��
 *            - 0  �й���ʽ
 *            - 1  ŷ����ʽ
 *            - 2  �ձ���ʽ
 * @return    ����ִ�н��
 *            - -1 ����δ�ɹ��򿪻��ߴ���Ĳ���type��Ч
 *            - 0  �ɹ����õ�̨Ƶ��
 *            - >0 д���ڳ��?���ش���д��ݵ��ֽ���
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
