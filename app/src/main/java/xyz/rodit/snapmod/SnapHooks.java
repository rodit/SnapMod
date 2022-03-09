package xyz.rodit.snapmod;

import static xyz.rodit.snapmod.Shared.SNAPMOD_MAPPINGS_DIR;
import static xyz.rodit.snapmod.Shared.SNAP_PACKAGE;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import xyz.rodit.dexsearch.client.Mappings;
import xyz.rodit.snapmod.mappings.AdInAppReportClient;
import xyz.rodit.snapmod.mappings.BitmojiUriHandler;
import xyz.rodit.snapmod.mappings.CalendarDate;
import xyz.rodit.snapmod.mappings.ChatActionHelper;
import xyz.rodit.snapmod.mappings.ChatMediaHandler;
import xyz.rodit.snapmod.mappings.ChatMediaInAppReportClient;
import xyz.rodit.snapmod.mappings.ChatModelAudioNote;
import xyz.rodit.snapmod.mappings.ChatModelBase;
import xyz.rodit.snapmod.mappings.ChatModelLiveSnap;
import xyz.rodit.snapmod.mappings.ChatModelSavedSnap;
import xyz.rodit.snapmod.mappings.ContentType;
import xyz.rodit.snapmod.mappings.ConversationManager;
import xyz.rodit.snapmod.mappings.DirectSnapInAppReportClient;
import xyz.rodit.snapmod.mappings.EncryptionAlgorithm;
import xyz.rodit.snapmod.mappings.FooterInfoItem;
import xyz.rodit.snapmod.mappings.FriendActionClient;
import xyz.rodit.snapmod.mappings.FriendActionRequest;
import xyz.rodit.snapmod.mappings.FriendProfilePageData;
import xyz.rodit.snapmod.mappings.FriendProfileTransformer;
import xyz.rodit.snapmod.mappings.FriendPublicProfileTile;
import xyz.rodit.snapmod.mappings.FriendStoryInAppReportClient;
import xyz.rodit.snapmod.mappings.GallerySnapMedia;
import xyz.rodit.snapmod.mappings.InAppReportManagerImpl;
import xyz.rodit.snapmod.mappings.LiveSnapMedia;
import xyz.rodit.snapmod.mappings.LocalMessageContent;
import xyz.rodit.snapmod.mappings.LocationMessage;
import xyz.rodit.snapmod.mappings.LocationMessageBuilder;
import xyz.rodit.snapmod.mappings.MainActivity;
import xyz.rodit.snapmod.mappings.MediaType;
import xyz.rodit.snapmod.mappings.MemoriesPickerVideoDurationConfig;
import xyz.rodit.snapmod.mappings.MessageMetadata;
import xyz.rodit.snapmod.mappings.MessageSenderCrossroad;
import xyz.rodit.snapmod.mappings.MessageUpdate;
import xyz.rodit.snapmod.mappings.NetworkApi;
import xyz.rodit.snapmod.mappings.OperaActionMenuOptionViewModel;
import xyz.rodit.snapmod.mappings.OperaContextActions;
import xyz.rodit.snapmod.mappings.OperaMediaInfo;
import xyz.rodit.snapmod.mappings.ParameterPackage;
import xyz.rodit.snapmod.mappings.ParamsMap;
import xyz.rodit.snapmod.mappings.PublicUserReportParams;
import xyz.rodit.snapmod.mappings.PublicUserStoryInAppReportClient;
import xyz.rodit.snapmod.mappings.PublisherStoryInAppReportClient;
import xyz.rodit.snapmod.mappings.RxObserver;
import xyz.rodit.snapmod.mappings.SavePolicy;
import xyz.rodit.snapmod.mappings.SaveToCameraRollActionHandler;
import xyz.rodit.snapmod.mappings.SaveType;
import xyz.rodit.snapmod.mappings.StoryMetadata;
import xyz.rodit.snapmod.mappings.TopicSnapInAppReportClient;
import xyz.rodit.snapmod.utils.StreamUtils;

public class SnapHooks implements IXposedHookLoadPackage {

