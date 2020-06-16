package api.domain;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: JianLei
 * @date: 2020/6/13 9:59 下午
 * @description:
 */
@Data
@Builder
public class Goods implements Serializable {

  private String goodsId;
  private String goodsName;
  private String price;
}
