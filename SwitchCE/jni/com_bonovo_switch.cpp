#define LOG_TAG "com_bonovo_switch.cpp"

#include <asm/ioctl.h>

#include <assert.h>
#include <limits.h>
#include <utils/threads.h>
#include <main.h>


#include "jni.h"
#include "JNIHelp.h"
#include "android_runtime/AndroidRuntime.h"

char *dev = "/dev/ttyS3";
int fdUART;

typedef struct _CMD_MSG_STRU{
	unsigned int type;
	unsigned char size;
	unsigned char data[COMM_MAX_PARAMNUM];

	pthread_mutex_t msg_lock;
}CMD_MSG_STRU,*PCMD_MSG_STRU;
CMD_MSG_STRU smsg;

extern "C" int open_Serial(int* fd, char* dev);
extern "C" int set_Parity(int fd,int nBits,int nParity,int nStop);
extern "C" int set_Speed(int fd, int nSpeed);
extern "C" int close_Serial(int fd);
extern "C" int write_Serial(int fd, unsigned char *data, int size);
extern "C" int CmdTreat(int cmd, CMD_MSG_STRU* msg, unsigned char* param, unsigned char paramnumber);


static int android_serial_write(void)
{
	int nwrite, n;
	unsigned char *p = NULL;
	
	nwrite = write_Serial(fdUART, smsg.data, smsg.size);
#ifdef BT_BUG
	LOGE("nwrite = %d\n", nwrite);
	p = &smsg.data[0];
	for(n = 0; n < smsg.size; n++)
	{
        LOGE("frame = %x--------%d\n", *(p+n),n);
	}
#endif		
	return(JNI_TRUE);
}

static int android_switch_serialSetup(void)
{
	if(open_Serial(&fdUART, dev) < 0)
	{
		LOGE("Initial error!!\n");
		return(JNI_FALSE);
	}
	
#ifdef BT_BUG
	LOGE("open serial fd = %d, &fd = 0x%x\n", fdUART, &fdUART);
#endif

/*	if(set_Parity(fdUART,8,'N',1) < 0)
    {
		LOGE("set_Parity error\n");
		close_Serial(fdUART);
		return(JNI_FALSE);
    }

	if(set_Speed(fdUART, 115200) < 0)
	{
		LOGE("set_Speed error\n");
		close_Serial(fdUART);
		return(JNI_FALSE);
	}*/
	return(JNI_TRUE);
}

static int android_switch_openSerial(JNIEnv *env, jobject thiz)
{
	if(android_switch_serialSetup() < 0)
	{
		LOGE("setup serial error!!\n");
		return JNI_FALSE;
	}
	return JNI_TRUE;
}

static int android_switch_closeSerial(JNIEnv *env, jobject thiz)
{
	close_Serial(fdUART);
	return JNI_TRUE;
}

static int android_switch_goWinCE(JNIEnv *env, jobject thiz)
{
	memset(&smsg,0,sizeof(CMD_MSG_STRU));
	CmdTreat(0, &smsg, NULL, 0);
#ifdef BT_BUG
	LOGE("android_switch_goWinCE\n");
#endif
	android_serial_write();

	return JNI_TRUE;
}


static const char *classPathName = "com/bonovo/switchce/SwitchCEActivity";

static JNINativeMethod sMethods[] = {
	{"jniopenserial","()V", (void *)android_switch_openSerial},
	{"jnigoWinCE", "()V", (void *)android_switch_goWinCE},
	{"jnicloseserial", "()V", (void *)android_switch_closeSerial},
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

