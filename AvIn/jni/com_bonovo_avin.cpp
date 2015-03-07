#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/ioctl.h>
#include <fcntl.h>
#include <errno.h>
#include <string.h>
#include <stdlib.h>

#include "jni.h"
#include "JNIHelp.h"
#include "android_runtime/AndroidRuntime.h"

#define SEND_DEV_NODE    "/dev/ttyS3"         // 发送命令设备节点
#define AUDIO_CTRL_NODE  "/dev/bonovo_handle" // 控制音频设备节点
#define LOG_TAG    "com_bonovo_avin"
#define HANDLE_CTRL_DEV_MAJOR		230
#define HANDLE_CTRL_DEV_MINOR		0

// codec
#define IOCTL_HANDLE_CODEC_SWITCH_SRC       _IO(HANDLE_CTRL_DEV_MAJOR, 30)
#define IOCTL_HANDLE_CODEC_RECOVER_SRC      _IO(HANDLE_CTRL_DEV_MAJOR, 31)
#define IOCTL_HANDLE_CODEC_INIT             _IO(HANDLE_CTRL_DEV_MAJOR, 32)
#define IOCTL_HANDLE_CODEC_GET_CURRENT_SRC  _IO(HANDLE_CTRL_DEV_MAJOR, 33)

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

int fdTtyS3 = -1;
int fdAudio = -1;

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
 * function: getAudioLevel
 *     获取当前模拟音频的输入源
 * @return int 当前模拟音频的输入源
 */
int getCurrentAudioLevel()
{
	int ret = 0;
	CODEC_Level codec_mode;
	if (fdAudio < 0) {
		return -1;
	}
	ret = ioctl(fdAudio,IOCTL_HANDLE_CODEC_GET_CURRENT_SRC, &codec_mode);
	if(ret != 0){
		ret = -1;
	}else{
		ret = (int)codec_mode;
	}
	return ret;
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
	if (fdAudio < 0) {
        return -1;
    }
	ret = ioctl(fdAudio,IOCTL_HANDLE_CODEC_SWITCH_SRC, codec_mode);
	if(ret){
		ALOGE("[=== BONOVO ===]%s ioctl is error. ret:%d\n", __FUNCTION__, ret);
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

	if (fdAudio < 0) {
        return -1;
    }
	ret = ioctl(fdAudio, IOCTL_HANDLE_CODEC_RECOVER_SRC, codec_mode);
	if(ret){
		ALOGE("[=== BONOVO ===]%s ioctl is error. ret:%d\n", __FUNCTION__, ret);
	}
	return ret;
}

//////////////////////////////////////////////////////////////////////////////////////

static jboolean android_avin_openSerial(JNIEnv *env, jobject thiz){
	fdTtyS3 = open(SEND_DEV_NODE, O_RDWR|O_NOCTTY|O_NONBLOCK);
	if(fdTtyS3 < 0){
		ALOGE("open %s failed,fdTtyS3=%d(%d,%s)\n", SEND_DEV_NODE, fdTtyS3, errno, strerror(errno));
		return JNI_FALSE;
	}

	fdAudio = open(AUDIO_CTRL_NODE, O_RDWR|O_NOCTTY|O_NONBLOCK);
	if(fdAudio < 0) {
		ALOGE("open %s failed,fdAudio=%d(%d,%s)\n", AUDIO_CTRL_NODE, fdAudio, errno, strerror(errno));
		close(fdTtyS3);
		fdTtyS3 = -1;
		return JNI_FALSE;
	}

	return JNI_TRUE;
}

static void android_avin_closeSerial(JNIEnv *env, jobject thiz){
	if(fdTtyS3 > 0){
		close(fdTtyS3);
		fdTtyS3 = -1;
	}

	if(fdAudio > 0) {
		close(fdAudio);
		fdAudio = -1;
	}
	return;
}

static jboolean android_avin_switch(JNIEnv *env, jobject thiz, jboolean offOn) {
	unsigned char videoCmd[10] = {0xFA, 0xFA, 0x0A, 0x00, 0x83, 0x03, 0x00, 0x00};
	unsigned int sum = 0;
	unsigned int wLen = 0;

	if((fdTtyS3 < 0)||(fdAudio < 0)){
		ALOGE("fdTtyS3 or fdAudio is invalid. fdTtyS3:%d, fdAudio:%d", fdTtyS3, fdAudio);
		return JNI_FALSE;
	}

//	if(getCurrentAudioLevel() == CODEC_LEVEL_BT_TEL){
//		ALOGE("current audio source is bluetooth telephone. You can't switch audio source.");
//		return JNI_FALSE;
//	}

	if(JNI_TRUE == offOn){
		videoCmd[6] = 0x01;
	}else{
		videoCmd[6] = 0x00;
	}
	sum = checkSum(videoCmd, 8);
	videoCmd[8] = sum & 0x00FF;
	videoCmd[9] = (sum >> 8) & 0x00FF;
	if((wLen = write(fdTtyS3, videoCmd, 10)) != 10){
		ALOGE("write %s failed,fdAudio=%d. writeLen:%d\n", SEND_DEV_NODE, fdTtyS3, wLen);
		return JNI_FALSE;
	}else{
		if(JNI_TRUE == offOn){
			activeAudio(CODEC_LEVEL_AV_IN);
		}else{
			recoverAudio(CODEC_LEVEL_AV_IN);
		}
	}

	return JNI_TRUE;
}

static const char *classPathName = "com/bonovo/avin/AvInActivity";

static JNINativeMethod sMethods[] = {
	{"jniopenserial","()Z", (void *)android_avin_openSerial},
	{"jniAvInSwitch", "(Z)Z", (void *)android_avin_switch},
	{"jnicloseserial", "()V", (void *)android_avin_closeSerial},
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
        ALOGE("Native registration unable to find class '%s'", className);
        return JNI_FALSE;
    }

    if (env->RegisterNatives(clazz, methods, numMethods) < 0) {
        ALOGE("RegisterNatives failed for '%s'", className);
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
          sMethods, sizeof(sMethods) / sizeof(sMethods[0]))) {
    return JNI_FALSE;
  }

  return JNI_TRUE;
}

// ----------------------------------------------------------------------------

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

    ALOGI("JNI_OnLoad");

    if (vm->GetEnv(&uenv.venv, JNI_VERSION_1_6) != JNI_OK) {
        ALOGE("ERROR: GetEnv failed");
        goto bail;
    }
    env = uenv.env;

    if (registerNatives(env) != JNI_TRUE) {
        ALOGE("ERROR: registerNatives failed");
        goto bail;
    }

    result = JNI_VERSION_1_6;

bail:
    return result;
}

