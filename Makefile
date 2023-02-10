SHELL=/bin/bash

setup-emulator:
	@chmod +x shell/push-test-resources-to-emulator.sh
	@sudo shell/push-test-resources-to-emulator.sh

optimize-drawables:
	@avocado app/src/main/res/drawable/*.xml

build-apk:
	@./gradlew assembleRelease --console verbose

build-aab:
	@./gradlew :app:bundleRelease --console verbose

publish-release-gh:
	@gh release create $(version) --generate-notes app/build/outputs/apk/release/$(version).apk