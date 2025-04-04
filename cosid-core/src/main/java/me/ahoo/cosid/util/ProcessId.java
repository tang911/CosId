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

package me.ahoo.cosid.util;

import com.google.errorprone.annotations.Immutable;

import java.lang.management.ManagementFactory;

/**
 * get current process id .
 *
 * @author ahoo wang
 */
@Immutable
public enum ProcessId {
    CURRENT;
    
    private final int processId;
    
    ProcessId() {
        this.processId = getCurrentProcessId();
    }
    
    public int getProcessId() {
        return processId;
    }
    
    /**
     * get current process name .
     *
     * @return process name
     */
    public static String getCurrentProcessName() {
        return ManagementFactory.getRuntimeMXBean().getName();
    }
    
    /**
     * get current process id .
     *
     * @return process id
     */
    public static int getCurrentProcessId() {
        String processName = getCurrentProcessName();
        String processIdStr = processName.split("@")[0];
        return Integer.parseInt(processIdStr);
    }
}
