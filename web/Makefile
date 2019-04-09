test_bundle := build/bundles/test.js
node_modules := node_modules/.bin/mocha

$(node_modules):
	npm install

core:
	cd ../core; ./gradlew --quiet jsMainClasses jsTestClasses
	cp ../core/build/classes/kotlin/js/*/*.js node_modules/

$(test_bundle): test/index.js core
	mkdir -p build/bundles
	npx webpack $< --mode development --target web --output $@

test: $(test_bundle) $(node_modules)
	open test/index.html

clean:
	rm -rf build

distclean: clean
	rm -rf node_modules

.PHONY: test clean distclean core
