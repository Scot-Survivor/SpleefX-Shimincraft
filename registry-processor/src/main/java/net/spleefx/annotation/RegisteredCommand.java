package net.spleefx.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface RegisteredCommand {

    /**
     * The parameters added to the command in code generation. Straight Java code.
     *
     * @return The stuff after <code>new SomeClassName</code>
     */
    String constructorSignature() default "()";

}
