#include <asm/ioctl.h>
#include <assert.h>
#include <limits.h>
#include <utils/threads.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <errno.h>
#include <string.h>

#include "jni.h"
#include "JNIHelp.h"
#include "android_runtime/AndroidRuntime.h"
#include "remotectl.h"

#define LOG_TAG "COM_BONOVO_IRCTLS"
#define DEV_PATH "/dev/bonovo_irctl"
//#define DEBUG

jint isOpenDev = 0;

static int set_mode(int fd, jboolean mode)
{
	int err = 0;
	if(mode == JNI_TRUE)
	{
		if((err = ioctl(fd, IOCTL_BONOVO_CUSTOM_MODE)) < 0){
			LOGE("set custom mode failed. Error:%d - %s\n", errno, strerror(errno));
			return err;
		}
		isOpenDev = 1;
	}else{
		if((err = ioctl(fd, IOCTL_BONOVO_NORMAL_MODE)) < 0){
			LOGE("set normal mode failed. Error:%d - %s\n", errno, strerror(errno));
			return err;
		}
		isOpenDev = 0;
	}
	return err;
}

static jint android_remotectrl_set_mode(JNIEnv* env, jobject thiz, jboolean mode)
{
#ifdef DEBUG
	LOGD("============ bonovo_irctls_set_mode :%s==========\n", (mode == JNI_TRUE ? "true" : "false"));
#endif
	int fd;
	int err;

	if((fd = open(DEV_PATH, O_RDWR)) < 0){
		LOGE("can't open %s in bonovo_irctls_set_mode.\n", DEV_PATH);
		return -1;
	}
	err = set_mode(fd, mode);
	close(fd);

	return isOpenDev;
}

static jint android_remotectrl_read_key(JNIEnv* env, jobject thiz, jintArray buff)
{
	int fd;
	int err = 0;
	struct bonovo_rc_key rc_key;
	jint buff_size;
#ifdef DEBUG
	LOGD("============ bonovo_irctls_read_key==========\n");
#endif
	if(!isOpenDev){
/*		if(ioctl(fd, IOCTL_BONOVO_CUSTOM_MODE) < 0){
			LOGE("set custom mode failed. Error:%d - %s\n", errno, strerror(errno));
			close(fd);
			isOpenDev = 0;
			return -1;
		}else{
			isOpenDev = 1;
		}
*/
		return -1;
	}

	buff_size = env->GetArrayLength(buff);
	if(buff_size < sizeof(struct bonovo_rc_key)/sizeof(int)){
		LOGE("the buff is not enough. sizeof(struct bonovo_rc_key):%d\n", sizeof(struct bonovo_rc_key));
		LOGE("buff_size:%d\n", buff_size);
		return -3;
	}

	if((fd = open(DEV_PATH, O_RDWR)) < 0){
		LOGE("can't open %s in android_remotectrl_read_key.\n", DEV_PATH);
		LOGE("fail Info:fd=%d errno=%d - %s\n", fd, errno, strerror(errno));
		return -2;
	}

	if((err = ioctl(fd, IOCTL_BONOVO_READ_KEY, &rc_key)) < 0){
		LOGE("read a key failed.err:%d errno:%d - %s\n", err, errno, strerror(errno));
		close(fd);
		return err;
	}

	close(fd);

	jint* temp_buf;
	jint* rc_key_addr = (jint *)&rc_key;

	temp_buf = env->GetIntArrayElements(buff, NULL);
	memset(temp_buf, 0, buff_size);
	for(int k = 0; k < sizeof(struct bonovo_rc_key)/sizeof(int); k++)
	{
		temp_buf[k] = rc_key_addr[k];
//		if((temp_buf[k] & 0x0FF) != ((~temp_buf[k] >> 8) & 0x0FF)){
//			LOGE("buf[%d] error:%d\n", k, temp_buf[k]);
//			err = -4;
//			break;
//		}
//		if(k != 0)
//			temp_buf[k] = (temp_buf[k] >> 8) & 0x0FF;
//		LOGE("after  buff[] = 0x%04x\n", temp_buf[k]);
	}
	env->ReleaseIntArrayElements(buff, temp_buf, 0);

	return err;
}

