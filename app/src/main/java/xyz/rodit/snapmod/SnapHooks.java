package xyz.rodit.snapmod;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.View;

import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import xyz.rodit.dexsearch.client.xposed.MappedObject;
import xyz.rodit.snapmod.mappings.BitmojiUriHandler;
import xyz.rodit.snapmod.mappings.CalendarDate;
import xyz.rodit.snapmod.mappings.ChatActionHelper;
import xyz.rodit.snapmod.mappings.ChatMediaHandler;
import xyz.rodit.snapmod.mappings.ChatModelAudioNote;
import xyz.rodit.snapmod.mappings.ChatModelBase;
import xyz.rodit.snapmod.mappings.ChatModelLiveSnap;
import xyz.rodit.snapmod.mappings.ChatModelSavedSnap;
import xyz.rodit.snapmod.mappings.ContentType;
import xyz.rodit.snapmod.mappings.ContextActionMenuModel;
import xyz.rodit.snapmod.mappings.ContextClickHandler;
import xyz.rodit.snapmod.mappings.ConversationManager;
import xyz.rodit.snapmod.mappings.FooterInfoItem;
import xyz.rodit.snapmod.mappings.FriendActionClient;
import xyz.rodit.snapmod.mappings.FriendActionRequest;
import xyz.rodit.snapmod.mappings.FriendProfilePageData;
import xyz.rodit.snapmod.mappings.FriendProfileTransformer;
import xyz.rodit.snapmod.mappings.GallerySnapMedia;
import xyz.rodit.snapmod.mappings.LiveSnapMedia;
import xyz.rodit.snapmod.mappings.LocalMessageContent;
import xyz.rodit.snapmod.mappings.LocationMessage;
import xyz.rodit.snapmod.mappings.LocationMessageBuilder;
import xyz.rodit.snapmod.mappings.MainActivity;
import xyz.rodit.snapmod.mappings.MediaBaseBase;
import xyz.rodit.snapmod.mappings.MediaContainer;
import xyz.rodit.snapmod.mappings.MediaType;
import xyz.rodit.snapmod.mappings.MemoriesPickerVideoDurationConfig;
import xyz.rodit.snapmod.mappings.MessageMetadata;
import xyz.rodit.snapmod.mappings.MessageSenderCrossroad;
import xyz.rodit.snapmod.mappings.MessageUpdate;
import xyz.rodit.snapmod.mappings.NetworkApi;
import xyz.rodit.snapmod.mappings.OperaContextAction;
import xyz.rodit.snapmod.mappings.OperaContextActions;
import xyz.rodit.snapmod.mappings.ParameterPackage;
import xyz.rodit.snapmod.mappings.ParamsMap;
import xyz.rodit.snapmod.mappings.PublicProfileTile;
import xyz.rodit.snapmod.mappings.PublicProfileTileTransformer;
import xyz.rodit.snapmod.mappings.RxObserver;
import xyz.rodit.snapmod.mappings.SavePolicy;
import xyz.rodit.snapmod.mappings.SaveToCameraRollActionHandler;
import xyz.rodit.snapmod.mappings.SaveType;
import xyz.rodit.snapmod.mappings.SerializableContent;
import xyz.rodit.snapmod.mappings.StoryMetadata;
import xyz.rodit.xposed.HooksBase;
import xyz.rodit.xposed.mappings.LoadScheme;

public class SnapHooks extends HooksBase {

    private static final String PROFILE_PICTURE_RESOLUTION_PATTERN = "0,\\d+_";

    private final Map<Integer, Object> chatMediaMap = new HashMap<>();
    private ChatMediaHandler chatMediaHandler;

    private Activity mainActivity;

