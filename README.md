# SnapMod
Xposed module for Snapchat.

## Setup
To set SnapMod up, download and install the latest apk from [here](https://github.com/rodit/SnapMod/releases). When you open it, it will ask to install some bindings. Press 'Download' and be sure to kill and restart Snapchat afterwards. The latest and only fully supported version of Snapchat is **11.76.1.34**. Mappings **will not** be downloaded for previous versions of Snapchat automatically, only for the latest supported version. If you are using an older version, you must manually place the mappings in `/data/data/xyz.rodit.snapmod/files/[build].json` or `/Android/data/xyz.rodit.snapmod/files/[build].json` on your internal storage where `[build]` is the version code of Snapchat the mappings correspond to. Note, there is no guarentee the newest version of Snapmod will work with old mappings (it usually will not for a couple of features).

## Features
- Disable camera
- Hide:
  - Read receipts
  - Chat saves/unsaves
  - Screenshot/screen recording notifications
  - Save to camera roll notifications
  - Conversation presence (Bitmoji in bottom left)
  - Typing status
  - Story views
  - Snap views
- Block ads
- Pin conversations
- Save any media (including snaps and audio notes) to camera roll
- Disable Bitmojis
- Send snaps from gallery
- Download stories
- Bypass video length restriction from drawer in chat
- Show more info on profile (birthday, friendship status)
- Disable some metrics/logging
- Per-conversation stealth mode (hide read receipts, snap views etc for individual conversations)

## Issues
If you have an issue please post a log from LSPosed (I have no interest in supporting other Xposed implementations, although SnapMod should work fine with them) and a description of the issue. It is **extremely** unhelpful to just say "something doesn't work" or "I can't download stories."

## Troubleshooting
If anything doesn't work, the first thing you should try is killing and relaunching Snapchat (through the system settings app, not recent app list) **TWICE** and then trying again. This will ensure any cached mappings are updated. This is especially important after updating SnapMod and its mappings.

## Donations
Although this is a personal project I do entirely for fun (and it will most likely stay that way), if you would like to support the development of SnapMod, you can do so by donating:
- PayPal: https://paypal.me/roditmod
- Cashapp: https://cash.app/rodit9
- BTC: bc1qr06chdv85jf9v7ldf7l24lrgp6ad7av8y7jwyc
- ETH: 0x90659C0556b37107359FA32b40AA74c593590E04
- XRP: rDgNCbi4eCeczpzGHGs3XHsR5C3SyUCr5r
