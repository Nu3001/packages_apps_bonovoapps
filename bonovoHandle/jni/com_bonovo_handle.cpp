#define LOG_TAG "com_bonovo_handle.cpp"

#include <asm/ioctl.h>

#include <assert.h>
#include <limits.h>
#include <utils/threads.h>
#include <main.h>


#include "jni.h"
#include "JNIHelp.h"
#include "android_runtime/AndroidRuntime.h"

#define HANDLE_CTRL_DEV_MAJOR		230
#define HANDLE_CTRL_DEV_MINOR		0

#define COMMAND_SET_LOUT1_VOL       0x05
#define COMMAND_SET_ROUT1_VOL       0x06
#define COMMAND_SET_LOUT2_VOL       0x07
#define COMMAND_SET_ROUT2_VOL       0x08

#define IOCTL_HANDLE_GET_BRIGHTNESS		    _IO(HANDLE_CTRL_DEV_MAJOR, 1)
// power
#define IOCTL_HANDLE_SEND_POWER_KEY       _IO(HANDLE_CTRL_DEV_MAJOR, 11)

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

static jboolean android_handle_sendPowerKey(JNIEnv *env, jobject thiz){
    int result = -1;
    int fdPower = open("/dev/bonovo_handle", O_RDWR|O_NOCTTY|O_NONBLOCK);
	
	if(fdPower < 0){
		ALOGE("fdTtyS3 or fdPower is invalid. fdPower:%d", fdPower);
		return JNI_FALSE;
	}

	if((result = ioctl(fdPower, IOCTL_HANDLE_SEND_POWER_KEY)) < 0){
		ALOGE("ioctl SEND POWER KEY failed. result:%d", result);
		close(fdPower);
		return JNI_FALSE;
	}
    close(fdPower);
	return JNI_TRUE;
}

static jboolean android_power_wakeUp(JNIEnv *env, jobject thiz) {
	unsigned char cmd[8] = {0xFA, 0xFA, 0x08, 0x00, 0x95, 0x01, 0x00, 0x00};
	unsigned int sum = 0;
	unsigned int cmdLen = 0;

	int fdTtyS3 = open("/dev/ttyS3", O_RDWR|O_NOCTTY|O_NONBLOCK);
	if(fdTtyS3 < 0){
		ALOGE("fdTtyS3 is invalid. fdTtyS3:%d", fdTtyS3);
		return JNI_FALSE;
	}

	sum = checkSum(cmd, 6);
	cmd[6] = sum & 0x00FF;
	cmd[7] = (sum >> 8) & 0x00FF;
	if((cmdLen = write(fdTtyS3, cmd, 8)) != 8){
		ALOGE("write /dev/ttyS3 failed,fdPower=%d. writeLen:%d\n", fdTtyS3, cmdLen);
		close(fdTtyS3);
		return JNI_FALSE;
	}
	close(fdTtyS3);

	return JNI_TRUE;
}

static jboolean android_power_goToSleep(JNIEnv *env, jobject thiz) {
	unsigned char cmd[8] = {0xFA, 0xFA, 0x08, 0x00, 0x95, 0x06, 0x00, 0x00};
	unsigned int sum = 0;
	unsigned int len = 0;

	int fdTtyS3 = open("/dev/ttyS3", O_RDWR|O_NOCTTY|O_NONBLOCK);
	if(fdTtyS3 < 0){
		ALOGE("fdTtyS3 is invalid. fdTtyS3:%d", fdTtyS3);
		return JNI_FALSE;
	}

	sum = checkSum(cmd, 6);
	cmd[6] = sum & 0x00FF;
	cmd[7] = (sum >> 8) & 0x00FF;
	if((len = write(fdTtyS3, cmd, 8)) != 8){
		close(fdTtyS3);
		ALOGE("write /dev/ttyS3 failed,fdPower=%d. writeLen:%d\n", fdTtyS3, len);
		return JNI_FALSE;
	}
	close(fdTtyS3);

	return JNI_TRUE;
}

//by dm
static int android_handle_systeminit(JNIEnv *env, jobject thiz)
{
	int ttyS3_fd = -1;
	int dwByteWrite;
	unsigned char StartComplete[] = {0xFA, 0xFA, 0x07, 0x00, 0x80, 0x7B, 0x02};
	unsigned char gpsClose[] = {0xFA, 0xFA, 0x07, 0x00, 0x93, 0x8E, 0x02};

	if((ttyS3_fd = open("/dev/ttyS3", O_RDWR|O_NOCTTY|O_NONBLOCK)) < 0) {
		LOGI("======bonovo dev/ttyS3 SystemInit open result = %d\n",ttyS3_fd);
		return -1;
	} else {
		dwByteWrite = write(ttyS3_fd, StartComplete, sizeof(StartComplete));
		LOGE("==============%d==============\n",dwByteWrite);
		dwByteWrite = write(ttyS3_fd, gpsClose, sizeof(gpsClose));
		close(ttyS3_fd);
		return 0;
	}
}

