![license](https://img.shields.io/github/license/mathisdt/sdbviewer.svg?style=flat)
[![last released](https://img.shields.io/github/release-date/mathisdt/sdbviewer.svg?label=last%20released&style=flat)](https://github.com/mathisdt/sdbviewer/releases)
[![build](https://github.com/mathisdt/sdbviewer/actions/workflows/build.yaml/badge.svg)](https://github.com/mathisdt/sdbviewer/actions/)

# Song Database Viewer

The Song Database is a program to show lyrics on a digital projector for worship in a congregation.
This app can display the data produced by [Song Database](https://github.com/mathisdt/sdb2/) if it
is accessible via a URL (web address). It cannot be used for anything else, so if you don't use
Song Database, this probably isn't for you!

[<img src="https://zephyrsoft.org/wp-content/uploads/get-it-on-fdroid.png"
     alt="Get it on F-Droid"
     height="54px">](https://f-droid.org/packages/org.zephyrsoft.sdbviewer/)
[<img src="https://zephyrsoft.org/wp-content/uploads/get-it-on-google-play.png"
     alt="Get it on Google Play"
     height="54px">](https://play.google.com/store/apps/details?id=org.zephyrsoft.sdbviewer)

If you find a bug or want a new feature, you are welcome to
[file an issue](https://github.com/mathisdt/sdbviewer/issues) or even fix things yourself
and create a pull request (see below).
Please don't try to communicate with me via reviews, that doesn't work in both directions.
You can also [write me an email](https://zephyrsoft.org/contact-about-me) and I'll see what I can do.

## Contributing

I'm a big fan of open source and very much welcome contributions, may it be error reports,
feature proposals or code.

If you want to build the app yourself, you can do so using
[Earthly](https://docs.earthly.dev/). This uses Docker, so be sure to have both Docker and
Earthly ready before calling `earthly +build` which will create a container with everything
needed for the build, create the app package(s) inside it and then copy the results to the
directory build/outputs/apk for you.

For real development, it is of course necessary to have everything installed locally, 
in that case the Earthfile (which describes the build process) can be used as blueprint
for the setup.
