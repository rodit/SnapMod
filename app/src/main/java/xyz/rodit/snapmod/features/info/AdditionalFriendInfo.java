package xyz.rodit.snapmod.features.info;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import xyz.rodit.snapmod.Shared;
import xyz.rodit.snapmod.features.Feature;
import xyz.rodit.snapmod.features.FeatureContext;
import xyz.rodit.snapmod.mappings.CalendarDate;
import xyz.rodit.snapmod.mappings.FooterInfoItem;
import xyz.rodit.snapmod.mappings.FriendProfilePageData;
import xyz.rodit.snapmod.mappings.FriendProfileTransformer;

public class AdditionalFriendInfo extends Feature {

    public AdditionalFriendInfo(FeatureContext context) {
        super(context);
    }

    @Override
    protected void performHooks() {
        // Show more info in friend profile footer
        FriendProfileTransformer.apply.hook(new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (context.config.getBoolean("more_profile_info") && FriendProfilePageData.isInstance(param.args[0]) && param.getResult() instanceof List) {
                    List viewModelList = (List) param.getResult();
                    if (viewModelList.isEmpty()) {
                        return;
                    }

                    Object viewModel = viewModelList.get(0);
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
    }
}
