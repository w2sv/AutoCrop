SHELL=/bin/bash

setup-emulator:
	@chmod +x shell/push-test-resources-to-emulator.sh
	@sudo shell/push-test-resources-to-emulator.sh

optimize-drawables:
	@avocado app/src/main/res/drawable/*.xml

# ==============
# Publishing
# ==============

VERSION := $(shell grep -Po '^version=\K.*' gradle.properties)

publish-listing:
	@./gradlew publishListing  --console verbose

build-and-publish:
	@echo -e "Retrieved Version: $(VERSION)\n\n Hit enter if you have\n 1. Incremented the version\n 2. Updated the release notes\n\n Otherwise cancel target now."
	@read

#	@./gradlew check

	@echo "Clean"  # Required as 'publishBundle' publishes all .aab's in specified archive dir
	@./gradlew clean

#	@git add .; git commit -m "$(VERSION)"; git push;

#	@echo "Build APK"
#	@./gradlew assembleRelease --console verbose

	@echo "Build AAB"
	@./gradlew :app:bundleRelease --console verbose

	@echo "Create GitHub Release"
	@gh release create $(VERSION) -F app/src/main/play/release-notes/en-US/production.txt # app/build/outputs/apk/release/$(VERSION).apk

	@echo "Publish Bundle"
	@./gradlew publishBundle --artifact-dir app/build/outputs/bundle/release --console verbose