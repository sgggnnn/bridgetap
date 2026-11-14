import com.bridger.momentum.*;
import java.nio.file.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.zip.ZipFile;

/** Contact sheet drawn from all six actual exported island models. */
public class CreateIslandGallery {
 public static void main(String[] args)throws Exception{
  String[] titles={"Jade Sanctuary","Ember Ruins","Frost Observatory","Aether Gate","Astral Reef","Willow Ruins"};
  String[] styles={"OVERGROWN RUINS","VOLCANIC RUINS","FROZEN SANCTUARY","ORIGINAL DESIGN","MCPLAY SILHOUETTE","WOODLAND RUINS"};
  String[] files={"dist/island-variants/Default - Jade Sanctuary.btisland","dist/island-variants/Default - Ember Ruins.btisland","dist/island-variants/Default - Frost Observatory.btisland","dist/aether-gate/Aether Gate.btisland","dist/astral-reef/Astral Reef.btisland","dist/willow-ruins/Willow Ruins.btisland"};
  int[] accents={0x8BD5A6,0xF3AE77,0xABDFF6,0xDADDB1,0x88DAD5,0xB1D69F};
  CreateIslandVariants.assets=new ZipFile(args[0]);
  BufferedImage image=new BufferedImage(3000,1600,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
  g.setColor(new Color(10,19,29));g.fillRect(0,0,3000,1600);
  g.setFont(new Font("SansSerif",Font.BOLD,40));g.setColor(new Color(229,240,245));g.drawString("BRIDGETAP / ISLAND COLLECTION",32,57);
  g.setFont(new Font("SansSerif",Font.PLAIN,18));g.setColor(new Color(146,173,191));g.drawString("6 designs / 12 islands / actual exported block models",34,94);
  for(int i=0;i<6;i++){
   IslandTemplate t=IslandTemplate.read(Paths.get(files[i]));int x=i*496+14;
   g.setColor(new Color(23,37,50));g.fillRoundRect(x,124,486,1398,18,18);
   g.setColor(new Color(accents[i]));g.fillRect(x+18,145,40,3);g.setFont(new Font("SansSerif",Font.BOLD,25));g.drawString(titles[i],x+20,187);
   g.setFont(new Font("SansSerif",Font.PLAIN,13));g.setColor(new Color(155,178,194));g.drawString(styles[i],x+20,214);
   g.setColor(new Color(223,233,239));g.setFont(new Font("SansSerif",Font.BOLD,15));g.drawString("DEPARTURE",x+20,258);
   CreateIslandVariants.model(g,t,0,x+8,274,470,540);
   g.setColor(new Color(53,74,90));g.fillRect(x+20,830,446,1);g.setColor(new Color(223,233,239));g.drawString("ARRIVAL",x+20,868);
   CreateIslandVariants.model(g,t,1,x+8,886,470,576);
   g.setFont(new Font("SansSerif",Font.PLAIN,13));g.setColor(new Color(152,175,193));g.drawString(t.blocks.size()+" blocks / paired scenery",x+20,1495);
  }
  g.setFont(new Font("SansSerif",Font.PLAIN,15));g.setColor(new Color(143,164,181));g.drawString("Minecraft 1.8.9 materials / model previews, not in-game screenshots / partial blocks simplified",32,1566);
  g.dispose();ImageIO.write(image,"png",Paths.get(args[1]).toFile());CreateIslandVariants.assets.close();System.out.println("Created 3000 x 1600 PNG: all 6 designs and 12 island models");
 }
}
