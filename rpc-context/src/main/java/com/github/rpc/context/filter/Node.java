package com.github.rpc.context.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jianlei.shi
 * @date 2021/3/19 11:38 上午
 * @description Node
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Node<T> {

    private T context;
    private Node<T> next;
}