static int android_handle_setVolume(JNIEnv *env, jobject thiz, jint volume)
{
	int ttyS3_fd = -1;
	int dwByteWrite;
	int vol = (volume*100)/32, sum;
	unsigned char command[9] = {0xFA, 0xFA, 0x09, 0x00, 0xA0, 0x09};
	command[6] = vol;
	sum = checkSum((unsigned char*)command, 7);
	command[7] = sum & 0x00FF;
	command[8] = (sum >> 8) & 0x00FF;

	if((ttyS3_fd = open("/dev/ttyS3", O_RDWR|O_NOCTTY|O_NONBLOCK)) < 0) {
		LOGI("======bonovo dev/ttyS3 SetVolume open result = %d\n",ttyS3_fd);
		return -1;
	} else {
		dwByteWrite = write(ttyS3_fd, command, sizeof(command));
		//LOGV("=== dwByteWrite:%d vol:%d volume:%d\n",dwByteWrite, vol, volume);
		close(ttyS3_fd);
		return 0;
	}
}

static int android_handle_setSingleVolume(JNIEnv *env, jobject thiz, jint channel, jint volume)
{
	int ttyS3_fd = -1;
	int dwByteWrite;
	int cmd = 0, sum;
	unsigned char command[9] = {0xFA, 0xFA, 0x09, 0x00, 0xA0};
    switch(channel){
    case 0x00:
        cmd = COMMAND_SET_LOUT1_VOL;
        break;
    case 0x01:
        cmd = COMMAND_SET_ROUT1_VOL;
        break;
    case 0x02:
        cmd = COMMAND_SET_LOUT2_VOL;
        break;
    case 0x03:
        cmd = COMMAND_SET_ROUT2_VOL;
        break;
    default:
        return -2;
        break;
    }
    if(volume > 100 || volume < 0){
        return -2;
    }
    command[5] = cmd;
	command[6] = volume;
	sum = checkSum((unsigned char*)command, 7);
	command[7] = sum & 0x00FF;
	command[8] = (sum >> 8) & 0x00FF;

	if((ttyS3_fd = open("/dev/ttyS3", O_RDWR|O_NOCTTY|O_NONBLOCK)) < 0) {
		LOGE("======bonovo dev/ttyS3 SetVolume open result = %d\n",ttyS3_fd);
		return -1;
	} else {
		dwByteWrite = write(ttyS3_fd, command, sizeof(command));
		//LOGV("=== dwByteWrite:%d vol:%d volume:%d\n",dwByteWrite, vol, volume);
		close(ttyS3_fd);
		return 0;
	}
}

static int android_handle_setMute(JNIEnv *env, jobject thiz, jboolean mute)
{
	int ttyS3_fd = -1;
	int dwByteWrite;
	int sum;
	unsigned char command[9] = {0xFA, 0xFA, 0x09, 0x00, 0xA0, 0x0A};
	if(mute){
		command[6] = 1;
	}else{
		command[6] = 0;
	}
	sum = checkSum((unsigned char*)command, 7);
	command[7] = sum & 0x00FF;
	command[8] = (sum >> 8) & 0x00FF;

	if((ttyS3_fd = open("/dev/ttyS3", O_RDWR|O_NOCTTY|O_NONBLOCK)) < 0) {
		LOGI("======bonovo dev/ttyS3 SetMute open result = %d\n",ttyS3_fd);
		return -1;
	} else {
		dwByteWrite = write(ttyS3_fd, command, sizeof(command));
		//LOGV("=== dwByteWrite:%d mute:%d\n",dwByteWrite, mute);
		close(ttyS3_fd);
		return 0;
	}
}

static int android_handle_getbrightness(JNIEnv *env, jobject thiz)
{
	int fd, brightness;

	fd = open("/dev/bonovo_handle", O_RDWR|O_NOCTTY|O_NONBLOCK);
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
	LOGE("brightness = %d\n",brightness);
	return brightness;
}

static const char *classPathName = "com/bonovo/bonovohandle/HandleService";

static JNINativeMethod sMethods[] = {
	{"jnigetbrightness","()I", (void *)android_handle_getbrightness},
	{"jniSystemInit","()I", (void *)android_handle_systeminit},
	{"jniSetVolume", "(I)I", (void *)android_handle_setVolume},
	{"jniSetMute","(Z)I", (void *)android_handle_setMute},
	{"jniSendPowerKey", "()Z", (void *)android_handle_sendPowerKey},
	{"jniOnGoToSleep", "()Z", (void *)android_power_goToSleep},
	{"jniOnWakeUp", "()Z", (void *)android_power_wakeUp},
	{"jniSetSoundBalance", "(II)I", (void*)android_handle_setSingleVolume},
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

