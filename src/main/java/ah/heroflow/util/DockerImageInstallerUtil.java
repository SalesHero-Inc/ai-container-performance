package ah.heroflow.util;

public class DockerImageInstallerUtil {

  public static void downloadAndInstallIfMissing(String imageName) {
    var imagePath = StorageProperties.imageFolder().resolve(imageName);
    var image = formatImageName(imageName);
    //todo we must download image before
  }

  private static String formatImageName(String imageName) {
    return String.format("%s.tar", imageName);
  }

}
