import com.bridger.momentum.FocusAudioRule;

public final class FocusAudioRegression {
    private static void check(boolean value,String why){if(!value)throw new AssertionError(why);}
    public static void main(String[] args){
        check(!FocusAudioRule.mute(false,true,100,0),"Disabled Focus muted sounds");
        check(!FocusAudioRule.mute(true,false,100,0),"Focus muted interface audio");
        check(!FocusAudioRule.mute(true,true,0,9),"Own footsteps were muted");
        check(!FocusAudioRule.mute(true,true,25,100),"Own reachable block placement was muted");
        check(FocusAudioRule.mute(true,true,100,100),"Remote block placement was audible");
        check(FocusAudioRule.mute(true,true,9,0),"Nearby other-player footsteps were audible");
        System.out.println("PASS: Focus audio keeps local interactions and interface sounds while muting remote sources");
    }
}
