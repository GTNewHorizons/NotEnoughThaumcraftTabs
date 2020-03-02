package fewizz.bt;

import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.launchwrapper.IClassTransformer;

public class BTTransformer implements IClassTransformer {
   public byte[] transform(String name, String transformedName, byte[] classB) {
      if (transformedName.equals("thaumcraft.client.gui.GuiResearchBrowser")) {
         try {
            ZipFile file = new ZipFile(BTPlugin.coreFile);
            ZipEntry entry = file.getEntry("thaumcraft/client/gui/GuiResearchBrowser.class");
            InputStream is = file.getInputStream(entry);
            int size = (int)entry.getSize();
            byte[] newByteArray = new byte[size];

            int len;
            for(int pos = 0; pos < size; pos += len) {
               len = is.read(newByteArray, pos, size - pos);
            }

            is.close();
            file.close();
            return newByteArray;
         } catch (Exception var11) {
            throw new RuntimeException("Class " + name + " not transformed!!!");
         }
      } else {
         return classB;
      }
   }
}
