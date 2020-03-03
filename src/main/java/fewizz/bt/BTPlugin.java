package fewizz.bt;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import java.io.File;
import java.util.Map;

@MCVersion("1.7.10")
public class BTPlugin implements IFMLLoadingPlugin {
    public static File coreFile;

    public String[] getASMTransformerClass() {
        return new String[] { "fewizz.bt.BTTransformer" };
    }

    public String getModContainerClass() {
        return null;
    }

    public String getSetupClass() {
        return null;
    }

    public void injectData(Map<String, Object> data) {
        coreFile = (File) data.get("coremodLocation");
    }

    public String getAccessTransformerClass() {
        return null;
    }
}
