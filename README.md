# SnapMod
Xposed module for Snapchat.

## Setup
To set SnapMod up, download and install the latest apk from [here](https://github.com/rodit/SnapMod/releases) and the corresponding `bindings.json`. Rename `bindings.json` to `[build].json` where `[build]` is the currently supported build of Snapchat and place it in the `/Android/data/xyz.rodit.snapmod/files` directory on your internal storage.

At the time of writing, the latest supported Snapchat version is `11.72.0.32` which has build number `84585` (so you would rename `bindings.json` to `84585.json`). You can find versions and corresponding build numbers on APKMirror or by installing Snapchat and running

`adb shell dumpsys package com.snapchat.android | grep versionCode`

on your development machine with your device plugged in.

Once the bindings have been installed, just launch SnapMod and configure it as you like. Configuration changes should be broadcast to the Snapchat app if it is already running but if they are not, just kill Snapchat and start it again.
