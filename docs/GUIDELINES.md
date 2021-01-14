# Developer Guidelines

## Communication Channels

* Our source code is [hosted on GitHub](https://github.com/iSoron/uhabits) and this is where all the main development takes place. We use [GitHub Issues](https://github.com/iSoron/uhabits/issues) for keeping track of open bugs and open development tasks. We also have a [gitter channel](https://gitter.im/loophabits/dev) for real-time discussions about coding and pull requests.

* Regular users are encouraged to post feature requests and support questions under [GitHub Discussions](https://github.com/iSoron/uhabits/discussions). This is also where major announcements about the project are made.

* Translations are managed in a [separate translation platform](https://translate.loophabits.org/).

## Building and Testing the Project

Please see `docs/BUILD.md` and `docs/TEST.md`

## Directory Layout

* `docs` Documentation for developers.
* `landing` Source code for our [landing page](http://loophabits.org/).
* `uhabits-android` Android-specific code.
* `uhabits-core` Common code used by all platforms (Android, iOS).
* `uhabits-core-legacy` Proof-of-concept module, developed to evaluate the feasibility of using Kotlin multiplatform for the app; not currently used, and it will be removed soon, once all useful code is ported to the other modules.
* `uhabits-ios` Experimental iOS port of Loop. Not currently used in production.
* `uhabits-server` Source code for any server-side components the app (for example, device sync).
* `uhabits-web` Experimental web port of Loop. Not currently used in production.

## Branching Policy

This repository uses the [git-flow branching model](https://nvie.com/posts/a-successful-git-branching-model/). Basically, there are two main branches, `dev` and `master`. All the development takes place in the `dev` branch. After the new features have been implemented and tested, they are merged into the `master` branch and a new version of the app is released. Please submit your pull requests against the `dev` branch.

## Submiting Code

Proposed code changes should be submitted to the project through [GitHub pull requests](https://github.com/iSoron/uhabits/pulls). For the basic steps of creating a pull request, see [GitHub's documentation](https://docs.github.com/en/free-pro-team@latest/github/collaborating-with-issues-and-pull-requests/creating-a-pull-request). The following suggestions will help your pull request gets merged quickly and with few changes. 

* **Write a clear description of your proposed code changes:** Although it may look obvious to you, it's not always clear to others what is your pull request trying to accomplish. Please always describe what problem is your pull request is trying to solve, and how does it solve it (on a very high level). If you are fixing a bug that has not been reported before, please describe it first, including the steps to reproduce it.

* If your pull request implements a completely new feature or contains large amounts of code, please **discuss it with other developers before writing it**. You are welcome to ask about it in our developer chat room, or to open a draft pull request outlining how are you planning to solve the problem. The draft pull request may not even contain any code.

* If your pull request involves changes to the user interface, **please work on a mockup first and submit a draft pull request with your proposed UI changes** before writing the code to make it functional, to gather feedback from other developers and users. [Inkscape](https://inkscape.org/) and [Figma](https://www.figma.com/) are good tools that you can use.

* **Keep your pull requests small.** Small pull requests are easy to review and can be quickly merged. The larger your pull request is, the longer it will take for others to review it and for it to get merged. Instead of submitting one large pull request that contains fixes for three separate issues, please submit three small pull requests instead.

* **Keep your pull requests independent.** If you submit multiple pull requests, please make sure that each one can be merged independently of the others. If one of the pull requests needs to be rewritten, the other ones can be merged.

* **Keep refactoring separate.** While implementing bug fixes and new features, you will certainly realize that other parts of our existing code could be improved. Please do not change it yet. Get your bug fix or new feature merged first, then submit a separate pull request for improving the existing code. Avoid renaming classes, removing unnecessary statements, or doing any other refactoring work on pull requests that propose bug fixes or new functionality.

Further resources:

* [*How to Make Your Code Reviewer Fall in Love with You*](https://mtlynch.io/code-review-love), by Michael Lynch.

## Code Style

For Kotlin, we follow [ktlint](https://ktlint.github.io/) style with default settings. This code style is enforced by our automated build pipeline. To make sure that IntelliJ and Android Studio are configured according to ktlint, run `./gradlew ktlintApplyToIdea`. To check that all code is properly formatted, run `./gradlew ktlintCheck`. You can install a Git pre-commit hook to ensure that the code is properly formatted when you commit using `./gradlew addKtlintFormatGitPreCommitHook`. See more details in [ktlint-gradle](https://github.com/jlleitschuh/ktlint-gradle).

For legacy Java code, we don't have strict guidelines. Please follow a code style similar to the file you are modifying. Note that new classes should be written in Kotlin. Pull requests converting existing Java code to Kotlin are also welcome.

## Release Process

The project loosely follows [semantic versioning](https://semver.org/), adapted for applications. Suppose, for example, that version `1.2.3` has just been released. The next version would be:
* `1.2.4` if a bug is being fixed.
* `1.3.0` if minor new features are being introduced.
* `2.0.0` if major new features are being introduced.

Releases are first made available to beta testers on Google Play and to all F-Droid users. After no bugs are found, they are rolled out to the remaining users. Releases are also made available on [GitHub Releases](https://github.com/iSoron/uhabits/releases).

