LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

#LOCAL_STATIC_JAVA_LIBRARIES :=libprotobuf-java-2.3.0-lite

LOCAL_MODULE_TAGS := optional

#LOCAL_SRC_FILES := $(call all-java-files-under, src)



LOCAL_SRC_FILES := $(call all-java-files-under, src) \
    $(call all-proto-files-under, protos)

LOCAL_PROTOC_OPTIMIZE_TYPE := lite
LOCAL_PROTOC_FLAGS := --proto_path=$(LOCAL_PATH)/protos

    

LOCAL_PACKAGE_NAME := BonovoMcu

LOCAL_CERTIFICATE := platform

#LOCAL_OVERRIDES_PACKAGES := bonovomcu

# deleted by shmin for using android.os.SystemProperties class
#LOCAL_SDK_VERSION := current:

LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)

#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := protobuf-java-2.3.0-lite

#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libprotobuf-java-2.3.0-lite:libs/protobuf.jar

#include $(BUILD_MULTI_PREBUILT) 


# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
