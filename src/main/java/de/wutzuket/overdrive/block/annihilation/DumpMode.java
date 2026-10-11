package de.wutzuket.overdrive.block.annihilation;

import net.minecraft.network.chat.Component;

/** Dump-Modus für den Steam-/Heißcoolant-Output, wie bei Mekanism. */
public enum DumpMode {
    IDLE("idle"),
    DUMP("dump"),
    DUMP_EXCESS("dump_excess");

    private final String key;
    DumpMode(String key){ this.key=key; }
    public String key(){ return key; }
    public DumpMode next(){ return values()[(ordinal()+1)%values().length]; }
    public Component displayName(){ return Component.translatable("dumpmode.overdrive."+key); }
    public static DumpMode fromKey(String k){
        for(DumpMode m: values()) if(m.key.equals(k)) return m;
        return IDLE;
    }
}
