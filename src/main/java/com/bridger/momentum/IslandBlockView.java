package com.bridger.momentum;

import java.util.*;
import net.minecraft.unmapped.*;

/** Native actual-state resolution uses copied neighbours, never the live map. */
public final class IslandBlockView implements C_46108969 {
    private final Map<Long,C_15815210> states=new HashMap<>();
    public IslandBlockView(IslandTemplate template,C_15815210[] blocks){for(int i=0;i<blocks.length;i++){IslandTemplate.Block b=template.blocks.get(i);states.put(new C_32594356(b.x,b.y,b.z).m_14165896(),blocks[i]);}}
    public C_15815210 m_77852500(C_32594356 pos){C_15815210 state=states.get(pos.m_14165896());return state==null?C_81592558.m_64320102(0).m_52891069():state;}
    public C_40735137 m_40611090(C_32594356 pos){return null;}
    public int m_72193523(C_32594356 pos,int light){return 0xF000F0;}
    public boolean m_64558657(C_32594356 pos){return C_81592558.m_63461719(m_77852500(pos).m_41958956())==0;}
    public C_28105876 m_25174963(C_32594356 pos){return C_28105876.f_81101322;}
    public boolean m_58307127(){return false;}
    public int m_48917424(C_32594356 pos,C_28786548 side){return 0;}
    public C_61837504 m_59003592(){return C_61837504.f_99408143;}
    public boolean isSideSolid(C_32594356 pos,C_28786548 side,boolean fallback){return m_77852500(pos).m_41958956().m_57757663();}
}
