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

package me.ahoo.cosid.test.machine.distributor;

import static me.ahoo.cosid.test.machine.distributor.MachineIdDistributorSpec.allInstances;
import static me.ahoo.cosid.test.machine.distributor.MachineIdDistributorSpec.mockInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import me.ahoo.cosid.machine.InstanceId;
import me.ahoo.cosid.machine.MachineIdDistributor;
import me.ahoo.cosid.machine.MachineIdOverflowException;
import me.ahoo.cosid.test.Assert;
import me.ahoo.cosid.test.MockIdGenerator;
import me.ahoo.cosid.test.TestSpec;

import com.google.common.base.Stopwatch;
import lombok.SneakyThrows;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

/**
 * DistributeSafeGuard .
 *
 * @author ahoo wang
 */
public class DistributeSafeGuard implements TestSpec {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(DistributeSafeGuard.class);
    private final Supplier<MachineIdDistributor> implFactory;
    private final int machineBit;
    private final Duration safeGuardDuration;

    public DistributeSafeGuard(Supplier<MachineIdDistributor> implFactory, int machineBit, Duration safeGuardDuration) {
        this.implFactory = implFactory;
        this.machineBit = machineBit;
        this.safeGuardDuration = safeGuardDuration;
    }

    @SneakyThrows
    @Override
    public void verify() {
        MachineIdDistributor distributor = implFactory.get();

        String namespace = MockIdGenerator.usePrefix("DistributeSafeGuard").generateAsString();
        logger.info("Verify namespace:{}", namespace);

        int moreMachineBit = machineBit + 1;
        List<InstanceId> allInstances = allInstances(moreMachineBit, false);
        int endIdx = MachineIdDistributor.totalMachineIds(machineBit);
        List<InstanceId> availableInstances = allInstances.subList(0, endIdx);
        assertThat(availableInstances, hasSize(MachineIdDistributor.totalMachineIds(machineBit)));

        for (int i = 0; i < availableInstances.size(); i++) {
            int machineId = distributor.distribute(namespace, machineBit, allInstances.get(i), safeGuardDuration).getMachineId();
            assertThat(machineId, equalTo(i));
        }

        InstanceId overflowInstanceId = mockInstance(MachineIdDistributor.totalMachineIds(machineBit), false);
        Assert.assertThrows(MachineIdOverflowException.class, () -> {
            distributor.distribute(namespace, machineBit, overflowInstanceId, safeGuardDuration);
        });

        /*
         * 等待所有实例到达安全守护点(SafeGuardAt)，即变成可回收状态.
         */
        Stopwatch stopwatch = Stopwatch.createStarted();
        Thread.sleep(safeGuardDuration.plusMillis(300).toMillis());
        logger.info("Verify namespace:{},safeGuardDuration:[{}], SafeGuardAt:[{}] ,Time:[{}]", namespace,
                safeGuardDuration,
                MachineIdDistributor.getSafeGuardAt(safeGuardDuration, false),
                stopwatch.elapsed()
        );
        availableInstances = allInstances.subList(endIdx, MachineIdDistributor.totalMachineIds(moreMachineBit));

        Integer[] machineIds = availableInstances
                .stream()
                .map(instanceId -> distributor.distribute(namespace, machineBit, instanceId, safeGuardDuration).getMachineId())
                .sorted().toArray(Integer[]::new);

        for (int i = 0; i < machineIds.length; i++) {
            assertThat(machineIds[i], equalTo(i));
        }

    }
}
