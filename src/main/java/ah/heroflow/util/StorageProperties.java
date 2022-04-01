package ah.heroflow.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.google.common.base.Preconditions;

public class StorageProperties {

  public static final String OCR = "ocr";
  public static final String MODELS = "models";
  public static final String OUTPUT_FOLDER = "output";
  public static final String WORKING_FOLDER = "working-dir";
  public static final String DEFAULT_IMAGE_FOLDER = "images";
  public static final String DEFAULT_SHARED_FOLDER = "sh_flow";

  public static final String DOCKER_ROOT_DIR = "/binary";

  public static Path rootFolder() {
    var homeFolder = System.getProperty("user.home");
    Preconditions.checkArgument(!homeFolder.isEmpty(), "home folder cannot be empty");
    Preconditions.checkArgument(!homeFolder.contains(".."), "home folder specified should be absolute. It is '%s'", homeFolder);
    return Paths.get(homeFolder, DEFAULT_SHARED_FOLDER);
  }

  public static Path imageFolder() {
    return rootFolder().resolve(DEFAULT_IMAGE_FOLDER);
  }

  public static Path modelFolder() {
    return rootFolder().resolve(MODELS);
  }

  public static Path ocrFolder() {
    return modelFolder().resolve(OCR);
  }

  public static Path workingDir() {
    return ocrFolder().resolve(WORKING_FOLDER);
  }

  public static Path outputDir() {
    return ocrFolder().resolve(OUTPUT_FOLDER);
  }

  public static void generateOcrMountDirs() {
    try {
      Files.createDirectories(workingDir());
      Files.createDirectories(outputDir());
    } catch (IOException e) {
      throw new RuntimeException("Could not create a working/output dir", e);
    }
  }

  public static String toDockerDir(String dir) {
    return dir.replace(modelFolder().toString(), DOCKER_ROOT_DIR);
  }

}
