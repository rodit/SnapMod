package xyz.rodit.snapmod.features.saving;

import android.app.AlertDialog;
import android.view.View;

import de.robv.android.xposed.XC_MethodHook;
import xyz.rodit.snapmod.util.PathManager;
import xyz.rodit.snapmod.features.Feature;
import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.PublicProfileTile;
import xyz.rodit.snapmod.mappings.PublicProfileTileTransformer;

public class PublicProfileSaving extends Feature {

    private static final String PROFILE_PICTURE_RESOLUTION_PATTERN = "0,\\d+_";

    public PublicProfileSaving(FeatureContext context) {
        super(context);
    }

    @Override
    protected void performHooks() {
        // Hook friend public profile tile to allow download profile picture.
        PublicProfileTileTransformer.transform.hook(new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                View view = (View) PublicProfileTileTransformer.wrap(param.thisObject).getProfileImageView();
                PublicProfileTile tile = PublicProfileTile.wrap(param.args[0]);
                view.setOnLongClickListener(v -> {
                    if (context.config.getBoolean("allow_download_public_dp")) {
                        String url = tile.getProfilePictureUrl();
                        context.activity.runOnUiThread(() ->
                                new AlertDialog.Builder(context.activity)
                                        .setTitle("Download Profile Picture?")
                                        .setPositiveButton("Yes", (d, i) -> {
                                            String resolution = context.config.getString("public_dp_resolution", "500");
                                            double resDouble = Double.parseDouble(resolution);
                                            if (resDouble < 1 || resDouble > 5000) {
                                                resDouble = 500;
                                            }
                                            resolution = String.valueOf((int) resDouble);
                                            String resizedUrl = url.replaceAll(PROFILE_PICTURE_RESOLUTION_PATTERN, "0," + resolution + "_");
                                            String username = tile.getInfo().getMetadata().getUsername();
                                            String dest = PathManager.getUri(context.config, PathManager.DOWNLOAD_PROFILE, PathManager.createParams("u", username), ".jpg");
                                            context.files.download(context.config.getBoolean("use_android_download_manager"), resizedUrl, dest, username + "'s profile picture", null);
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
    }
}
