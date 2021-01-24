/*
 * Copyright Jordan LEFEBURE © 2019.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package wiki.heh.minio.api.notification;

import org.springframework.scheduling.annotation.Async;

import java.lang.annotation.*;


/**
 * 向Minio存储桶添加一个侦听器，该侦听器处理{@code value}参数中给定的事件.
 * 带注释的方法应具有参数{@link io.minio.notification.NotificationInfo}并返回{@code void}.
 *
 * @author hehua
 */

@Async
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MinioNotification {

    /**
     * 方法处理程序应接收的所有事件。定义的值
     * <a href="https://docs.min.io/docs/minio-bucket-notification-guide.html">Minio 文档</a> are allowed.
     */
    String[] value();

    /**
     * 应处理项目的前缀
     */
    String prefix() default "";

    /**
     * 应处理项目的后缀
     */
    String suffix() default "";

}
