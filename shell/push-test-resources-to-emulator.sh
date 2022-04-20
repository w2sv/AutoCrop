# adb wait-for-device shell "while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done;"

ANDROID_TEST_RESOURCES_DIR_PATH=/sdcard/Pictures/AndroidTestResources

if adb shell find "$ANDROID_TEST_RESOURCES_DIR_PATH" | read -r; then
  echo "$ANDROID_TEST_RESOURCES_DIR_PATH already present on emulator"
else
  echo "$ANDROID_TEST_RESOURCES_DIR_PATH not yet present"
  adb shell mkdir "$ANDROID_TEST_RESOURCES_DIR_PATH"
  adb push app/src/androidTest/resources/* "$ANDROID_TEST_RESOURCES_DIR_PATH"
fi