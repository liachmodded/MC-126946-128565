package com.github.liachmodded.datapacks;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.FileFilter;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.resource.DirectoryResourcePack;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackContainer;
import net.minecraft.resource.ResourcePackCreator;
import net.minecraft.resource.ZipResourcePack;

public class DatapacksPackCreator implements ResourcePackCreator {

  private static final FileFilter POSSIBLE_PACK = (file_1) -> {
    boolean boolean_1 = file_1.isFile() && file_1.getName().endsWith(".zip");
    boolean boolean_2 = file_1.isDirectory() && (new File(file_1, "pack.mcmeta")).isFile();
    return boolean_1 || boolean_2;
  };
  private final String prefix;
  private final File packsFolder;

  public DatapacksPackCreator(String prefix, File packsFolder) {
    Preconditions.checkArgument(!prefix.contains("  "), "No consecutive spaces in data pack name!");
    this.prefix = prefix;
    this.packsFolder = packsFolder;
  }

  public <T extends ResourcePackContainer> void registerContainer(Map<String, T> map_1,
      ResourcePackContainer.Factory<T> factory) {
    if (!this.packsFolder.isDirectory()) {
      this.packsFolder.mkdirs();
    }

    File[] possibles = this.packsFolder.listFiles(POSSIBLE_PACK);
    if (possibles != null) {
      for (File each : possibles) {
        String fileName = each.getName();
        if (fileName.contains("  ")) {
          DataPacksMain.LOGGER.warn("Ignored data pack candidate %s as it contains consecutive spaces in its file name", fileName);
          continue;
        }
        String packName = prefix + "/" + fileName;
        T resourcePackContainer_1 = ResourcePackContainer.of(packName, false, this.createResourcePack(each), factory, ResourcePackContainer.SortingDirection.TOP);
        if (resourcePackContainer_1 != null) {
          map_1.put(packName, resourcePackContainer_1);
        }
      }

    }
  }

  private Supplier<ResourcePack> createResourcePack(File file) {
    return file.isDirectory() ? () -> new DirectoryResourcePack(file)
        : () -> new ZipResourcePack(file);
  }
}
