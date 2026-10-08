package mcjty.rftoolsutility.modules.screen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mcjty.rftoolsbase.api.screens.IScreenModuleRegistry;
import mcjty.rftoolsbase.api.screens.data.IModuleDataFactory;
import mcjty.rftoolsutility.modules.screen.data.ModuleDataBoolean;
import mcjty.rftoolsutility.modules.screen.data.ModuleDataInteger;
import mcjty.rftoolsutility.modules.screen.data.ModuleDataString;
import mcjty.rftoolsutility.modules.screen.modules.InventoryScreenModule;
import mcjty.rftoolsutility.modules.screen.modules.ScreenModuleHelper;

public class ScreenModuleRegistry implements IScreenModuleRegistry {
   private Map<String, IModuleDataFactory<?>> dataFactoryMap = new HashMap<>();
   private Map<String, Integer> idToIntMap = null;
   private Map<Integer, String> inttoIdMap = null;

   public void registerBuiltins() {
      this.dataFactoryMap.put("rftoolsutility:bool", ModuleDataBoolean::new);
      this.dataFactoryMap.put("rftoolsutility:integer", ModuleDataInteger::new);
      this.dataFactoryMap.put("rftoolsutility:string", ModuleDataString::new);
      this.dataFactoryMap.put("rftoolsutility:contents", ScreenModuleHelper.ModuleDataContents::new);
      this.dataFactoryMap.put("rftoolsutility:itemStacks", InventoryScreenModule.ModuleDataStacks::new);
   }

   public void registerModuleDataFactory(String id, IModuleDataFactory<?> dataFactory) {
      this.dataFactoryMap.put(id, dataFactory);
   }

   public IModuleDataFactory<?> getModuleDataFactory(String id) {
      return this.dataFactoryMap.get(id);
   }

   public String getNormalId(int i) {
      this.createIdMap();
      return this.inttoIdMap.get(i);
   }

   public int getShortId(String id) {
      this.createIdMap();
      return this.idToIntMap.get(id);
   }

   private void createIdMap() {
      if (this.idToIntMap == null) {
         this.idToIntMap = new HashMap<>();
         this.inttoIdMap = new HashMap<>();
         List<String> strings = new ArrayList<>(this.dataFactoryMap.keySet());
         strings.sort(Comparator.naturalOrder());
         int idx = 0;

         for (String s : strings) {
            this.idToIntMap.put(s, idx);
            this.inttoIdMap.put(idx, s);
            idx++;
         }
      }
   }
}
