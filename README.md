# SnapMod
Xposed module for Snapchat.

## Setup
To set SnapMod up, download and install the latest apk from [here](https://github.com/rodit/SnapMod/releases). When you open it, it will ask to install some bindings. Press 'Download' and be sure to kill and restart Snapchat afterwards. The latest and only fully supported version of Snapchat is **11.73.0.35**. Mappings **will not** be downloaded for previous versions of Snapchat automatically, only for the latest supported version. If you are using an older version, you must manually place the mappings in `/data/data/xyz.rodit.snapmod/files/[build].json` or `/Android/data/xyz.rodit.snapmod/files/[build].json` on your internal storage where `[build]` is the version code of Snapchat the mappings correspond to. Note, there is no guarentee the newest version of Snapmod will work with old mappings (it usually will not for a couple of features).

## Features
- Disable camera
- Hide:
  - Read receipts
  - Chat saves/unsaves
  - Screenshot/screen recording notifications
  - Save to camera roll notifications
  - Conversation presence (Bitmoji in bottom left)
  - Typing status
- Save any media (including snaps and audio notes) to camera roll
- Disable Bitmojis
- Send snaps from gallery
- Download stories
- Bypass video length restriction from drawer in chat
- Show more info on profile (birthday, friendship status)

## Issues
If you have an issue please post a log from LSPosed (I have no interest in supporting other Xposed implementations, although SnapMod should work fine with them) and a description of the issue.
