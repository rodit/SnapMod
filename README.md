<h1 align="center">
  <br>
  SnapMod
  <br>
</h1>

<h4 align="center">Simple to use Xposed module for Snapchat 👻</h4>

<p align="center">
  <a href="https://github.com/rodit/snapmod/releases">
    <img src="https://img.shields.io/github/v/release/rodit/snapmod?style=for-the-badge"></a>
  <a href="https://github.com/rodit/snapmod/releases"><img src="https://img.shields.io/github/downloads/rodit/snapmod/total?style=for-the-badge"></a>
  <a href="https://github.com/rodit/snapmod/stargazers">
    <img src="https://img.shields.io/github/stars/rodit/snapmod?style=for-the-badge"></a>
  <a href="#donate">
    <img src="https://img.shields.io/badge/$-donate-ff69b4.svg?maxAge=2592000&amp;style=for-the-badge">
  </a>
</p>

<p align="center">
  <a href="#how-to-use">How To Use</a> •
  <a href="#key-features">Key Features</a> •
  <a href="#issues">Issue reporting</a> •
  <a href="#feature-request">Feature Requests</a>
</p>


# How To Use

To use this Xposed module, you'll need a rooted or a non-rooted phone (😱). 

## For both
* Please install a supported Snapchat version ([APKMirror](https://apkmirror.com/apk/snap-inc/snapchat/) for example), currently the supported one is **11.96.0.31** for SnapMod 1.8.5. We do not offer official (You might get in GitHub issues) support for versions that are behind either one.
* > **Note** If you are an android developer or something similar, you can create mappings yourself that might work for higher/lower Snapchat versions. You can follow the steps in `.github/workflows`
* > **Note** If you have obtained custom mappings, move them in `/(Android|data)/data/xyz.rodit.snapmod/files/[build].json`. Build numbers you can obtain from APKMirror, if they are not provided. With a good file explorer (Like MiXplorer or File Manager Plus) non-rooted users can do this as well.

* > **Warning** We might not offer support for non-rooted users


## Installation for non-rooted users 
1. Download and install [LSPatch](https://github.com/LSPosed/LSPatch) and [Shizuku](https://github.com/RikkaApps/Shizuku)
2. Download and install [app-release.apk](https://github.com/rodit/SnapMod/releases/download/v1.8.5/app-release.apk) from latest release
3. Turn on Shizuku and open LSPatch and go to the second tab from the left
4. Press the plus sign and search Snapchat and select it
5. Choose the the Patch Mode to be Portable
6. Click on Embed modules and select SnapMod and tap Start Patch
7. Open SnapMod and verify that it says "Supported version". If it doesn't, try again. Make sure you have the correct mappings!

## Installation for root users
1. Download and install [LSPosed Framework](https://github.com/LSPosed/LSPosed). We do not offer support for other Xposed implementatios although SnapMod would work fine with them.
2. Download and install [app-release.apk](https://github.com/rodit/SnapMod/releases/download/v1.8.5/app-release.apk) from latest release.
3. Enable SnapMod in LSPosed manager and close Snapchat completely.
5. Open SnapMod and verify that it says "Supported version". If it doesn't, please see <a href="#issues">Issues</a>



## Key Features
> **Note** Full feature list and their status is available [here](https://github.com/rodit/SnapMod/wiki/Features)! More than 30 features available!
* Download Snaps and stories!
  - Save them automatically too!
  - Customizable behaviour
* Privacy
  - Disable notifications for certain activities for other people and Snapchat's metrics and logging
* Tweaks
  - Change how parts of Snapchat work, like removing ads or disabling the Spotlight tab
* Camera
  - Change image settings
  - Change video settings
* Snaps
  - Change the behaviour of incoming snaps
* Notifications
  - Incoming notification changing
* Miscellanous
  - Additional user info
  - Module update checking

# Issues
> **Warning** Please do not create feature requests or common questions in GitHub Issues, use **Discussions**.

## Troubleshooting
If nothing works, the first thing to do is: kill the Snapchat app process through system settings and after that relaunch it and do this twice. This will make sure any cached mappings are updated.

## GitHub Issues
If you have encountered an issue: First check Github Issues if others have experienced it if not, please get logs from LSPosed and upload them while creating a new issue. Please describe the bug, give us steps to reproduce, screenshots, expected behaviour and any additional context you might have.

# Feature request
If you have an idea for a new feature, please check **Discussions** tab first if it has been requested already. If it has not, create one!

# Donate
If you would like to support the deveploment of SnapMod, you can do it by donating
* [PayPal](https://paypal.me/roditmod) 
* [Cashapp](https://cash.app/rodit9)

## Crypto
  - BTC: bc1qr06chdv85jf9v7ldf7l24lrgp6ad7av8y7jwyc
  - ETH: 0x90659C0556b37107359FA32b40AA74c593590E04
  - XRP: rDgNCbi4eCeczpzGHGs3XHsR5C3SyUCr5r
