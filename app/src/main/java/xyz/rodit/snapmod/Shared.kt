package xyz.rodit.snapmod

object Shared {

    const val SNAPCHAT_PACKAGE = "com.snapchat.android"

    const val SNAPMOD_PACKAGE_NAME = "xyz.rodit.snapmod"
    const val SNAPMOD_CONFIG_ACTION = SNAPMOD_PACKAGE_NAME + ".ACTION_CONFIG"
    const val SNAPMOD_FILES_ACTION = SNAPMOD_PACKAGE_NAME + ".ACTION_FILES"
    const val SNAPMOD_FORCE_RESUME_ACTIVITY = SNAPMOD_PACKAGE_NAME + ".ForceResumeActivity"

    const val CONTEXT_HOOK_CLASS = "android.app.Application"
    const val CONTEXT_HOOK_METHOD = "attach"

    const val PINNED_FRIENDMOJI_NAME = "pinned"
    const val PINNED_FRIENDMOJI_EMOJI = "\uD83D\uDCCC"

    val MONTHS = arrayOf(
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December",
        "Unknown"
    )
}