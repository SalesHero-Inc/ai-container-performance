package ah.heroflow.ocr.align;

import java.io.Serial;
import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AlignedImageResult implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private String imagePath;

  public static AlignedImageResult of(String imagePath) {
    return new AlignedImageResult.AlignedImageResultBuilder()
        .imagePath(imagePath)
        .build();
  }
}
