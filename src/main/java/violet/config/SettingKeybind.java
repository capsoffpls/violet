package violet.config;

import com.mojang.blaze3d.platform.InputConstants;

public class SettingKeybind extends SettingInt {
    public SettingKeybind(int defaultValue, String key, String parentKey) {
        super(defaultValue, key, parentKey);
    }

    public SettingKeybind(int defaultValue, String key, Feature instance) {
        this(defaultValue, key, instance.key());
    }

    public int key() {
        return this.value();
    }

    public boolean bound() {
        return this.value() != InputConstants.UNKNOWN.getValue();
    }

    public boolean isKey(int key) {
        return key != InputConstants.UNKNOWN.getValue() && key == this.value();
    }
}