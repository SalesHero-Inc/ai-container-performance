package ah.heroflow;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OcrContainerPerformanceSamplerTest {

  @Test
  void testDockerContainer() {
    OcrContainerPerformanceSampler dockerPerformance = new OcrContainerPerformanceSampler();
    var sampleResult = dockerPerformance.runTest(new JavaSamplerContext(new Arguments()));

    Assertions.assertTrue(sampleResult.isSuccessful());
  }
}
