package ah.heroflow.ocr.align;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import com.google.common.base.Verify;
import lombok.Builder;
import lombok.Data;
import ah.ocrstudio.service.OcrServiceTypes.AlignedImage;
import ah.ocrstudio.service.OcrServiceTypes.AlignmentResponse;

@Data
@Builder
public class AlignmentResult implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private String referenceId;
  private Map<Integer, AlignedImageResult> alignedImageByPageIndex;

  public void verify() {
    Verify.verify(!alignedImageByPageIndex.values().isEmpty(), "alignment result is empty");
  }

  public static AlignmentResult of(AlignmentResponse alignment) {
    Map<Integer, AlignedImageResult> alignedImageByPageIndex = new LinkedHashMap<>();
    for (Entry<Integer, AlignedImage> entry : alignment.getAlignedImagesMap().entrySet()) {
      alignedImageByPageIndex.put(entry.getKey(), AlignedImageResult.of(entry.getValue().getImagePath()));
    }
    return of(alignment.getReferenceId(), alignedImageByPageIndex);
  }

  public static AlignmentResult of(String referenceId, Map<Integer, AlignedImageResult> alignedImageByPageIndex) {
    return  new AlignmentResult.AlignmentResultBuilder()
        .referenceId(referenceId)
        .alignedImageByPageIndex(alignedImageByPageIndex)
        .build();
  }
}
