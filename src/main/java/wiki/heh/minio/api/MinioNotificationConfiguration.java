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

package wiki.heh.minio.api;


import wiki.heh.minio.api.config.MinioConfiguration;
import wiki.heh.minio.api.config.MinioConfigurationProperties;
import wiki.heh.minio.api.notification.MinioNotification;
import io.minio.MinioClient;
import io.minio.errors.*;
import io.minio.notification.NotificationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * minio通知配置文件
 * @author hehua
 */
@Configuration
@AutoConfigureBefore(MinioMetricConfiguration.class)
@AutoConfigureAfter(MinioConfiguration.class)
public class MinioNotificationConfiguration implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinioNotificationConfiguration.class);

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfigurationProperties minioConfigurationProperties;

    private List<Thread> handlers = new ArrayList<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Object obj = applicationContext.getBean(beanName);

            Class<?> objClz = obj.getClass();
            if (org.springframework.aop.support.AopUtils.isAopProxy(obj)) {
                objClz = org.springframework.aop.support.AopUtils.getTargetClass(obj);
            }

            for (Method m : objClz.getDeclaredMethods()) {
                if (m.isAnnotationPresent(MinioNotification.class)) {
                    //Check if has NotificationInfo parameter only
                    if (m.getParameterCount() != 1) {
                        throw new IllegalArgumentException("Minio notification handler should have only one NotificationInfo parameter");
                    }

                    if (m.getParameterTypes()[0] != NotificationInfo.class) {
                        throw new IllegalArgumentException("Parameter should be instance of NotificationInfo");
                    }

                    MinioNotification annotation = m.getAnnotation(MinioNotification.class);

                    //然后注册方法处理程序
                    Thread handler = new Thread(() -> {
                        for (; ; ) {
                            try {
                                LOGGER.info("Registering Minio handler on {} with notification {}", m.getName(), Arrays.toString(annotation.value()));
                                minioClient.listenBucketNotification(minioConfigurationProperties.getBucket(),
                                    annotation.prefix(),
                                    annotation.suffix(),
                                    annotation.value(),
                                    info -> {
                                        try {
                                            LOGGER.debug("Receive notification for method {}", m.getName());
                                            m.invoke(obj, info);
                                        } catch (IllegalAccessException | InvocationTargetException e) {
                                            LOGGER.error("Error while handling notification for method {} with notification {}", m.getName(), Arrays.toString(annotation.value()));
                                            LOGGER.error("Exception is", e);
                                        }
                                    });
                            } catch (InvalidBucketNameException | InternalException | ErrorResponseException | XmlPullParserException | InvalidKeyException | IOException | InsufficientDataException | NoSuchAlgorithmException | NoResponseException | InvalidResponseException e) {
                                LOGGER.error("Error while registering notification for method {} with notification {}", m.getName(), Arrays.toString(annotation.value()));
                                LOGGER.error("Exception is", e);
                                throw new IllegalStateException("Cannot register handler", e);
                            }
                        }
                    });
                    handler.start();
                    handlers.add(handler);
                }
            }
        }
    }
}
