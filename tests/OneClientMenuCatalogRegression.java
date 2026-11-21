import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import com.bridger.momentum.oneclient.OneClientConfig;
import org.polyfrost.oneconfig.api.config.v1.annotations.Button;

public final class OneClientMenuCatalogRegression {
    public static void main(String[] args) {
        Set<String> titles=new HashSet<String>(),categories=new HashSet<String>();
        int count=0;
        for(Method method:OneClientConfig.class.getDeclaredMethods()){
            Button action=method.getAnnotation(Button.class);if(action==null)continue;
            if(method.getParameterTypes().length!=0||!titles.add(action.title()))throw new AssertionError("Duplicate or invalid native action");
            categories.add(action.category());count++;
        }
        if(count!=30)throw new AssertionError("Expected all 30 menu entry points, got "+count);
        for(String category:new String[]{"Replays","Îles","Apparence","Enregistrement","HUD","Réglages","Aide"})
            if(!categories.contains(category))throw new AssertionError("Missing native category "+category);
        System.out.println("PASS: all 30 menu actions exposed by native OneConfig annotations across seven categories");
    }
}
