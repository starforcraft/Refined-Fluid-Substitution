package com.ultramega.refinedfluidsubstitution.fabric;

import com.ultramega.refinedfluidsubstitution.common.Config;
import com.ultramega.refinedfluidsubstitution.common.FluidSubstitutionIdentifierUtil;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;

@me.shedaniel.autoconfig.annotation.Config(name = FluidSubstitutionIdentifierUtil.MOD_ID)
@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "CanBeFinal"})
public class ConfigImpl implements ConfigData, Config {
    public static ConfigImpl get() {
        return AutoConfig.getConfigHolder(ConfigImpl.class).getConfig();
    }
}
