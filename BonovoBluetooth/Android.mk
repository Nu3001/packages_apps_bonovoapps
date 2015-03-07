LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := BonovoBluetooth

#LOCAL_OVERRIDES_PACKAGES := bluebooth

# deleted by shmin for using android.os.SystemProperties class
#LOCAL_SDK_VERSION := current:
LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)

# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
