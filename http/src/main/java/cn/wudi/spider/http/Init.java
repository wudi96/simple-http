package cn.wudi.spider.http;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author tusu.
 */
@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value = {java.lang.annotation.ElementType.TYPE})
public @interface Init {

}
