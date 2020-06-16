package api.service;

import api.domain.Goods;

import java.util.List;

/**
 * @author: JianLei
 * @date: 2020/6/13 10:01 下午
 * @description:
 */

public interface IGoodsService {

  List<Goods> getGoodsById(Long id);


  String helloRpc(String var);
}
