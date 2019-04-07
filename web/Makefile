test_bundle := build/bundles/test.js
node_modules := node_modules/.bin/mocha

$(node_modules):
	npm install

core:
	cd ../core; ./gradlew --quiet jsMainClasses jsTestClasses
	cp ../core/build/classes/kotlin/js/*/*.js node_modules/

$(test_bundle): test/index.js core
	mkdir -p build/bundles
	npx webpack $< --silent --mode production --output $@

test: $(test_bundle) $(node_modules)
	mkdir -p build/reports
	npx mocha $@ --reporter xunit > build/reports/tests.xml
	npx mocha $@

clean:
	rm -rf build

distclean: clean
	rm -rf node_modules

.PHONY: test clean distclean core
