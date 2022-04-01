package ah.heroflow.images;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import ah.heroflow.util.StorageProperties;

public class ImageDataProvider {

  public String checkOcrImageAndMoveToWorkingDir() {
    try {
      var imageName = "aaa";
      var path = StorageProperties.workingDir();

      var imagePath = path.resolve(imageName);
      if (imagePath.toFile().exists()) {
        return imagePath.toString();
      }

      var inputStream = this.getClass().getResourceAsStream("/images/" + imageName);
      Files.copy(inputStream, imagePath, StandardCopyOption.REPLACE_EXISTING);
      return imagePath.toString();
    } catch (IOException e) {
      throw new RuntimeException("could create a working dir or move image to it", e);
    }
  }
}
