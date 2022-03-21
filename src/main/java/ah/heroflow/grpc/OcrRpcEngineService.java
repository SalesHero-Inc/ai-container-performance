package ah.heroflow.grpc;

import java.util.List;
import ah.heroflow.ocr.OcrField;
import ah.heroflow.ocr.align.AlignmentResult;
import ah.ocrstudio.service.OcrServiceGrpc;
import ah.ocrstudio.service.OcrServiceGrpc.OcrServiceBlockingStub;
import ah.ocrstudio.service.OcrServiceTypes.AlignmentRequest;
import ah.ocrstudio.service.OcrServiceTypes.AlignmentResponse;
import ah.ocrstudio.service.OcrServiceTypes.ExtractionRequest;
import ah.ocrstudio.service.OcrServiceTypes.ExtractionResponse;
import ah.ocrstudio.service.OcrServiceTypes.FieldDefinition;
import ah.ocrstudio.service.OcrServiceTypes.HealthRequest;
import ah.ocrstudio.service.OcrServiceTypes.HealthResponse;
import ah.ocrstudio.service.OcrServiceTypes.HealthStatus;
import ah.ocrstudio.service.OcrServiceTypes.ReferenceConfigurationRequest;
import ah.ocrstudio.service.OcrServiceTypes.ReferencesConfigurationRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class OcrRpcEngineService {

  private static final HealthRequest HEALTH_REQUEST = HealthRequest.newBuilder().build();

  private ManagedChannel _channel;
  private final OcrServiceBlockingStub _stub;

  public OcrRpcEngineService(String host, int port) {
    _channel = ManagedChannelBuilder.forAddress(host, port)
        .usePlaintext()
        .build();
    _stub = OcrServiceGrpc.newBlockingStub(_channel);
  }

  public void verifyConnection() {
    HealthResponse healthResponse = _stub.healthcheck(HEALTH_REQUEST);
    if (healthResponse.getHealthStatus() != HealthStatus.FAIL) {
      return;
    }
    throw new IllegalStateException("OCR RPC service is reporting an error");
  }

  public void configureReferences(List<String> paths) {
    var configurationRequest = ReferencesConfigurationRequest.newBuilder();

    var request = ReferenceConfigurationRequest.newBuilder()
        .addAllReferencePaths(paths)
        .build();
    configurationRequest.putReferencePaths("single-ocr-function", request);

    var configureReferencesResponse = _stub.configureReferences(configurationRequest.build());
    System.out.printf("configureReferences %s\n", configureReferencesResponse.getStatus().getMessage());
  }

  public AlignmentResult align(List<String> imagesPath, String dockerOutputFolder) {
    AlignmentResponse alignment = _stub.align(AlignmentRequest.newBuilder()
        .addAllDocumentPaths(imagesPath)
        .setOutputFolder(dockerOutputFolder)
        .build());

    System.out.printf("alignment: %b-%s\n", alignment.getStatus().getSuccess(), alignment.getStatus().getMessage());

    return AlignmentResult.of(alignment);
  }

  public void extract(AlignmentResult alignmentResult, List<OcrField> ocrFields) {
    ExtractionRequest.Builder extractionRequest = ExtractionRequest.newBuilder();

    for (OcrField ocrField : ocrFields) {
      // Skip execution for OCR field without aligned image
      extractionRequest.putFields(
          ocrField.getOcrName(),
          FieldDefinition.newBuilder()
              .setX(ocrField.getX1())
              .setY(ocrField.getY1())
              .setW(ocrField.getWidth())
              .setH(ocrField.getHeight())
              .setImagePath(alignmentResult.getAlignedImageByPageIndex().get(0).getImagePath())
              .setEngine(ocrField.getEngineName())
              .build());

    }
    ExtractionResponse extractResponse = _stub.extract(extractionRequest.build());
    System.out.printf("extract: %b-%s\n", extractResponse.getStatus().getSuccess(), extractResponse.getStatus().getMessage());

  }

  public void close() {
    _channel.shutdownNow();
    _channel = null;

  }
}