static jint android_remotectrl_write_key(JNIEnv* env, jobject thiz, jintArray buff)
{
	int fd;
	int err = 0;
	jint buff_size;
#ifdef DEBUG
	LOGD("============ android_remotectrl_write_key ==========\n");
#endif
	if(!isOpenDev){
		/*
		if(ioctl(fd, IOCTL_BONOVO_CUSTOM_MODE) < 0){
			LOGE("set custom mode failed. Error:%d - %s\n", errno, strerror(errno));
			close(fd);
			isOpenDev = 0;
			return -1;
		}else{
			isOpenDev = 1;
		}
		*/
		return -1;
	}

	buff_size = env->GetArrayLength(buff);
	if(buff_size < sizeof(struct bonovo_rc_wkey)/sizeof(int)){
		LOGE("the buff is not enough. sizeof(struct bonovo_rc_wkey):%d\n", sizeof(struct bonovo_rc_wkey));
		LOGE("buff_size:%d\n", buff_size);
		return -3;
	}

	if((fd = open(DEV_PATH, O_RDWR)) < 0){
		LOGE("can't open %s in android_remotectrl_write_key.\n", DEV_PATH);
		LOGE("fail Info:fd=%d errno=%d - %s\n", fd, errno, strerror(errno));
		return -2;
	}

	jint* temp_buf;

	temp_buf = env->GetIntArrayElements(buff, NULL);
/*	for(int k = 0; k < sizeof(struct bonovo_rc_wkey)/sizeof(int); k++)
	{
		LOGE("before buff[] = %d\n", temp_buf[k]);
	}
*/

	if((err = ioctl(fd, IOCTL_BONOVO_WRITE_KEY, temp_buf)) < 0){
		LOGE("write key failed.\nINFO:fd=%d errno=%d - %s\n", fd, errno, strerror(errno));
	}
	env->ReleaseIntArrayElements(buff, temp_buf, 0);
	close(fd);
	return err;
}

static jint android_remotectrl_read_addr(JNIEnv* env, jobject thiz, jint key_type)
{
	int fd;
	int err = 0;
	int addrCode = key_type;
#ifdef DEBUG
	LOGD("============ android_remotectrl_read_addr ==========\n");
#endif
	if(!isOpenDev){
		/*
		if(ioctl(fd, IOCTL_BONOVO_CUSTOM_MODE) < 0){
			LOGE("set custom mode failed. Error:%d - %s\n", errno, strerror(errno));
			close(fd);
			isOpenDev = 0;
			return -1;
		}else{
			isOpenDev = 1;
		}
		*/
		return -1;
	}

	if((fd = open(DEV_PATH, O_RDWR)) < 0){
		LOGE("can't open %s in android_remotectrl_read_addr.\n", DEV_PATH);
		LOGE("fail Info:fd=%d errno=%d - %s\n", fd, errno, strerror(errno));
		return -2;
	}

	if((err = ioctl(fd, IOCTL_BONOVO_READ_ADDRCODE, (int*)&addrCode)) < 0){
		LOGE("write address code failed.\nINFO:fd=%d errno=%d - %s\n", fd, errno, strerror(errno));
		addrCode = err;
	}

	close(fd);
	return addrCode;
}

static jint android_remotectrl_write_addr(JNIEnv* env, jobject thiz, jintArray buff) // modify from jint to jintArray
{
	int fd;
	int err = 0, buff_size;
	jint* temp_buf;
#ifdef DEBUG
	LOGD("============ android_remotectrl_write_addr ==========\n");
#endif
	if(!isOpenDev){
		/*
		if(ioctl(fd, IOCTL_BONOVO_CUSTOM_MODE) < 0){
			LOGE("set custom mode failed. Error:%d - %s\n", errno, strerror(errno));
			close(fd);
			isOpenDev = 0;
			return -1;
		}else{
			isOpenDev = 1;
		}
		*/
		return -1;
	}

	buff_size = env->GetArrayLength(buff);
	if(buff_size < sizeof(struct bonovo_key_addr)/sizeof(int)){
		LOGE("the buff is not enough. sizeof(struct bonovo_rc_wkey):%d\n", sizeof(struct bonovo_key_addr));
		LOGE("buff_size:%d\n", buff_size);
		return -3;
	}

	if((fd = open(DEV_PATH, O_RDWR)) < 0){
		LOGE("can't open %s in android_remotectrl_write_addr.\n", DEV_PATH);
		LOGE("fail Info:fd=%d errno=%d - %s\n", fd, errno, strerror(errno));
		return -2;
	}
	temp_buf = env->GetIntArrayElements(buff, NULL);
/*	for(int k = 0; k < sizeof(struct bonovo_rc_wkey)/sizeof(int); k++)
		{
			LOGE("before buff[] = %d\n", temp_buf[k]);
		}
	*/

	if((err = ioctl(fd, IOCTL_BONOVO_WRITE_ADDRCODE, temp_buf)) < 0){
		LOGE("write address code failed.\nINFO:fd=%d errno=%d - %s\n", fd, errno, strerror(errno));
	}
	env->ReleaseIntArrayElements(buff, temp_buf, 0);

	close(fd);
	return err;
}

