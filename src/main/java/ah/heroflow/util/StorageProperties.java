package ah.heroflow.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import com.google.common.base.Preconditions;

public class StorageProperties {

  public static final String OCR = "ocr";
  public static final String MODELS = "models";
  public static final String DEFAULT_IMAGE_FOLDER = "images";
  public static final String DEFAULT_SHARED_FOLDER = "sh_flow";

  public static final String IMAGE_PATH_IN_DOCKER = "/binary/ocr/working-dir/aaa";


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

}
