package dev.local.noswipehaptics;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * SystemUI plays haptics through MSDLPlayer.playToken(token). On devices whose vibrator HAL has
 * no primitives, the continuous "drag indicator" token falls back to a stream of step waveforms,
 * which feels like rattling while swiping a notification.
 *
 * This hook drops only the tokens listed in BLOCKED. SWIPE_THRESHOLD_INDICATOR (the single tick
 * when the notification detaches/dismisses) is intentionally NOT blocked.
 * Every distinct token seen is logged once to the LSPosed module log so the list can be adjusted.
 */
public class HookEntry implements IXposedHookLoadPackage {

    private static final String TAG = "NoSwipeHaptics: ";

    private static final Set<String> BLOCKED = new HashSet<>(Arrays.asList(
            "DRAG_INDICATOR_CONTINUOUS",
            "DRAG_INDICATOR_DISCRETE"
    ));

    private static final Set<String> SEEN = new HashSet<>();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!"com.android.systemui".equals(lpparam.packageName)) return;

        Class<?> impl = XposedHelpers.findClassIfExists(
                "com.google.android.msdl.domain.MSDLPlayerImpl", lpparam.classLoader);
        if (impl == null) {
            XposedBridge.log(TAG + "MSDLPlayerImpl not found in SystemUI, nothing hooked");
            return;
        }

        Set<XC_MethodHook.Unhook> hooks = XposedBridge.hookAllMethods(impl, "playToken",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (param.args == null || param.args.length == 0 || param.args[0] == null) {
                            return;
                        }
                        String name = String.valueOf(param.args[0]);
                        boolean block = BLOCKED.contains(name);
                        synchronized (SEEN) {
                            if (SEEN.add(name)) {
                                XposedBridge.log(TAG + "token " + name + (block ? " -> blocked" : " -> allowed"));
                            }
                        }
                        if (block) {
                            param.setResult(null);
                        }
                    }
                });

        XposedBridge.log(TAG + "hooked " + hooks.size() + " playToken method(s)");
    }
}
