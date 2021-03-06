package me.zpp0196.qqsimple.hook;

import android.view.View;
import android.widget.LinearLayout;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import me.zpp0196.qqsimple.hook.base.BaseHook;
import me.zpp0196.qqsimple.hook.util.Util;

import static me.zpp0196.qqsimple.hook.comm.Classes.AbstractChatItemBuilder$ViewHolder;
import static me.zpp0196.qqsimple.hook.comm.Classes.BaseBubbleBuilder$ViewHolder;
import static me.zpp0196.qqsimple.hook.comm.Classes.BubbleManager;
import static me.zpp0196.qqsimple.hook.comm.Classes.ChatMessage;
import static me.zpp0196.qqsimple.hook.comm.Classes.EmoticonManager;
import static me.zpp0196.qqsimple.hook.comm.Classes.GatherContactsTips;
import static me.zpp0196.qqsimple.hook.comm.Classes.GrayTipsItemBuilder;
import static me.zpp0196.qqsimple.hook.comm.Classes.ItemBuilderFactory;
import static me.zpp0196.qqsimple.hook.comm.Classes.MedalNewsItemBuilder;
import static me.zpp0196.qqsimple.hook.comm.Classes.MessageForDeliverGiftTips;
import static me.zpp0196.qqsimple.hook.comm.Classes.MessageRecord;
import static me.zpp0196.qqsimple.hook.comm.Classes.OnLongClickAndTouchListener;
import static me.zpp0196.qqsimple.hook.comm.Classes.QzoneFeedItemBuilder;
import static me.zpp0196.qqsimple.hook.comm.Classes.RichStatItemBuilder;
import static me.zpp0196.qqsimple.hook.comm.Classes.SougouInputGrayTips;
import static me.zpp0196.qqsimple.hook.comm.Classes.TextItemBuilder;
import static me.zpp0196.qqsimple.hook.comm.Classes.TextPreviewActivity;
import static me.zpp0196.qqsimple.hook.comm.Classes.TroopEnterEffectController;
import static me.zpp0196.qqsimple.hook.comm.Classes.TroopGiftAnimationController;
import static me.zpp0196.qqsimple.hook.comm.Classes.VipSpecialCareGrayTips;

/**
 * Created by zpp0196 on 2018/3/11.
 */

class ChatInterfaceHook extends BaseHook {

    ChatInterfaceHook() {
        hideChatBubble();
        hideFontEffects();
        hideRecommendedExpression();
        hideTAI();
        hideGroupGiftAnim();
        hideGroupChatAdmissions();
    }

    /**
     * 隐藏个性气泡
     */
    private void hideChatBubble() {
        if (getBool("hide_chat_bubble")) findAndHookMethod(BubbleManager, "a", int.class, boolean.class, XC_MethodReplacement.returnConstant(null));
    }

    /**
     * 隐藏字体特效
     */
    private void hideFontEffects() {
        if (!getBool("hide_font_effects")) return;
        findAndHookMethod(TextItemBuilder, "a", BaseBubbleBuilder$ViewHolder, ChatMessage, XC_MethodReplacement.returnConstant(null));
        findAndHookMethod(TextPreviewActivity, "a", int.class, XC_MethodReplacement.returnConstant(null));
    }

    /**
     * 隐藏推荐表情
     */
    private void hideRecommendedExpression() {
        if (Util.isMoreThan732() && getBool("hide_recommended_expression")) findAndHookMethod(EmoticonManager, "a", boolean.class, int.class, boolean.class, XC_MethodReplacement.returnConstant(new ArrayList<>()));
    }

    private void hideTAI() {
        // 隐藏好友获得了新徽章
        hideItemBuilder(MedalNewsItemBuilder, Util.isMoreThan732() && getBool("hide_get_new_badge"));
        // 隐藏好友新动态
        hideItemBuilder(QzoneFeedItemBuilder, getBool("hide_new_feed"));
        // 隐藏好友新签名
        hideItemBuilder(RichStatItemBuilder, getBool("hide_new_signature"));
        // 隐藏取消隐藏不常用联系人提示
        hideGrayTips(GatherContactsTips, getBool("hide_chat_unusual_contacts"));
        // 隐藏设置特别消息提示音
        hideGrayTips(VipSpecialCareGrayTips, getBool("hide_chat_special_care"));
        // 隐藏搜狗输入法广告
        hideGrayTips(SougouInputGrayTips, getBool("hide_chat_sougou_input"));
        // 隐藏会员相关广告
        hideGrayTipsItem(getBool("hide_chat_vip_ad"), ".+会员.+");
        // 签到文本化
        simpleItem(getBool("simple_group_sign"), 71, 84);
        // 隐藏加入群提示
        hideGrayTipsItem(getBool("hide_group_join_tips"), ".+加入了本群.+", ".+邀请.+加入.+");
        // 隐藏获得新头衔
        hideGrayTipsItem(getBool("hide_group_member_level_tips"), ".+获得.+头衔.+");
        // 隐藏礼物相关提示
        hideGrayTipsItem(getBool("hide_group_gift_tips"), ".+礼物.+成为.+守护.+", ".+成为.+魅力.+", ".+成为.+豪气.+", ".+送给.+朵.+");
    }

    /**
     * 隐藏礼物动画
     */
    private void hideGroupGiftAnim() {
        if (getBool("hide_group_gift_anim")) findAndHookMethod(TroopGiftAnimationController, "a", MessageForDeliverGiftTips, XC_MethodReplacement.returnConstant(null));
    }

    /**
     * 隐藏群聊入场动画
     */
    private void hideGroupChatAdmissions() {
        if (!getBool("hide_group_chat_admissions")) return;
        hideGrayTipsItem(true, ".+进场.+");
        if (TroopEnterEffectController == null) return;
        Field[] fields = TroopEnterEffectController.getDeclaredFields();
        for (Field field : fields) {
            if (field.toString().contains("final")) {
                field.setAccessible(true);
                try {
                    field.set(null, "");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void simpleItem(boolean isSimple, int... rs) {
        if (isSimple) findAndHookMethod(ItemBuilderFactory, "a", ChatMessage, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                int result = (int) param.getResult();
                for (int i : rs) {
                    if (i == result) {
                        param.setResult(-1);
                    }
                }
            }
        });
    }

    private void hideItemBuilder(Class<?> ItemBuilder, boolean isHide) {
        if (!isHide) findAndHookMethod(ItemBuilder, "a", MessageRecord, AbstractChatItemBuilder$ViewHolder, View.class, LinearLayout.class, OnLongClickAndTouchListener, XC_MethodReplacement.returnConstant(null));
    }

    private void hideGrayTips(Class<?> Tips, boolean isHide) {
        if (isHide) findAndHookMethod(Tips, "a", Object[].class, XC_MethodReplacement.returnConstant(null));
    }

    private void hideGrayTipsItem(boolean isHide, String... regex) {
        if (isHide) findAndHookMethod(GrayTipsItemBuilder, "a", MessageRecord, AbstractChatItemBuilder$ViewHolder, View.class, LinearLayout.class, OnLongClickAndTouchListener, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Field field = XposedHelpers.findFieldIfExists(MessageRecord, "msg");
                if (field == null) return;
                String msg = (String) field.get(param.args[0]);
                if (msg == null) return;
                for (String str : regex) {
                    if (Pattern.matches(str, msg)) {
                        param.setResult(null);
                    }
                }
            }
        });
    }
}
