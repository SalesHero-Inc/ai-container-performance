package ah.heroflow.util;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.jaxrs.JerseyDockerHttpClient;

public class DockerImageUtil {

  public static void downloadAndInstallIfMissing(String imageName) {
    try(DockerClient dockerClient = openDefaultClient()) {
      List<Image> images = dockerClient.listImagesCmd().exec();
      var imageExists = images.stream()
          .flatMap(image -> Arrays.stream(image.getRepoTags()))
          .anyMatch(imageName::equals);

      if(!imageExists) {
        dockerClient.pullImageCmd(getImageRepository(imageName))
            .withTag(getImageVersion(imageName))
            .exec(new PullImageResultCallback())
            .awaitCompletion(10, TimeUnit.MINUTES);
      }

    } catch (Exception e) {
      throw new RuntimeException("Could not download the image - " + imageName, e);
    }
  }

  private static String getImageRepository(String imageName) {
    return StringUtils.substringBeforeLast(imageName, ":");
  }

  private static String getImageVersion(String imageName) {
    return StringUtils.substringAfterLast(imageName, ":");
  }

  public static DockerClient openDefaultClient() {
    var config = DefaultDockerClientConfig.createDefaultConfigBuilder()
        .withRegistryUrl("nexus.automationhero.ai:8443")
        .withRegistryUsername("SayanZhumashev")
        .withRegistryPassword("ulcqT0k2b9sGVlDid8YfcvJK")
        .build();

    return DockerClientBuilder.getInstance(config).build();
  }

  public static DockerClient openClientWithSocketTimeout() {
    var config = DefaultDockerClientConfig.createDefaultConfigBuilder()
        .withRegistryUrl("nexus.automationhero.ai:8443")
        .withRegistryUsername("SayanZhumashev")
        .withRegistryPassword("ulcqT0k2b9sGVlDid8YfcvJK")
        .build();
    var client = new JerseyDockerHttpClient.Builder()
        .dockerHost(config.getDockerHost())
        .sslConfig(config.getSSLConfig())
        .connectTimeout(100)
        .readTimeout(1_000)
        .build();
    return DockerClientBuilder.getInstance(config)
        .withDockerHttpClient(client)
        .build();
  }
}
