LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

GDX_CFLAGS 	  := -O2 -Wall

include $(LOCAL_PATH)/Box2D/Android.mk

LOCAL_MODULE    := gdx
LOCAL_ARM_MODE  := arm
LOCAL_SRC_FILES := $(GDX_SRC_FILES)
LOCAL_CFLAGS    := $(GDX_CFLAGS)

include $(BUILD_SHARED_LIBRARY)

