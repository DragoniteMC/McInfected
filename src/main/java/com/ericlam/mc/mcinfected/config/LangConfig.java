package com.ericlam.mc.mcinfected.config;

import com.dragonite.mc.dnmc.core.config.yaml.MessageConfiguration;
import com.dragonite.mc.dnmc.core.config.yaml.Prefix;
import com.dragonite.mc.dnmc.core.config.yaml.Resource;

@Prefix(path = "prefix")
@Resource(locate = "lang.yml")
public class LangConfig extends MessageConfiguration {
}
