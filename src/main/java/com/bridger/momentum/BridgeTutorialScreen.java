package com.bridger.momentum;

import net.minecraft.unmapped.C_85740840;

/** Always available, non-destructive walkthrough: reading a lesson never changes settings. */
public final class BridgeTutorialScreen extends BridgeScreen {
    private final C_85740840 parent;
    private int lesson,topicPage;private boolean gallery=true,rotating;
    private int dragX;private double yaw=-.65,zoom=.9;private long lastNanos=System.nanoTime();
    private final TutorialPlayback playback=new TutorialPlayback();
    private final TutorialScene scene=new TutorialScene();
    private void select(int index){lesson=index;topicPage=index/8;gallery=false;playback.restart();lastNanos=System.nanoTime();yaw=-.65;zoom=.9;}

    private static final String[][] LESSONS={
        {"Getting started","Record your first bridge","Play on a bridge practice map and start a normal attempt.","BridgeTap displays jump coordinates and detects the finish.","Open the replay library with your BridgeTap key (default B).","Select a run to inspect its time, jumps and detected mode.","Open BridgeTap settings","settings"},
        {"Coordinates & timing","Understand each jump","The main number is the coordinate decimal at the jump.","Enable Velo to show the same axis one tick before jumping.","Both axes adds the other axis and their difference.","Offsets and Turn detection can be adjusted in Settings.","Open detection settings","settings"},
        {"HUD layout","Make the overlay yours","Open Edit HUD layout, then select the overlay to edit.","Drag it anywhere on screen; resize with its handle.","Choose colors for techniques, deltas, angles and velocity.","Use display modes to keep only the information you need.","Edit HUD layout","layout"},
        {"Replay playback","Watch, pause and inspect","Select a playable recording and click PLAY REPLAY.","Fly around freely, or click the recorded player for POV.","Paused sticks step ticks; playing sticks skip seconds.","Use the barrier item to leave the replay.","Open replay library","replays"},
        {"Replay inputs","Review keys and movement","Enable Keystrokes in replays in Settings.","Move the input overlay through Edit HUD layout.","Choose compact, detailed or hidden replay information.","Keystrokes outside replays is a separate setting.","Open replay HUD settings","settings"},
        {"Bookmarks","Jump straight to a moment","Select a replay, then Tools > Replay bookmarks.","Use automatic jump markers to find each takeoff.","Add your own marks for moments you want to review.","Scrub the timeline; set A and B to loop a passage.","Select a replay first","replays"},
        {"Folders & filters","Keep recordings organized","Use server folders and mode folders in the library.","Search or filter successes, imports and server modes.","Drag a recording onto a folder to move it.","Save important runs to protect them from cleanup.","Organize replays","replays"},
        {"Sharing replays","Send a recording to a friend","Select a run and choose Copy share code from its details.","Review privacy, then copy the code or export a shared file.","Your friend opens Import and previews before confirming.","Large recordings should be shared as .btreplay files.","Open replay library","replays"},
        {"Storage & cleanup","Prevent a huge replay library","Open Tools > Replay storage / auto-save.","Choose which recordings should be kept automatically.","Save or favorite important attempts before cleanup.","Optional two-day fail cleanup excludes protected runs.","Manage replay storage","storage"},
        {"Statistics","Track progress over time","Open Tools > Statistics after recording attempts.","Filter by server, mode and day, week or all time.","Compare successful average time and success rate.","Import saved history to include older local runs.","Open statistics","stats"},
        {"Fail analysis","Understand a missed placement","Enable automatic fail analysis only if you want it.","After a fail, read the short aiming hint in chat.","Select a failed replay for a more detailed review.","Hints depend on recorded evidence and may be uncertain.","Open failed bridge settings","fails"},
        {"Place an island","Change your client scenery","Stand on the start platform and open Island library.","Choose an island, then click Apply automatically.","Use Preview / Adjust if you need to tune the placement.","Remove placed island restores the original scenery.","Choose an island","islands"},
        {"Copy & import islands","Build your personal collection","Select a replay, open Island library > From replay.","Copy both islands together to preserve their arrangement.","Select blocks lets you capture a region instead.","Export .btisland files; friends use the Import tab.","Open island library","islands"},
        {"Island materials","Create your own island style","Choose an island and click Materials.","Pick an existing material, then its replacement block.","Compare the live 3D preview before replacing it.","Save as new island keeps the original unchanged.","Choose an island to edit","islands"},
        {"Presets & randomization","Reuse your favorite setup","Place an island, then save its arrangement in Presets.","Presets can be shared and associated with servers.","Random island and Random block vary attempts.","Auto reapply restores scenery when an island is absent.","Open island presets","presets"},
        {"Focus mode","Keep attention on your lane","Enable Focus in Island library.","Other scenery is hidden and other-player sounds muted.","Your custom islands remain client-side decorations.","Disable Focus to restore the surrounding scene.","Open focus settings","islands"},
        {"Block appearance","Customize the blocks you place","Open Tools > Block appearance and select a block.","Import modern assets if you want newer block visuals.","The hand, hotbar and placement sound follow the choice.","This changes appearance; server blocks stay unchanged.","Choose block appearance","blocks"},
        {"Create a sky overlay","Extract a sky from a pack","Open Custom sky overlays and browse folders on the left.","Select a pack and wait for custom sky detection.","Pick a color and compare original / recolored layers.","Name, create and activate it; custom sky needs compatible rendering.","Create a sky overlay","sky"},
        {"Video clips & audio","Save the last moments of play","Open Video clips & audio and configure local tools.","Choose duration, quality, FPS and optional microphone.","Enable the buffer, play, then use your Save clip shortcut.","The buffer starts OFF each launch; exports run locally.","Configure video clips","clips"},
        {"Replay video export","Render a saved bridge as video","Select a playable run, then Tools > Export video.","Choose POV and presentation options, including inputs.","Enable the final timer and block stack hotbar if wanted.","Export MP4, then use Open exported videos.","Choose a replay to export","replays"},
        {"Keybind profiles","Switch between control setups","Open Keybind profiles and save your current controls.","Name, rename or apply an existing profile.","Assign the cycle shortcut in Minecraft Controls.","The cycle shortcut stays the same across profiles.","Manage keybind profiles","keys"},
        {"Updates & support","Stay up to date","Open Updates to check for a newer compatible edition.","Review and confirm installation before restarting.","Use Join Discord for release news and support.","Your settings and local recordings remain on your PC.","Open updates","updates"},
        {"Sandstone overlays","Extract and recolor sandstone","Open Sandstone overlay from Tools or Block appearance.","Browse folders, select a pack, choose red sandstone if wanted.","Pick a color swatch, or enter a custom HEX color.","Compare the preview, create a new ZIP, then activate it.","Create a sandstone overlay","sandstone"}
    };
    @Override protected int canvasHeight(){return 620;}
    public BridgeTutorialScreen(C_85740840 parent){this.parent=parent;}
    private void launch(String target){
        switch(target){
            case "layout":open(new BridgeLayoutScreen(this));break;
            case "islands":open(new IslandScreen(this));break;
            case "presets":open(new IslandPresetsScreen(this));break;
            case "blocks":open(new PlacementAppearanceScreen(this));break;
            case "sky":open(new SkyOverlayScreen(this));break;
            case "sandstone":open(new SkyOverlayScreen(this,true));break;
            case "clips":open(new ClipScreen(this));break;
            case "stats":open(new StatisticsScreen(this));break;
            case "storage":open(new ReplayStorageScreen(this));break;
            case "fails":open(new FailureDisplayScreen(this));break;
            case "keys":open(new KeybindProfilesScreen(this));break;
            case "updates":UpdateManager.open(this);break;
            case "replays":open(ReplayFolderScreen.home());break;
            default:open(new BridgeSettingsScreen(this));
        }
    }
    @Override protected void paint(){
        long now=System.nanoTime();double elapsed=(now-lastNanos)/1e9;lastNanos=now;
        if(!gallery)playback.advance(elapsed);
        header("Discover BridgeTap",gallery?"Interactive lessons":TutorialScene.spatial(lesson)?"Interactive 3D demonstration":"Illustrated walkthrough");
        button(gallery?"Back":"All lessons",730,15,110,()->{if(gallery)back();else gallery=true;});
        if(gallery){
            label("Choose a feature. Watch it work. Try it yourself.",24,64);
            for(int n=0;n<8;n++){
                final int index=topicPage*8+n;if(index>=LESSONS.length)break;
                int x=20+(n%4)*208,y=94+(n/4)*170;
                panel(x,y,196,154);MenuIcons.paint(ui,index==5?"bookmark":index==6?"folders":index==7?"share":index==8?"storage":index==9?"stats":index==20?"keys":LESSONS[index][7],x+78,y+20,ACCENT);
                clipped(LESSONS[index][0],x+14,y+67,168,TEXT);
                clipped(TutorialScene.spatial(index)?"4 steps / 3D scene":"4 steps / Illustrated guide",x+14,y+88,168,MUTED);
                button("Explore",x+12,y+112,172,28,true,true,()->select(index));
            }
            button("Previous",20,442,110,24,topicPage>0,false,()->topicPage--);
            label((topicPage+1)+" / "+((LESSONS.length+7)/8),399,448);
            button("Next",730,442,110,24,(topicPage+1)*8<LESSONS.length,false,()->topicPage++);
            footer("Illustrated local demonstrations / No changes to your world or settings");return;
        }
        panel(20,72,180,476);panel(212,72,628,476);
        text("IN THIS LESSON",34,88,MUTED);clipped(LESSONS[lesson][0],34,110,152,TEXT);
        for(int step=0;step<4;step++){
            final int index=step;int y=148+step*48;
            button("Step "+(step+1),34,y,152,34,true,playback.step()==step,()->playback.step(index));
        }
        button("Previous lesson",34,366,152,26,lesson>0,false,()->select(lesson-1));
        button("Next lesson",34,402,152,26,lesson+1<LESSONS.length,false,()->select(lesson+1));
        clipped(LESSONS[lesson][1],228,86,596,TEXT);
        // Every scene is rendered inside its own viewport; never in the live Minecraft world.
        scene.paint(ui,lesson,playback.time(),yaw,zoom,228,112,596,300);
        if(TutorialScene.spatial(lesson))region(228,112,596,300,()->{rotating=true;dragX=mouseX;});
        text("STEP "+(playback.step()+1)+" / 4",228,423,ACCENT);
        clipped(LESSONS[lesson][playback.step()+2],228,442,596,TEXT);
        rounded(228,472,596,4,0xFF3D474F);
        rounded(228,472,Math.max(4,(int)(596*playback.time()/TutorialPlayback.LENGTH)),4,ACCENT);
        region(228,465,596,18,()->playback.seek((mouseX-228)/596.0*TutorialPlayback.LENGTH));
        button(playback.playing()?"Pause":"Play",228,496,70,28,true,false,playback::toggle);
        button("Replay",306,496,70,28,true,false,playback::restart);
        button("Reset view",384,496,98,28,TutorialScene.spatial(lesson),false,()->{yaw=-.65;zoom=.9;});
        button("Try it",664,496,160,28,true,true,()->launch(LESSONS[lesson][7]));
        footer((TutorialScene.spatial(lesson)?"Drag to rotate / Scroll to zoom / ":"")+"Space: play-pause / Left-right: step / Esc: all lessons");
    }
    @Override public void dragPreview(int x,int y){super.dragPreview(x,y);if(rotating){yaw+=(x-dragX)*.012;dragX=x;}}
    @Override public void releasePreview(){rotating=false;}
    @Override protected void scroll(int x,int y,int direction){if(!gallery&&TutorialScene.spatial(lesson)&&x>=228&&y>=112&&y<412)zoom=Math.max(.55,Math.min(1,zoom-direction*.05));}
    @Override protected void key(int key){if(gallery)return;if(key==57)playback.toggle();if(key==203)playback.step(playback.step()-1);if(key==205)playback.step(playback.step()+1);}
    @Override protected boolean escapeModal(){if(!gallery){gallery=true;return true;}return false;}
    @Override protected void back(){open(parent);}
}
