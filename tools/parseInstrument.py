#!/usr/bin/env python3
"""
Android Instrumentation Test Parser

Given a raw Android Instrumentation log (produced by "adb shell am instrument -r ...") this script
return zero if all tests pass and non-zero if some tests fail. In case of failure, this script
also prints arguments that, if passed to "am instrument", will cause it to re-run just the tests
that failed. This script additionally prints warnings about the tests on the STDERR; e.g. slow tests.
"""
import sys
import re

STATUS_START = 1
STATUS_DISABLED = -3
SLOW_TEST_THRESHOLD = 5.0

COLOR_RED = '\033[91m'
COLOR_YELLOW = '\033[93m'
COLOR_END = '\033[0m'

def error(msg):
    sys.stderr.write("%s%s%s\n" % (COLOR_RED, msg, COLOR_END))

def warning(msg):
    sys.stderr.write("%s%s%s\n" % (COLOR_YELLOW, msg, COLOR_END))

log_filename = sys.argv[1]
current_class, current_method = None, None
failed_tests = []
am_args = "-e class "
exit_code = 1

for line in open(log_filename).readlines():
    matches = re.findall('^([0-9.]*)', line)
    current_time = float(matches[0])

    matches = re.findall('INSTRUMENTATION_STATUS: class=(.*)', line)
    if len(matches) > 0:
        current_class = matches[0]

    matches = re.findall('INSTRUMENTATION_STATUS: test=(.*)', line)
    if len(matches) > 0:
        current_method = matches[0]

    matches = re.findall('OK \([0-9]* tests?\)', line)
    if len(matches) > 0:
        exit_code = 0

    matches = re.findall('INSTRUMENTATION_STATUS_CODE: ([-0-9]*)', line)
    if len(matches) > 0:
        status_code = int(matches[0])
        if (status_code < 0) and (status_code != STATUS_DISABLED):
            am_args += f"{current_class}#{current_method},"
            failed_tests.append(f"{current_class}#{current_method}")
        if status_code == STATUS_START:
            initial_time = current_time
        else:
            elapsed_time = current_time - initial_time
            if(elapsed_time > SLOW_TEST_THRESHOLD):
                warning("SLOW %s#%s (%.2f seconds)" % (current_class, current_method, elapsed_time))

if len(failed_tests) > 0:
    for test in failed_tests:
        error("FAIL %s" % test)
    print(am_args[:-1])

sys.exit(exit_code)
