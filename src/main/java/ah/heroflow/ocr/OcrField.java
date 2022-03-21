package ah.heroflow.ocr;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OcrField implements Serializable {

  private int x1;
  private int y1;
  private int width;
  private int height;
  private int pageIndex;
  private String ocrName;
  private String engineName;

  public static OcrField testOcrField() {
    return new OcrFieldBuilder()
        .ocrName("field")
        .x1(164)
        .y1(147)
        .width(610)
        .height(146)
        .engineName("tesseract")
        .build();
  }
}
