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

package me.ahoo.cosid.machine;

import com.google.common.base.Strings;
import com.google.errorprone.annotations.ThreadSafe;
import jakarta.annotation.Nonnull;

import java.time.Duration;

/**
 * MachineId Distributor.
 *
 * @author ahoo wang
 */
@ThreadSafe
public interface MachineIdDistributor {
    Duration FOREVER_SAFE_GUARD_DURATION = Duration.ofMillis(Long.MAX_VALUE);
    
    static int maxMachineId(int machineBit) {
        return ~(-1 << machineBit);
    }
    
    static int totalMachineIds(int machineBit) {
        return maxMachineId(machineBit) + 1;
    }
    
    static String namespacedMachineId(String namespace, int machineId) {
        return namespace + "." + Strings.padStart(String.valueOf(machineId), 8, '0');
    }
    
    static long getSafeGuardAt(Duration safeGuardDuration, boolean stable) {
        if (stable) {
            return 0L;
        }
        
        if (FOREVER_SAFE_GUARD_DURATION.equals(safeGuardDuration)) {
            return 0L;
        }
        
        long safeGuardAt = System.currentTimeMillis() - safeGuardDuration.toMillis();
        if (safeGuardAt < 0) {
            return 0L;
        }
        return safeGuardAt;
    }
    
    /**
     * distribute machine id.
     *
     * @param namespace namespace
     * @param machineBit machineBit
     * @param instanceId instanceId
     * @param safeGuardDuration safe Guard Duration
     * @return machine state
     * @throws MachineIdOverflowException This exception is thrown when the machine number allocation exceeds the threshold
     */
    @Nonnull
    MachineState distribute(String namespace, int machineBit, InstanceId instanceId, Duration safeGuardDuration) throws MachineIdOverflowException;
    
    /**
     * revert machine id.
     *
     * @param namespace namespace
     * @param instanceId instanceId
     */
    void revert(String namespace, InstanceId instanceId) throws NotFoundMachineStateException;
    
    /**
     * Guard the machine id by heartbeat.
     *
     * <p><img src="../../doc-files/Machine-Id-Safe-Guard.png" alt="Machine-Id-Safe-Guard"></p>
     *
     * @param namespace namespace
     * @param instanceId instanceId
     * @param safeGuardDuration safe Guard Duration
     */
    void guard(String namespace, InstanceId instanceId, Duration safeGuardDuration) throws NotFoundMachineStateException, MachineIdLostException;
    
}
