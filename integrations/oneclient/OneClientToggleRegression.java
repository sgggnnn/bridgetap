import com.bridger.momentum.oneclient.OneClientConfig;
import org.polyfrost.oneconfig.api.config.v1.annotations.MasterToggle;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;
public final class OneClientToggleRegression {
 public static void main(String[] args)throws Exception{
  java.lang.reflect.Field field=OneClientConfig.class.getField("modEnabled");
  if(field.getType()!=boolean.class||field.getAnnotation(MasterToggle.class)==null||field.getAnnotation(Switch.class)==null)throw new AssertionError("Missing native OneClient master card switch");
  if(!field.getAnnotation(Switch.class).title().equals("Enable BridgeTap"))throw new AssertionError("Master switch label");
  System.out.println("PASS actual OneConfig master-toggle annotation and searchable Enable BridgeTap setting");
 }
}
