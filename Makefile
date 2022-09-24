SHELL=/bin/bash

setup-emulator:
	@chmod +x shell/push-test-resources-to-emulator.sh
	@sudo shell/push-test-resources-to-emulator.sh

tasks:
	@./gradlew tasks

unit-test:
	@./gradlew test  # runs test on debug and release

check:
	@./gradlew check

build:
	@./gradlew build  # requires release keys