/*
 * * Copyright 2020 github.com/moltenjson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.spleefx.config.json.select;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark fields which are to be saved by {@link SelectableConfiguration}. Every field
 * annotated with this will be saved to the {@link SelectableConfiguration}, and on application bootstrap
 * every field annotated with this annotation will have its value set according to the value assigned in the
 * JSON configuration.
 * <p>
 * Fields annotated with this annotation must be {@code static}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigOpt {

    /**
     * The key of the field. If a field does not have a key (hence is empty or null) it will
     * have its field name used as a key.
     *
     * @return The field key
     */
    String value() default "";

}
