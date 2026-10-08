package mcjty.rftoolsutility.modules.environmental;

import java.util.function.Supplier;
import mcjty.rftoolsutility.modules.environmental.modules.EnvironmentModule;

public interface EnvModuleProvider {
   Supplier<? extends EnvironmentModule> getServerEnvironmentModule();

   String getName();
}
