import com.bridger.momentum.*;

public class BridgeDetectionRegression {
    static void check(boolean value,String why){if(!value)throw new AssertionError(why);}
    static String late(int index,int opening,int ticks,double x,double y,double z,double tx,double ty,double tz,boolean axis){return BridgeDetection.lateLabel(index,opening,ticks,x,y,z,tx,ty,tz,axis,true);}
    public static void main(String[] args){
        RunStartCooldown cooldown=new RunStartCooldown();check(cooldown.ready(0),"First run has no cooldown");cooldown.finish(100);
        for(int tick=100;tick<105;tick++)check(!cooldown.ready(tick),"Placements cannot restart a bridge before tick 105");
        check(cooldown.ready(105),"The next bridge is allowed exactly five ticks after completion");
        cooldown.finish(105);check(!cooldown.ready(109)&&cooldown.ready(110),"Each completed attempt starts a fresh cooldown");
        cooldown.reset();check(cooldown.ready(0),"Explicit reset clears cooldown");
        check("1.0 Flat".equals(BridgeDetection.flatLabel(3,1.20)),"Three ticks override a delayed scoreboard reading");
        check(!"1.0 Flat".equals(BridgeDetection.flatLabel(2,1.0)),"A scoreboard time alone cannot create a three-tick flat");
        check("1.05 Flat".equals(BridgeDetection.flatLabel(4,1.0)),"Existing four-tick flat remains distinct");
        check("1.1 Flat".equals(BridgeDetection.flatLabel(5,1.1)),"Existing flat labels remain supported");
        check("4b Flat".equals(BridgeDetection.flatLabel(12,1.05)),"Four-block flat still supported");
        check("5b Late".equals(late(1,3,9,.5,70,.5,3.5,71,.5,true)),"1.0 flat followed by a three-column one-high opening");
        check("6b Late".equals(late(1,4,9,.5,70,.5,.5,71,3.5,false)),"1.05 flat produces 6b late");
        check("5b Late".equals(late(1,3,9,-.5,70,-.5,-3.5,71,-.5,true)),"Negative X geometry uses floor, not truncation");
        check("6b Late".equals(late(1,4,9,-.5,70,-.5,-.5,71,-3.5,false)),"Negative Z opening");
        check(late(2,3,9,.5,70,.5,3.5,71,.5,true)==null,"Lates cannot be labeled later in a bridge");
        check(late(1,3,10,.5,70,.5,3.5,71,.5,true)==null,"A late must be exactly nine ticks later");
        check(late(1,3,9,.5,70,.5,2.5,71,.5,true)==null,"Two-column tap speeds must not become lates");
        check(late(1,3,9,.5,70,.5,3.5,70,.5,true)==null,"Three columns without one block of rise is not a late");
        check(late(1,5,9,.5,70,.5,3.5,71,.5,true)==null,"Only the two supported flat openings qualify");
        check(!"5b Late".equals(BridgeDetection.lateLabel(1,3,9,.5,70,.5,3.5,71,.5,true,false)),"Placement evidence is required");
        PlacementEvidence.reset();PlacementEvidence.record(5,1,0,2,0);PlacementEvidence.record(8,2,0,3,0);
        check(PlacementEvidence.qualifies(4,12,.5,3.5,true,2),"Two extra observed block columns qualify");
        check(!PlacementEvidence.qualifies(6,12,.5,3.5,true,2),"Evidence outside the jump interval is excluded");
        System.out.println("PASS: 1.0 flats and opening-only 5b/6b late timing, height, axes and placement gates");
    }
}
