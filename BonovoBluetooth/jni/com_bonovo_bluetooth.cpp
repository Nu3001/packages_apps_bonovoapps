#define LOG_TAG "com_bonovo_bluetooth"

#include <asm/ioctl.h>
#include <assert.h>
#include <limits.h>
#include <utils/threads.h>
#include <utils/Log.h>

#include "jni.h"
#include "JNIHelp.h"
#include "android_runtime/AndroidRuntime.h"
#include "com_bonovo_bluetooth_cmd.h"

jobject gBonovoBlueToothObj,gBluePhoneBookObj;
JavaVM *gJavaVM;
static jmethodID method_report,method_report2;

void start_bonovo_bluetooth(void);
void stop_bonovo_bluetooth(void);
void set_bonovo_bluetooth(int cmd, char *byteData, int byteLen);
int set_bonovo_bluetooth_power(int onOff);
int activeAudio(CODEC_Level codec_mode);
int recoverAudio(CODEC_Level codec_mode);

extern char myIncoingCallNumber[];
extern int myIncomingCallLen;
#define MAX_BUFF_LEN 50
void android_callback(int cmd, char* param, int len) {
	int status;
	JNIEnv *env;
//	char buf[MAX_BUFF_LEN];
//	memset(buf, 0, sizeof(buf));

	status = gJavaVM->AttachCurrentThread(&env, NULL);
	if (status < 0) {
		ALOGE("callback_handler: failed to attach " "current thread");
		return;
	}
	jstring str_param;

	if(param != NULL){
		str_param = env->NewStringUTF(param);
	}else{
		str_param = env->NewStringUTF("NULL");
	}
	env->CallVoidMethod(gBonovoBlueToothObj, method_report, cmd, str_param);
    gJavaVM->DetachCurrentThread();
}

void android_synchPhoneBook(char *na ,char * num){
	int status;
	JNIEnv *env;

	status = gJavaVM->AttachCurrentThread(&env, NULL);
		if (status < 0) {
			ALOGE("callback_handler: failed to attach " "current thread");
			return;
		}

	jstring jnum = env->NewStringUTF(num);
	jstring name = env->NewStringUTF(na);

	env->CallVoidMethod(gBluePhoneBookObj, method_report2, name,jnum);

	gJavaVM->DetachCurrentThread();
}

static void android_bonovo_bluetooth_init(JNIEnv* env, jobject thiz) {
	if (!gBonovoBlueToothObj) {
		gBonovoBlueToothObj = env->NewGlobalRef(thiz);
	}

	if(!gBluePhoneBookObj){
		gBluePhoneBookObj = env ->NewGlobalRef(thiz);
	}
	jclass interfaceClass = env->GetObjectClass(gBonovoBlueToothObj);
	method_report = env->GetMethodID(interfaceClass, "BlueToothCallback","(ILjava/lang/String;)V");

	jclass interfaceClass2 = env->GetObjectClass(gBluePhoneBookObj);
	method_report2 =env->GetMethodID(interfaceClass2, "SynchPhoneBook","(Ljava/lang/String;Ljava/lang/String;)V");

	start_bonovo_bluetooth();
}

static void android_bonovo_bluetooth_destroy(JNIEnv* env, jobject thiz)
{
	stop_bonovo_bluetooth();
}

static void android_bonovo_bluetooth_set(JNIEnv* env, jobject thiz, jint cmd)
{
	if (cmd == CMD_AT_CW)
		set_bonovo_bluetooth(cmd, myIncoingCallNumber, myIncomingCallLen);
	else
		set_bonovo_bluetooth(cmd, NULL, 0);
}

/**
 * add by bonovo zbiao
 */
static void android_bonovo_bluetooth_set_withParam(JNIEnv* env, jobject thiz,
		jint cmd, jstring s)
{
	char param[255];
	int paramLen = 0;
	const char *str = env->GetStringUTFChars(s, NULL);
	if(str == NULL){
		ALOGE("android_bonovo_bluetooth_set_withParam GetStringUTFChars() failed.");
		return;
	}

	memset(param, 0, sizeof(param));
	strcpy(param, str);
	paramLen = strlen(str);
	set_bonovo_bluetooth(cmd, param, paramLen);
	env->ReleaseStringUTFChars(s, str);
}

static int android_bonovo_bluetooth_power(JNIEnv* env, jobject thiz, jint status)
{
	return set_bonovo_bluetooth_power(status);
}

static void android_bonovo_bluetooth_set_phone(JNIEnv* env, jobject thiz,
		jstring s) {
	const char *str = env->GetStringUTFChars(s, NULL);
	if(str == NULL){
		ALOGE("android_bonovo_bluetooth_set_phone GetStringUTFChars() failed.");
		return;
	}
	strcpy(myIncoingCallNumber, str);
	myIncomingCallLen = strlen(str);
	env->ReleaseStringUTFChars(s, str);
}

static jstring android_bonovo_bluetooth_get_phone(JNIEnv* env, jobject thiz) {
	jstring jstr = env->NewStringUTF(myIncoingCallNumber);

	return jstr;
}

static jint android_active_audio(JNIEnv *env, jobject thiz, jint level){
	return activeAudio((CODEC_Level)level);
}

static jint android_recover_audio(JNIEnv *env, jobject thiz, jint level){
	return recoverAudio((CODEC_Level)level);
}

static const char *classPathName = "com/bonovo/bluetooth/BonovoBlueToothService";

static JNINativeMethod sMethods[] = {
	{ "BonovoBlueToothInit", 	"()V",	(void *) android_bonovo_bluetooth_init },
	{ "BonovoBlueToothDestroy",	"()V", 	(void *) android_bonovo_bluetooth_destroy },
	{ "BonovoBlueToothSet", 	"(I)V", (void *) android_bonovo_bluetooth_set },
	{ "BonovoBlueToothPower", 	"(I)I", (void *) android_bonovo_bluetooth_power },
	{ "BonovoBlueToothSetWithParam", "(ILjava/lang/String;)V", (void*)android_bonovo_bluetooth_set_withParam },
	{ "BonovoBlueToothActiveAudio", "(I)I", (void *) android_active_audio},
	{ "BonovoBlueToothRecoveryAudio", "(I)I", (void *) android_recover_audio},
	//{"BonovoBlueToothSetPhone","(Ljava/lang/String;)V",(void *)android_bonovo_bluetooth_set_phone},
	//{"GetContacts","()Ljava/lang/String;",(void *) android_bonovo_bluetooth_get_phone},
};

/*
 * Register several native methods for one class.
 */
static int registerNativeMethods(JNIEnv* env, const char* className,
		JNINativeMethod* methods, int numMethods) {
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
static int registerNatives(JNIEnv* env) {
	if (!registerNativeMethods(env, classPathName, sMethods,
			sizeof(sMethods) / sizeof(sMethods[0]))) {
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

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	UnionJNIEnvToVoid uenv;
	uenv.venv = NULL;
	jint result = -1;
	JNIEnv* env = NULL;
	gJavaVM = vm;

	ALOGD("JNI_OnLoad");

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

	bail: return result;
}