static jint android_remotectrl_read_offset(JNIEnv* env, jobject thiz, jint key_type)
{
	int fd;
	int err = 0;
	int addrCode = key_type;

	if((fd = open(DEV_PATH, O_RDWR)) < 0){
		LOGE("can't open %s in android_remotectrl_read_addr.\n", DEV_PATH);
		LOGE("fail Info:fd=%d errno=%d - %s\n", fd, errno, strerror(errno));
		return -2;
	}

	if((err = ioctl(fd, IOCTL_BONOVO_READ_ACCURACY, (int*)&addrCode)) < 0){
		LOGE("write address code failed.\nINFO:fd=%d errno=%d - %s\n", fd, errno, strerror(errno));
		addrCode = err;
	}

	close(fd);
	return addrCode;
}

static jint android_remotectrl_write_offset(JNIEnv* env, jobject thiz, jintArray buff) // modify from jint to jintArray
{
	int fd;
	int err = 0, buff_size;
	jint* temp_buf;

	buff_size = env->GetArrayLength(buff);
	if(buff_size < sizeof(struct bonovo_key_addr)/sizeof(int)){
		LOGE("the buff is not enough. sizeof(struct bonovo_rc_wkey):%d\n", sizeof(struct bonovo_key_addr));
		LOGE("buff_size:%d\n", buff_size);
		return -3;
	}

	if((fd = open(DEV_PATH, O_RDWR)) < 0){
		LOGE("can't open %s in android_remotectrl_write_addr.\n", DEV_PATH);
		LOGE("fail Info:fd=%d errno=%d - %s\n", fd, errno, strerror(errno));
		return -2;
	}
	temp_buf = env->GetIntArrayElements(buff, NULL);

	if((err = ioctl(fd, IOCTL_BONOVO_WRITE_ACCURACY, temp_buf)) < 0){
		LOGE("write address code failed.\nINFO:fd=%d errno=%d - %s\n", fd, errno, strerror(errno));
	}
	env->ReleaseIntArrayElements(buff, temp_buf, 0);

	close(fd);
	return err;
}

static jint android_remotectrl_clear_keys(JNIEnv* env, jobject thiz, jint key_type)   // add key_type
{
	int fd;
	int err = 0;
#ifdef DEBUG
	LOGD("============ android_remotectrl_clear_keys ==========\n");
#endif

	if((fd = open(DEV_PATH, O_RDWR)) < 0){
		LOGE("can't open %s in android_remotectrl_clear_keys.\n", DEV_PATH);
		LOGE("fail Info:fd=%d errno=%d - %s\n", fd, errno, strerror(errno));
		return -2;
	}

	if((err = ioctl(fd, IOCTL_BONOVO_CLEAR_KEYS, (int*)&key_type)) < 0){
		LOGE("clear keys failed.\nINFO:fd=%d errno=%d - %s\n", fd, errno, strerror(errno));
		return err;
	}
	close(fd);
	return 0;
}

static jint android_remotectrl_read_config(JNIEnv* env, jobject thiz,  jintArray buff)  // buff 第一个字节为要读取的按键类型
{
	int fd;
	int err = 0;
	jint buff_size;
#ifdef DEBUG
	LOGD("============ android_remotectrl_read_config ==========\n");
#endif
	buff_size = env->GetArrayLength(buff);
	if(buff_size < (KEY_VALUE_PAIRS_NUM + KEY_VALUE_PAIRS_NUM)){
		LOGE("the buff is not enough. sizeof(struct bonovo_rc_wkey):%d\n", sizeof(struct bonovo_rc_wkey));
		LOGE("buff_size:%d\n", buff_size);
		return -3;
	}

	if((fd = open(DEV_PATH, O_RDWR)) < 0){
		LOGE("can't open %s in android_remotectrl_write_key.\n", DEV_PATH);
		LOGE("fail Info:fd=%d errno=%d - %s\n", fd, errno, strerror(errno));
		return -2;
	}

	jint* temp_buf;
	int k;
	temp_buf = env->GetIntArrayElements(buff, NULL);

	if((err = ioctl(fd, IOCTL_BONOVO_READ_CONFIG, temp_buf)) < 0){
		LOGE("write key failed.\nINFO:fd=%d errno=%d - %s\n", fd, errno, strerror(errno));
	}

	// debug
/*	for(k = 0; k < KEY_VALUE_PAIRS_NUM*2; k++)
	{
		LOGE("buff[%d] = %d --- buff[%d] = %d\n", k, temp_buf[k], k+1, temp_buf[k+1]);
		k++;
	}
*/
	env->ReleaseIntArrayElements(buff, temp_buf, 0);
	close(fd);
	return err;
}

