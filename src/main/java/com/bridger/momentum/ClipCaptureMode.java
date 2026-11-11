package com.bridger.momentum;

/** A failed optional GPU path must not prevent the buffer from being enabled. */
public final class ClipCaptureMode {
    private boolean async=true,framebuffer=true;
    public boolean async(boolean supported){return supported&&async&&framebuffer;}
    public boolean framebuffer(boolean supported){return supported&&framebuffer;}
    public boolean failed(boolean usedAsync,boolean usedFramebuffer){if(usedAsync){async=false;return true;}if(usedFramebuffer){framebuffer=false;return true;}return false;}
}
