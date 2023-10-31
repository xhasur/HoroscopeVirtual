package horoscope.model;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Builder
public class Signs {

  private Integer id;
  private String name;
  private String since;
  private String until;
  private String characteristics;

}
