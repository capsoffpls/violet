package violet.features.render;

import static violet.Main.mc;

import meteordevelopment.orbit.EventHandler;
import violet.config.Feature;
import violet.config.SettingInt;
import violet.events.WorldTickEvent;

public class TimeChanger {
    public static final Feature instance = new Feature("timeChanger");
    public static final SettingInt time = new SettingInt(12000, "time", instance);

    // change to tick as it never updated with advance_time false
    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && mc.level != null) {
            mc.level.setTimeFromServer(time.get().getAsLong());
        }
    }
}
