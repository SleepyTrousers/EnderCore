//package com.enderio.core.common.event;
//
//import net.minecraftforge.fml.client.event.ConfigChangedEvent;
//
///**
// * This event is posted to the FML bus when the /reloadConfigs command is run,
// * and should be used to reload your config file and parse its contents.
// * <p>
// * This differs from the existing events because you should call config.load()
// * before re-parsing the contents, to reload the file from disk.
// * <p>
// * This event will be sent once to all mods on load to determine if they are
// * valid candidates for receiving the event.
// * <p>
// * Note that requiresMcRestart is always false, so this might not work 100% of
// * the time
// * <p>
// * <b>If <code>setSuccessful()</code> is not called, the event post will be
// * considered a failure!</b>
// */
//public class ConfigFileChangedEvent extends ConfigChangedEvent {
//  private boolean successful;
//
//  public ConfigFileChangedEvent(String modID) {
//    super(modID, "null", true, false);
//  }
//
//  public boolean isSuccessful() {
//    return this.successful;
//  }
//
//  public void setSuccessful() {
//    this.successful = true;
//  }
//}
