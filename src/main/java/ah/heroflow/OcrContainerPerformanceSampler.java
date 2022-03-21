package ah.heroflow;

import static ah.heroflow.util.StorageProperties.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HealthCheck;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.jaxrs.JerseyDockerHttpClient;
import ah.heroflow.grpc.OcrRpcEngineService;
import ah.heroflow.ocr.OcrField;
import ah.heroflow.util.StorageProperties;

public class OcrContainerPerformanceSampler extends AbstractJavaSamplerClient {

  public static final int INTERNAL_PORT = 8274;
  public static final String LOCALHOST = "localhost";


  /**
   * we need to add docker data here
   *
   * @return - Arguments
   */
  @Override
  public Arguments getDefaultParameters() {
    Arguments defaultParameters = new Arguments();
    defaultParameters.addArgument("port", "9090");
    defaultParameters.addArgument("internal_docker_mount_path", "/binary");
    defaultParameters.addArgument("external_mount_path", StorageProperties.modelFolder().toString());
    defaultParameters.addArgument("imageName", "nexus.automationhero.ai:8443/ocr-service:1.7.4.1");
    return defaultParameters;
  }

  @Override
  public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
    var result = new SampleResult();
    result.sampleStart();
    //start

    //todo download image if needed
    var imageName = getArgument("imageName");
    var port = Integer.parseInt(getArgument("port"));
    var mountPath = getArgument("external_mount_path");
    var dockerMountPath = getArgument("internal_docker_mount_path");

    try {
      Files.createDirectories(Path.of(mountPath));
    } catch (Exception e) {
      throw new RuntimeException("Failed to create directory " + mountPath, e);
    }
    System.out.printf("Request to start new container from image %s was received.", imageName);

    var portBindings = List.of(PortBinding.parse(String.format("%d:%s", INTERNAL_PORT, port)));
    var exposedPorts = portBindings.stream()
        .map(PortBinding::getExposedPort)
        .collect(Collectors.toList());
    var volumes = List.of(new Volume(dockerMountPath));
    var binds = List.of(new Bind(mountPath, new Volume(dockerMountPath)));

    try (DockerClient dockerClientWithTimeout = openClientWithSocketTimeout();
        CreateContainerCmd createContainerCmd = dockerClientWithTimeout.createContainerCmd(imageName)) {
      var config = new HostConfig() //todo check configuration, we might need to increase it
          .withPortBindings(portBindings)
          .withMemory(951619276L)
          .withMemorySwap(961619276L)
          .withNanoCPUs(4000L)
          .withBinds(binds);

      createContainerCmd.withHostConfig(config)
          .withExposedPorts(exposedPorts)
          .withVolumes(volumes)
          .withLabels(Map.of("automationhero.namespace", "hero_flow"));

      createContainerCmd.withHealthcheck(new HealthCheck()
          .withStartPeriod(100_000_000L)
          .withInterval(100_000_000L)
          .withRetries(2)
          .withTimeout(1_000_000_000L));

      createContainerCmd.withCmd();
      var container = createContainerCmd.exec();
      var containerId = container.getId();

      //start container
      dockerClientWithTimeout.startContainerCmd(containerId).exec();

      //health check
      var rpcSession = new OcrRpcEngineService(LOCALHOST, INTERNAL_PORT);
      runWithTimeout(rpcSession::verifyConnection, 30);

      //todo now we use static path, we need to generate sub-folders for each request
      //configure references, we must do it before alignment
      rpcSession.configureReferences(List.of(IMAGE_PATH_IN_DOCKER));

      //alignment
      var alignmentResult = rpcSession.align(List.of(IMAGE_PATH_IN_DOCKER),
          "/binary/ocr/output/");

      //extract
      rpcSession.extract(alignmentResult, List.of(OcrField.testOcrField()));

      //stop/remove docker container
      dockerClientWithTimeout.removeContainerCmd(containerId).withForce(true).exec();
    } catch (IOException e) {
      throw new RuntimeException("Something went wrong", e);
    }

    //end
    result.sampleEnd();
    result.setSuccessful(true);
    return result;
  }

  /**
   * after starting container it's not running immediately
   * we need some time to check that container stated and running correctly
   * @param runnable -  Runnable(void callback)
   * @param seconds - timeout
   */
  private void runWithTimeout(Runnable runnable, int seconds) {
    long start = System.currentTimeMillis();
    long end = start + seconds * 1000;
    while (System.currentTimeMillis() < end) {
      try {
        runnable.run();
        break;
      } catch (Exception e) {
        //do nothing
      }
    }
  }

  private String getArgument(String param) {
    return getDefaultParameters().getArgumentsAsMap().get(param);
  }

  public static DockerClient openDefaultClient() {
    var config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
    return openDefaultClient(config);
  }

  public static DockerClient openDefaultClient(DefaultDockerClientConfig config) {
    return DockerClientBuilder.getInstance(config)
        .withDockerHttpClient(new ApacheDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())
            .sslConfig(config.getSSLConfig())
            .build())
        .build();
  }

  public static DockerClient openClientWithSocketTimeout() {
    var config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
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