    public SnapHooks() {
        super(Collections.singletonList(Shared.SNAPCHAT_PACKAGE),
                EnumSet.of(LoadScheme.CACHED_ON_CONTEXT, LoadScheme.SERVICE),
                Shared.SNAPMOD_PACKAGE_NAME,
                Shared.SNAPMOD_CONFIG_ACTION,
                Shared.CONTEXT_HOOK_CLASS,
                Shared.CONTEXT_HOOK_METHOD);
    }

    @Override
    protected void onPackageLoad() {
        XposedBridge.hookAllMethods(DevicePolicyManager.class, "getCameraDisabled", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config == null || !config.isLoaded() || config.getBoolean("disable_camera")) {
                    param.setResult(true);
                }
            }
        });
    }

    @Override
    protected void onContextHook(Context context) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            XposedBridge.log("Uncaught exception on thread " + thread + ".");
            XposedBridge.log(throwable);
        });
    }

    @Override
    protected void onConfigLoaded(boolean first) {
        if (mainActivity != null) {
            new Handler(mainActivity.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent();
                intent.setClassName(Shared.SNAPMOD_PACKAGE_NAME, Shared.SNAPMOD_FORCE_RESUME_ACTIVITY);
                mainActivity.startActivity(intent);
            }, 500);
        }
    }

    @Override
    protected void performHooks() {
        requireFileService(Shared.SNAPMOD_FILES_ACTION);
        requireStreamServer(0);

        MainActivity.attachBaseContext.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                mainActivity = (Activity) param.thisObject;
            }
        });

        // Prevent screenshot/save to gallery notifications
        ConversationManager.sendMessageWithContent.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                Object contentType = XposedHelpers.callMethod(param.args[1], "getContentType");
                prevent(param, "hide_screenshot", contentType, ContentType.STATUS_CONVERSATION_CAPTURE_RECORD(), ContentType.STATUS_CONVERSATION_CAPTURE_SCREENSHOT());
                prevent(param, "hide_save_gallery", contentType, ContentType.STATUS_SAVE_TO_CAMERA_ROLL());
            }
        });

        // Prevent message updates from sending (screenshots, replays etc)
        ConversationManager.updateMessage.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                Object update = param.args[2];
                prevent(param, "hide_read", update, MessageUpdate.READ());
                prevent(param, "hide_save", update, MessageUpdate.SAVE(), MessageUpdate.UNSAVE());
                prevent(param, "hide_screenshot", update, MessageUpdate.SCREENSHOT(), MessageUpdate.SCREEN_RECORD());
                prevent(param, "hide_replay", update, MessageUpdate.REPLAY());
                prevent(param, "dont_release", update, MessageUpdate.RELEASE());
            }
        });

        // Hide read status
        ConversationManager.displayedMessages.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.getBoolean("hide_read")) {
                    param.setResult(null);
                }
            }
        });

        // Hide conversation open
        ConversationManager.enterConversation.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.getBoolean("hide_enter_conversation")) {
                    param.setResult(null);
                }
            }
        });

        // Hide conversation close
        ConversationManager.exitConversation.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.getBoolean("hide_exit_conversation")) {
                    param.setResult(null);
                }
            }
        });

        // Hide Bitmoji typing status
        ConversationManager.sendTypingNotification.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.getBoolean("hide_typing")) {
                    param.setResult(null);
                }
            }
        });

        // Allow any local message content to be saved (maybe unnecessary)
        LocalMessageContent.getSavePolicy.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.getBoolean("save_any")) {
                    param.setResult(SavePolicy.LIFETIME().instance);
                }
            }
        });

        MessageMetadata.getIsSaveable.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.getBoolean("save_any")) {
                    param.setResult(true);
                }
            }
        });

        // Network requests logging
        XC_MethodHook networkHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.getBoolean("log_network_requests")) {
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
                if (config.getBoolean("override_snap")) {
                    MessageSenderCrossroad $this = MessageSenderCrossroad.wrap(param.thisObject);
                    MediaContainer container = $this.getPayload().getMedia();
                    if (SerializableContent.isInstance(container.instance)) {
                        SerializableContent content = SerializableContent.wrap(container.instance);
                        MediaBaseBase message = content.getMessage();
                        if (GallerySnapMedia.isInstance(message.instance)) {
                            String id = GallerySnapMedia.wrap(message.instance).getMedia().getId();
                            LiveSnapMedia snap = new LiveSnapMedia();
                            double timer = Double.parseDouble(config.getString("override_snap_timer", "0"));
                            snap.setMediaId(id);

                            if (config.getBoolean("enable_snap_type_override")) {
                                String overrideType = config.getString("snap_type_override", "IMAGE");
                                snap.setMediaType(MediaType.valueOf(overrideType));
                            } else {
                                snap.setMediaType(MediaType.IMAGE());
                            }

                            ParameterPackage paramPackage = new ParameterPackage(timer == 0d, timer, null, null, null, null, null, null, null, null, null, null, false);
                            snap.setParameterPackage(paramPackage);
                            content.setMessage(MediaBaseBase.wrap(snap.instance));
                        }
                    }
                }
            }
        });

        // Allow save action in chat long-press menu
        ChatActionHelper.canSave.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.getBoolean("allow_save_snaps")) {
                    param.setResult(true);
                }
            }
        });

        // Allow save action in chat model
        ChatModelBase.canSave.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.getBoolean("allow_save_snaps")) {
                    param.setResult(true);
                }
            }
        });

        // Override save type to gallery to allow saving any snaps
        ChatModelBase.getSaveType.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.getBoolean("allow_save_snaps")
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
                if (config.getBoolean("allow_save_snaps")) {
                    int hashCode = param.thisObject.hashCode();
                    chatMediaMap.put(hashCode, param.args[6]);
                }
            }
        });

        // Show more info in friend profile footer
        FriendProfileTransformer.apply.hook(new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (config.getBoolean("more_profile_info") && FriendProfilePageData.isInstance(param.args[0]) && param.getResult() instanceof List) {
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
                if (config.getBoolean("allow_save_snaps")) {
                    if (ChatModelLiveSnap.isInstance(param.args[1])) {
                        // Convert live snap to saved snap
                        int hashCode = param.args[1].hashCode();
                        LiveSnapMedia media = LiveSnapMedia.wrap(chatMediaMap.get(hashCode));
                        ChatModelBase base = ChatModelBase.wrap(param.args[1]);
                        param.args[1] = new ChatModelSavedSnap(base.getContext(), base.getMessageData(), base.getSenderId(), Collections.emptyMap(), true, base.getReactionsViewModel(), true, 0, 0, media, null, base.getStatus(), true, true).instance;
                    } else if (ChatModelAudioNote.isInstance(param.args[1])) {
                        // Resolve audio uri and resolve through proxy of RxObserver.
                        // Note: the content resolver provided by appContext cannot open a stream from the uri.
                        ChatModelBase base = ChatModelBase.wrap(param.args[1]);
                        ChatModelAudioNote audio = ChatModelAudioNote.wrap(param.args[1]);
                        String dest = PathManager.getUri(config, PathManager.DOWNLOAD_AUDIO_NOTE, PathManager.createParams("id", base.getSenderId()), ".aac");
                        XposedBridge.log("Downloading audio note from " + audio.getUri() + " to " + dest + ".");
                        Object observerProxy = Proxy.newProxyInstance(lpparam.classLoader, new Class[]{RxObserver.getMappedClass()}, new UriResolverSubscriber.MediaUriDownloader(appContext, config, files, server, dest));
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
                if (config.getBoolean("disable_bitmojis")) {
                    param.setResult(null);
                }
            }
        });

        // Override friend add method (not sure if this works)
        FriendActionClient.sendFriendAction.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.getBoolean("enable_friend_override")) {
                    FriendActionRequest request = FriendActionRequest.wrap(param.args[0]);
                    if (request.getAction().equals("add")) {
                        String addMethod = config.getString("friend_override_value", "ADDED_BY_USERNAME");
                        request.setAddedBy(addMethod);
                    }
                }
            }
        });

        // Bypass video duration limit from gallery in chat
        MemoriesPickerVideoDurationConfig.constructors.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.getBoolean("bypass_video_length_restrictions")) {
                    param.args[0] = Long.MAX_VALUE;
                }
            }
        });

        // Override location (not sure if this works)
        LocationMessageBuilder.transform.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.getBoolean("location_share_override")) {
                    double lat = Double.parseDouble(config.getString("location_share_lat", "0"));
                    double $long = Double.parseDouble(config.getString("location_share_long", "0"));
                    LocationMessage loc = LocationMessage.wrap(param.args[0]);
                    loc.setLatitude(lat);
                    loc.setLongitude($long);
                }
            }
        });

        // Public profile picture downloads for friends
        PublicProfileTileTransformer.transform.hook(new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                View view = (View) PublicProfileTileTransformer.wrap(param.thisObject).getProfileImageView();
                PublicProfileTile tile = PublicProfileTile.wrap(param.args[0]);
                view.setOnLongClickListener(v -> {
                    if (config.getBoolean("allow_download_public_dp")) {
                        String url = tile.getProfilePictureUrl();
                        mainActivity.runOnUiThread(() ->
                                new AlertDialog.Builder(mainActivity)
                                        .setTitle("Download Profile Picture?")
                                        .setPositiveButton("Yes", (d, i) -> {
                                            String resolution = config.getString("public_dp_resolution", "500");
                                            double resDouble = Double.parseDouble(resolution);
                                            if (resDouble < 1 || resDouble > 5000) {
                                                resDouble = 500;
                                            }
                                            resolution = String.valueOf((int) resDouble);
                                            String resizedUrl = url.replaceAll(PROFILE_PICTURE_RESOLUTION_PATTERN, "0," + resolution + "_");
                                            String username = tile.getInfo().getMetadata().getUsername();
                                            String dest = PathManager.getUri(config, PathManager.DOWNLOAD_PROFILE, PathManager.createParams("u", username), ".jpg");
                                            files.download(config.getBoolean("use_android_download_manager"), resizedUrl, dest, username + "'s profile picture", null);
                                        })
                                        .setNegativeButton("No", (d, i) -> {
                                        })
                                        .show());
                        return true;
                    }

                    return false;
                });
            }
        });

        // Story menu creation (add save option).
        ParamsMap.put.hook(new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (config.getBoolean("allow_download_stories")
                        && param.args[0] == StoryMetadata.getActionMenuOptions().instance
                        && param.args[1] instanceof List) {
                    List newList = new ArrayList();
                    for (Object i : ((List) param.args[1])) {
                        newList.add(i);
                    }

                    newList.add(OperaContextActions.getSaveAction().instance);
                    param.args[1] = newList;
                }
            }
        });

        // Override save story click.
        ContextActionMenuModel.constructors.hook(new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                ContextActionMenuModel model = ContextActionMenuModel.wrap(param.thisObject);
                if (config.getBoolean("allow_download_stories")
                        && model.getAction().instance == OperaContextAction.SAVE().instance) {
                    Object clickProxy = Proxy.newProxyInstance(lpparam.classLoader, new Class[]{ContextClickHandler.getMappedClass()}, new StoryDownloadProxy(appContext, config, server, files));
                    model.setOnClick(ContextClickHandler.wrap(clickProxy));
                }
            }
        });
    }

    private void prevent(XC_MethodHook.MethodHookParam param, String pref, Object enumObj, Object... enumVals) {
        if (config.getBoolean(pref)) {
            for (Object value : enumVals) {
                if (value instanceof MappedObject) {
                    value = ((MappedObject) value).instance;
                }

                if (enumObj.equals(value)) {
                    param.setResult(null);
                    break;
                }
            }
        }
    }
}
