package com.github.rpc.context.compile;

import com.github.rpc.context.util.ClassUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: JianLei
 * @date: 2020/9/17 2:46 下午
 * @description: 参考dubbo源码,动态编译抽象类
 */
public abstract class AbstractCompiler implements Compiler {

  private static final Pattern PACKAGE_PATTERN =
      Pattern.compile("package\\s+([$_a-zA-Z][$_a-zA-Z0-9\\.]*);");

  private static final Pattern CLASS_PATTERN =
      Pattern.compile("class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s+");

  @Override
  public Class<?> compile(String code, ClassLoader classLoader) {
    code = code.trim();
    Matcher matcher = PACKAGE_PATTERN.matcher(code);
    String pkg;
    if (matcher.find()) {
      pkg = matcher.group(1);
    } else {
      pkg = "";
    }
    matcher = CLASS_PATTERN.matcher(code);
    String cls;
    if (matcher.find()) {
      cls = matcher.group(1);
    } else {
      throw new IllegalArgumentException("No such class name in " + code);
    }
    String className = pkg != null && pkg.length() > 0 ? pkg + "." + cls : cls;
    try {
      return Class.forName(
          className,
          true,
              ClassUtils.getCallerClassLoader(getClass()));
    } catch (ClassNotFoundException e) {
      if (!code.endsWith("}")) {
        throw new IllegalStateException("The java code not endsWith \"}\", code: \n" + code + "\n");
      }
      try {
        return doCompile(className, code);
      } catch (RuntimeException t) {
        throw t;
      } catch (Throwable t) {
        throw new IllegalStateException(
            "Failed to compile class, cause: "
                + t.getMessage()
                + ", class: "
                + className
                + ", code: \n"
                + code
                + "\n, stack: "
                + t.toString());
      }
    }
  }



  protected abstract Class<?> doCompile(String name, String source) throws Throwable;
}
