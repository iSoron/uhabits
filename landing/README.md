Loop Habit Tracker Landing Page
===============================

This folder contains the source code that generates the project landing page, currently hosted at https://loophabits.org/

Pull requests with ideas for improving it are very welcome.

Build instructions
------------------

1. Install `haml`:
```bash
sudo apt install ruby-haml
```
2. Install `pandoc-ruby`:
```bash
gem install pandoc-ruby
```
3. Run `Makefile`
```bash
make
```
4. View the results (using, for example, [npm serve](https://www.npmjs.com/package/serve))
```bash
npm serve out/
```

