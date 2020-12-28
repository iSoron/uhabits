haml   := src/*.haml
sass   := src/*.sass

html   := $(patsubst src/%, out/%, $(patsubst %.haml,%.html,$(wildcard $(haml))))
css    := $(patsubst src/%, out/%, $(patsubst %.sass,%.css,$(wildcard $(sass))))
src    := $(wildcard src/**)

compile: $(html) $(css)
	@rsync -rupE assets/ out/

out/%.css: src/%.sass $(src)
	@echo '    sass  $<'
	@mkdir -p `dirname $@`
	@sass $< $@

out/%.html: src/%.haml $(src)
	@echo '    haml  $<'
	@mkdir -p `dirname $@`
	@haml -E UTF-8 $< $@

push:
	rsync -avP out/ axavier.org:/www/loophabits.org/

clean:
	@rm -rfv out
	@rm -rfv tmp