    private static final String PROFILE_PICTURE_RESOLUTION_PATTERN = "0,\\d+_";

    private static String lastPublicProfilePictureUrl;

    private static Config config;

    private Context appContext;
    private ChatMediaHandler chatMediaHandler;
    private final Map<Integer, Object> chatMediaMap = new HashMap<>();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(SNAP_PACKAGE)) {
            return;
        }

        config = new Config();

        int versionCode = XposedHelpers.getStaticIntField(XposedHelpers.findClass(SNAP_PACKAGE + ".BuildConfig", lpparam.classLoader), "VERSION_CODE");
        File mappingsFile = new File(SNAPMOD_MAPPINGS_DIR, versionCode + ".json");
        if (!mappingsFile.isFile()) {
            XposedBridge.log("No mappings found for current build at " + mappingsFile + ".");
            return;
        }

        try (InputStream in = new FileInputStream(mappingsFile)) {
            Mappings.loadMappings(lpparam.classLoader, in);
        }

        // Disable camera
        XposedBridge.hookAllMethods(DevicePolicyManager.class, "getCameraDisabled", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.get("disable_camera")) {
                    param.setResult(true);
                }
            }
        });

        // Get application context
        MainActivity.attachBaseContext.hook(new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                appContext = (Context) param.thisObject;
                Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                    XposedBridge.log("Exception on thread " + thread.getName() + ", " + thread.getId());
                    XposedBridge.log(throwable);
                });
            }
        });

        // Prevent screenshot/save to gallery notifications
        ConversationManager.sendMessageWithContent.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                Object contentType = XposedHelpers.callMethod(param.args[1], "getContentType");
                config.prevent(param, "hide_screenshot", contentType, ContentType.STATUS_CONVERSATION_CAPTURE_RECORD(), ContentType.STATUS_CONVERSATION_CAPTURE_SCREENSHOT());
                config.prevent(param, "hide_save_gallery", contentType, ContentType.STATUS_SAVE_TO_CAMERA_ROLL());
            }
        });

        // Prevent message updates from sending (screenshots, replays etc)
        ConversationManager.updateMessage.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                Object update = param.args[2];
                config.prevent(param, "hide_read", update, MessageUpdate.READ());
                config.prevent(param, "hide_save", update, MessageUpdate.SAVE(), MessageUpdate.UNSAVE());
                config.prevent(param, "hide_screenshot", update, MessageUpdate.SCREENSHOT(), MessageUpdate.SCREEN_RECORD());
                config.prevent(param, "hide_replay", update, MessageUpdate.REPLAY());
                config.prevent(param, "dont_release", update, MessageUpdate.RELEASE());
            }
        });

        // Hide read status
        ConversationManager.displayedMessages.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.get("hide_read")) {
                    param.setResult(null);
                }
            }
        });

        // Hide conversation open
        ConversationManager.enterConversation.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.get("hide_enter_conversation")) {
                    param.setResult(null);
                }
            }
        });

        // Hide conversation close
        ConversationManager.exitConversation.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.get("hide_exit_conversation")) {
                    param.setResult(null);
                }
            }
        });

        // Hide Bitmoji typing status
        ConversationManager.sendTypingNotification.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.get("hide_typing")) {
                    param.setResult(null);
                }
            }
        });

        // Allow any local message content to be saved (maybe unnecessary)
        LocalMessageContent.getSavePolicy.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.get("save_any")) {
                    param.setResult(SavePolicy.LIFETIME().instance);
                }
            }
        });

        MessageMetadata.getIsSaveable.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.get("save_any")) {
                    param.setResult(true);
                }
            }
        });

        // Network requests logging
        XC_MethodHook networkHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.get("log_network_requests")) {
                    XposedBridge.log(param.args[0].toString());
                }
            }
        };

        NetworkApi.submit.hook(networkHook);
        NetworkApi.submitToNetworkManagerDirectly.hook(networkHook);

        // Obtain chat media handler instance
        ChatMediaHandler.constructors.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                chatMediaHandler = ChatMediaHandler.wrap(param.thisObject);
            }
        });

        MessageSenderCrossroad.apply.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.get("override_snap")) {
                    MessageSenderCrossroad $this = MessageSenderCrossroad.wrap(param.thisObject);
                    Object media = $this.getMedia();
                    if (GallerySnapMedia.isInstance(media)) {
                        String id = GallerySnapMedia.wrap(media).getMedia().getId();
                        LiveSnapMedia snap = new LiveSnapMedia();
                        double timer = Double.parseDouble(config.getPreferences().getString("override_snap_timer", "0"));
                        snap.setMediaId(id);

                        if (config.get("enable_snap_type_override")) {
                            String overrideType = config.getPreferences().getString("snap_type_override", "IMAGE");
                            snap.setMediaType(MediaType.valueOf(overrideType));
                        } else {
                            snap.setMediaType(MediaType.IMAGE());
                        }

                        ParameterPackage paramPackage = new ParameterPackage(timer == 0d, timer, null, null, null, null, null, null, null, null, null, null, false);
                        snap.setParameterPackage(paramPackage);
                        $this.setMedia(snap);
                    }
                }
            }
        });

        // Allow save action in chat long-press menu
        ChatActionHelper.canSave.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.get("allow_save_snaps")) {
                    param.setResult(true);
                }
            }
        });

        // Allow save action in chat model
        ChatModelBase.canSave.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.get("allow_save_snaps")) {
                    param.setResult(true);
                }
            }
        });

        // Override save type to gallery to allow saving any snaps
        ChatModelBase.getSaveType.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.get("allow_save_snaps")
                        && (ChatModelLiveSnap.isInstance(param.thisObject)
                        || ChatModelAudioNote.isInstance(param.thisObject))) {
                    XposedBridge.log("Overriding getSaveType on " + param.thisObject.getClass().getName());
                    param.setResult(SaveType.SNAPCHAT_ALBUM().instance);
                }
            }
        });

        // Map live snap model hashCode to media object for download later
        ChatModelLiveSnap.constructors.hook(new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (config.get("allow_save_snaps")) {
                    int hashCode = param.thisObject.hashCode();
                    chatMediaMap.put(hashCode, param.args[6]);
                }
            }
        });

        // Show more info in friend profile footer
        FriendProfileTransformer.apply.hook(new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (config.get("more_profile_info") && FriendProfilePageData.isInstance(param.args[0]) && param.getResult() instanceof List) {
                    Object viewModel = ((List<?>) param.getResult()).get(0);
                    if (FooterInfoItem.isInstance(viewModel)) {
                        FriendProfilePageData data = FriendProfilePageData.wrap(param.args[0]);
                        Date friendDate = new Date(Math.max(data.getAddedTimestamp(), data.getReverseAddedTimestamp()));
                        CalendarDate birthday = data.getBirthday();
                        if (birthday.isNull()) {
                            birthday = new CalendarDate(13, -1);
                        }

                        String info = "Friends with " +
                                data.getDisplayName() +
                                " since " +
                                SimpleDateFormat.getDateInstance().format(friendDate) +
                                ".\n" +
                                data.getDisplayName() +
                                "'s birthday is " +
                                birthday.getDay() +
                                " " +
                                Shared.MONTHS[birthday.getMonth() - 1] +
                                "\nFriendship: " +
                                data.getFriendLinkType().instance +
                                "\nAdded by: " +
                                data.getAddSourceTypeForNonFriend().instance;

                        FooterInfoItem.wrap(viewModel).setText(info);
                    }
                }
            }
        });

        // Export non-savable media (live snaps and voice notes)
        SaveToCameraRollActionHandler.exportMedia.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.get("allow_save_snaps")) {
                    if (ChatModelLiveSnap.isInstance(param.args[1])) {
                        // Convert live snap to saved snap
                        int hashCode = param.args[1].hashCode();
                        LiveSnapMedia media = LiveSnapMedia.wrap(chatMediaMap.get(hashCode));
                        ChatModelBase base = ChatModelBase.wrap(param.args[1]);
                        param.args[1] = new ChatModelSavedSnap(base.getContext(), base.getMessageData(), base.getSenderId(), Collections.emptyMap(), true, base.getReactionsViewModel(), true, 0, 0, media, null, base.getStatus(), true).instance;
                    } else if (ChatModelAudioNote.isInstance(param.args[1])) {
                        // Resolve audio uri and resolve through proxy of RxObserver.
                        Toast.makeText(appContext, "Downloading audio note...", Toast.LENGTH_SHORT).show();
                        ChatModelBase base = ChatModelBase.wrap(param.args[1]);
                        ChatModelAudioNote audio = ChatModelAudioNote.wrap(param.args[1]);
                        File dest = new File(Shared.SNAPMOD_MEDIA_DIR, Shared.SNAPMOD_MEDIA_PREFIX + base.getSenderId() + "_" + System.currentTimeMillis() + ".aac");
                        Object observerProxy = Proxy.newProxyInstance(lpparam.classLoader, new Class[]{RxObserver.getMappedClass()}, new UriResolverSubscriber.MediaUriDownloader(dest));
                        chatMediaHandler.resolve(audio.getUri(), Collections.emptySet(), true, Collections.emptySet()).subscribe(RxObserver.wrap(observerProxy));
                        param.setResult(null);
                    }
                }
            }
        });

        // Disable Bitmojis
        BitmojiUriHandler.handle.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.get("disable_bitmojis")) {
                    param.setResult(null);
                }
            }
        });

        // Override friend add method (not sure if this works)
        FriendActionClient.sendFriendAction.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.get("enable_friend_override")) {
                    FriendActionRequest request = FriendActionRequest.wrap(param.args[0]);
                    if (request.getAction().equals("add")) {
                        String addMethod = config.getPreferences().getString("friend_override_value", "ADDED_BY_USERNAME");
                        request.setAddedBy(addMethod);
                    }
                }
            }
        });

        // Bypass video duration limit from gallery in chat
        MemoriesPickerVideoDurationConfig.constructors.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.get("bypass_video_length_restrictions")) {
                    param.args[0] = Long.MAX_VALUE;
                }
            }
        });

        // Override location (not sure if this works)
        LocationMessageBuilder.transform.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.get("location_share_override")) {
                    double lat = Double.parseDouble(config.getPreferences().getString("location_share_lat", "0"));
                    double $long = Double.parseDouble(config.getPreferences().getString("location_share_long", "0"));
                    LocationMessage loc = LocationMessage.wrap(param.args[0]);
                    loc.setLatitude(lat);
                    loc.setLongitude($long);
                }
            }
        });

        // Get last viewed public profile url for download
        FriendPublicProfileTile.constructors.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                lastPublicProfilePictureUrl = FriendPublicProfileTile.wrap(param.thisObject).getProfilePictureUrl();
            }
        });

        InAppReportManagerImpl.handle.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.get("allow_download_public_dp") && lastPublicProfilePictureUrl != null && PublicUserReportParams.isInstance(param.args[0])) {
                    String resolution = config.getPreferences().getString("public_dp_resolution", "500");
                    double resDouble = Double.parseDouble(resolution);
                    if (resDouble < 1 || resDouble > 5000) {
                        resDouble = 500;
                    }
                    resolution = String.valueOf((int) resDouble);
                    String url = lastPublicProfilePictureUrl.replaceAll(PROFILE_PICTURE_RESOLUTION_PATTERN, "0," + resolution + "_");
                    PublicUserReportParams params = PublicUserReportParams.wrap(param.args[0]);
                    try {
                        new Thread(() -> downloadFile(params.getUsername() + "_profile_" + System.currentTimeMillis(), url)).start();
                    } catch (Exception e) {
                        XposedBridge.log(e);
                    }

                    param.setResult(null);
                }
            }
        });

        if (config.get("allow_download_stories")) {
            Class<?> clsOperaContextActions = OperaContextActions.getMappedClass();
            Field reportAction = null;
            Field saveAction = null;
            for (Field f : clsOperaContextActions.getDeclaredFields()) {
                Object field = f.get(null);
                if (OperaActionMenuOptionViewModel.isInstance(field)) {
                    OperaActionMenuOptionViewModel model = OperaActionMenuOptionViewModel.wrap(field);
                    String eventName = model.getEventName();
                    if (eventName == null) {
                        eventName = model.getActionMenuId().toString();
                    }
                    if (eventName.equals(StoryHelper.REPORT_EVENT_NAME)) {
                        reportAction = f;
                        XposedBridge.log("Found report action @ " + reportAction.getName());
                    } else if (eventName.equals(StoryHelper.SAVE_EVENT_NAME)) {
                        saveAction = f;
                        XposedBridge.log("Found save action @ " + saveAction.getName());
                    }
                }
            }

            if (reportAction != null && saveAction != null) {
                OperaActionMenuOptionViewModel saveActionModel = OperaActionMenuOptionViewModel.wrap(saveAction.get(null));
                OperaActionMenuOptionViewModel reportActionModel = OperaActionMenuOptionViewModel.wrap(reportAction.get(null));
                reportActionModel.setIconResource(saveActionModel.getIconResource());
                reportActionModel.setTextResource(saveActionModel.getTextResource());
                reportActionModel.setTextColorResource(saveActionModel.getTextColorResource());
                reportActionModel.setIsLoading(false);
                XposedBridge.log("Replaced report with save button.");
            }

            XC_MethodHook downloadHook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    OperaMediaInfo info = StoryHelper.getMediaInfo(ParamsMap.wrap(param.args[0]));
                    if (info != null && info.isNotNull()) {
                        //Toast.makeText(appContext, "Downloading story snap...", Toast.LENGTH_SHORT).show();

                        XposedBridge.log("Starting downloader thread...");
                        try {
                            new Thread(() -> downloadStorySnap(String.valueOf(System.nanoTime()), info)).start();
                        } catch (Exception e) {
                            XposedBridge.log(e);
                        }
                    } else {
                        XposedBridge.log("Null media info for story download.");
                    }

                    param.setResult(null);
                }
            };

            PublicUserStoryInAppReportClient.report.hook(downloadHook);
            FriendStoryInAppReportClient.report.hook(downloadHook);
            PublisherStoryInAppReportClient.report.hook(downloadHook);
            AdInAppReportClient.report.hook(downloadHook);
            ChatMediaInAppReportClient.report.hook(downloadHook);
            TopicSnapInAppReportClient.report.hook(downloadHook);
            DirectSnapInAppReportClient.report.hook(downloadHook);
        }
    }

    private void downloadStorySnap(String filePrefix, OperaMediaInfo media) {
        try {
            InputStream stream = new URL(media.getUri()).openStream();

            EncryptionAlgorithm enc = media.getEncryption();
            if (enc.isNotNull()) {
                XposedBridge.log("Stream is encrypted: " + enc);
                stream = enc.decryptStream(stream);
            }

            boolean video = media.getStreamingMethod().isNotNull() || (media.getUri() != null && media.getUri().endsWith("mp4"));

            File dest = new File(Shared.SNAPMOD_MEDIA_DIR, Shared.SNAPMOD_MEDIA_PREFIX + filePrefix + (video ? ".mp4" : ".jpg"));
            dest.getParentFile().mkdirs();

            XposedBridge.log("Downloading snap media to path " + dest);
            StreamUtils.copyTo(stream, dest);

            stream.close();
        } catch (Exception e) {
            XposedBridge.log("Error while downloading story snap.");
            XposedBridge.log(e);
        }
    }

    private void downloadFile(String filePrefix, String url) {
        try {
            InputStream stream = new URL(url).openStream();
            File dest = new File(Shared.SNAPMOD_MEDIA_DIR, Shared.SNAPMOD_MEDIA_PREFIX + filePrefix + ".jpg");
            dest.getParentFile().mkdirs();

            XposedBridge.log("Downloading file to " + dest);
            StreamUtils.copyTo(stream, dest);

            stream.close();
        } catch (Exception e) {
            XposedBridge.log("Error while downloading file.");
            XposedBridge.log(e);
        }
    }
}