static jint android_remotectrl_write_config(JNIEnv* env, jobject thiz,  jintArray buff)
{
	int fd;
	int err = 0;
	jint buff_size;
#ifdef DEBUG
	LOGD("============ android_remotectrl_write_config ==========\n");
#endif
	if(!isOpenDev){
		/*
		if(ioctl(fd, IOCTL_BONOVO_CUSTOM_MODE) < 0){
			LOGE("set custom mode failed. Error:%d - %s\n", errno, strerror(errno));
			close(fd);
			isOpenDev = 0;
			return -1;
		}else{
			isOpenDev = 1;
		}
		*/
		return -1;
	}

	buff_size = env->GetArrayLength(buff);
	if(buff_size < (KEY_VALUE_PAIRS_NUM + KEY_VALUE_PAIRS_NUM)){
		LOGE("the buff is not enough. sizeof(struct bonovo_rc_wkey):%d\n", sizeof(struct bonovo_rc_wkey));
		LOGE("buff_size:%d\n", buff_size);
		return -3;
	}

	if((fd = open(DEV_PATH, O_RDWR)) < 0){
		LOGE("can't open %s in android_remotectrl_write_key.\n", DEV_PATH);
		LOGE("fail Info:fd=%d errno=%d - %s\n", fd, errno, strerror(errno));
		return -2;
	}

	jint* temp_buf;
	int k;
	temp_buf = env->GetIntArrayElements(buff, NULL);

	// debug
/*	for(k = 0; k < KEY_VALUE_PAIRS_NUM * 2; k++)
	{
		LOGE("buff[%d] = %d --- buff[%d] = %d\n", k, temp_buf[k], k+1, temp_buf[k+1]);
		k++;
	}
*/
	if((err = ioctl(fd, IOCTL_BONOVO_WRITE_CONFIG, temp_buf)) < 0){
		LOGE("write key failed.\nINFO:fd=%d errno=%d - %s\n", fd, errno, strerror(errno));
	}

	env->ReleaseIntArrayElements(buff, temp_buf, 0);
	close(fd);
	return err;
}

static jint android_remotectrl_import_config(JNIEnv* env, jobject thiz,  jintArray buff)
{
	return 0;
}

static jint android_remotectrl_export_config(JNIEnv* env, jobject thiz,  jintArray buff)
{
	return 0;
}

static const char *classPathName = "com.bonovo.keyeditor.KeyService";
static JNINativeMethod sMethods[] = {
	{"nativeSetMode","(Z)I",(void *)android_remotectrl_set_mode},
	{"nativeReadKey", "([I)I", (void *)android_remotectrl_read_key},
	{"nativeWriteKey", "([I)I", (void *)android_remotectrl_write_key},
	{"nativeReadAddrCode", "(I)I", (void *)android_remotectrl_read_addr},
	{"nativeWriteAddrCode", "([I)I", (void *)android_remotectrl_write_addr},
	{"nativeClearKeys", "(I)I", (void *)android_remotectrl_clear_keys},
	{"nativeReadConfig", "([I)I", (void *)android_remotectrl_read_config},
	{"nativeWriteConfig", "([I)I", (void *)android_remotectrl_write_config},
	{"nativeReadOffset", "(I)I", (void *)android_remotectrl_read_offset},
	{"nativeWriteOffset", "([I)I", (void *)android_remotectrl_write_offset},
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

/*
  if (!registerNativeMethods(env, serverPathName,
            sMethods, sizeof(sMethods) / sizeof(sMethods[0]))) {
      return JNI_FALSE;
    }
*/

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
#ifdef DEBUG
    LOGI("JNI_OnLoad");
#endif
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
