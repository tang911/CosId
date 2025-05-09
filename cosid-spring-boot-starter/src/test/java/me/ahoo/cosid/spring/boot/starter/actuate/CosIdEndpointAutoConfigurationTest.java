/*
 * Copyright [2021-present] [ahoo wang <ahoowang@qq.com> (https://github.com/Ahoo-Wang)].
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ahoo.cosid.spring.boot.starter.actuate;

import me.ahoo.cosid.provider.DefaultIdGeneratorProvider;
import me.ahoo.cosid.provider.IdGeneratorProvider;

import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class CosIdEndpointAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void contextLoads() {
        this.contextRunner
            .withBean(IdGeneratorProvider.class, () -> DefaultIdGeneratorProvider.INSTANCE)
            .withUserConfiguration(CosIdEndpointAutoConfiguration.class)
            .run(context -> AssertionsForInterfaceTypes.assertThat(context)
                .hasSingleBean(CosIdEndpointAutoConfiguration.class)
                .hasSingleBean(CosIdEndpoint.class)
                .hasSingleBean(CosIdGeneratorEndpoint.class)
                .hasSingleBean(CosIdStringGeneratorEndpoint.class));
    }

    @Test
    void contextLoadsDisabled() {
        this.contextRunner
            .withPropertyValues("cosid.enabled=false")
            .withUserConfiguration(CosIdEndpointAutoConfiguration.class)
            .run(context -> AssertionsForInterfaceTypes.assertThat(context)
                .doesNotHaveBean(CosIdEndpointAutoConfiguration.class)
            );
    }
}