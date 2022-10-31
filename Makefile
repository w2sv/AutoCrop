SHELL=/bin/bash

setup-emulator:
	@chmod +x shell/push-test-resources-to-emulator.sh
	@sudo shell/push-test-resources-to-emulator.sh

tasks:
	@./gradlew tasks

unit-test:
	@./gradlew test  # runs test on debug and release

android-test:
	@./gradlew connectedCheck

check:
	@./gradlew check

build:
	@./gradlew build  # requires release keys

optimize-drawables:
	@avocado app/src/main/res/drawable/*.xml