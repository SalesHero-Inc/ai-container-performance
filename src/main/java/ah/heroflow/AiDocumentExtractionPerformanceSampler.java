package ah.heroflow;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

public class AiDocumentExtractionPerformanceSampler extends AbstractJavaSamplerClient {

  /**
   * we need to add docker data here
   * @return - Arguments
   */
  @Override
  public Arguments getDefaultParameters() {
    //empty
    return super.getDefaultParameters();
  }

  @Override
  public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
    var result = new SampleResult();
    result.sampleStart();

    //code here
    //todo implement

    result.sampleEnd();
    result.setSuccessful(true);
    return result;
  }
}
