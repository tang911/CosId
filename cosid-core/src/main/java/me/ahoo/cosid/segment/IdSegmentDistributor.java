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

package me.ahoo.cosid.segment;

import static me.ahoo.cosid.segment.IdSegment.TIME_TO_LIVE_FOREVER;

import me.ahoo.cosid.segment.grouped.Grouped;
import me.ahoo.cosid.segment.grouped.GroupedKey;
import me.ahoo.cosid.util.Clock;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import jakarta.annotation.Nonnull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;


/**
 * Id Segment Distributor.
 *
 * @author ahoo wang
 */
public interface IdSegmentDistributor extends Grouped {
    int DEFAULT_SEGMENTS = 1;
    long DEFAULT_OFFSET = 0;
    long DEFAULT_STEP = 10;
    
    @Nonnull
    String getNamespace();
    
    @Nonnull
    String getName();
    
    default String getNamespacedName() {
        return getNamespacedName(getNamespace(), getName());
    }
    
    static String getNamespacedName(String namespace, String name) {
        return namespace + "." + name;
    }
    
    long getStep();
    
    default long getStep(int segments) {
        return Math.multiplyExact(getStep(), segments);
    }
    
    default boolean allowReset() {
        return GroupedKey.NEVER.equals(group());
    }
    
    long nextMaxId(long step);
    
    default long nextMaxId() {
        return nextMaxId(getStep());
    }
    
    @Nonnull
    default IdSegment nextIdSegment() {
        return nextIdSegment(TIME_TO_LIVE_FOREVER);
    }
    
    @Nonnull
    default IdSegment nextIdSegment(long ttl) {
        Preconditions.checkArgument(ttl > 0, "ttl:[%s] must be greater than 0.", ttl);
        
        final long maxId = nextMaxId();
        return new DefaultIdSegment(maxId, getStep(), Clock.SYSTEM.secondTime(), ttl, group());
    }
    
    @Nonnull
    default IdSegment nextIdSegment(int segments, long ttl) {
        Preconditions.checkArgument(segments > 0, "segments:[%s] must be greater than 0.", segments);
        Preconditions.checkArgument(ttl > 0, "ttl:[%s] must be greater than 0.", ttl);
        
        final long totalStep = getStep(segments);
        final long maxId = nextMaxId(totalStep);
        final IdSegment nextIdSegment = new DefaultIdSegment(maxId, totalStep, Clock.SYSTEM.secondTime(), ttl, group());
        return new MergedIdSegment(segments, nextIdSegment);
    }
    
    @Nonnull
    default IdSegmentChain nextIdSegmentChain(IdSegmentChain previousChain) {
        return nextIdSegmentChain(previousChain, DEFAULT_SEGMENTS, TIME_TO_LIVE_FOREVER);
    }
    
    @Nonnull
    default IdSegmentChain nextIdSegmentChain(IdSegmentChain previousChain, int segments, long ttl) {
        if (DEFAULT_SEGMENTS == segments) {
            IdSegment nextIdSegment = nextIdSegment(ttl);
            return new IdSegmentChain(previousChain, nextIdSegment, allowReset());
        }
        
        IdSegment nextIdSegment = nextIdSegment(segments, ttl);
        return new IdSegmentChain(previousChain, nextIdSegment, allowReset());
    }
    
    static void ensureStep(long step) {
        Preconditions.checkArgument(step > 0, "step:[%s] must be greater than 0!", step);
    }
    
    
    class Atomic implements IdSegmentDistributor {
        private static final AtomicInteger ATOMIC_COUNTER = new AtomicInteger();
        private final long step;
        private final String name;
        private final AtomicLong adder = new AtomicLong();
        
        public Atomic() {
            this(DEFAULT_STEP);
        }
        
        public Atomic(long step) {
            this.step = step;
            this.name = "atomic__" + ATOMIC_COUNTER.incrementAndGet();
        }
        
        @Nonnull
        @Override
        public String getNamespace() {
            return "__";
        }
        
        @Nonnull
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public long getStep() {
            return step;
        }
        
        @Override
        public long nextMaxId(long step) {
            return adder.addAndGet(step);
        }
        
    }
    
    @VisibleForTesting
    class Mock implements IdSegmentDistributor {
        private static final AtomicInteger MOCK_COUNTER = new AtomicInteger();
        private final long step;
        private final String name;
        private final long ioWaiting;
        private final AtomicLong adder = new AtomicLong();
        
        public Mock() {
            this(DEFAULT_STEP, 220000);
        }
        
        /**
         * ctor.
         *
         * @param step 单次获取IdSegment的区间长度
         * @param tps 发号器的TPS，用于模拟网络IO请求的等待时常
         */
        public Mock(long step, int tps) {
            this.step = step;
            this.ioWaiting = TimeUnit.SECONDS.toNanos(1) / tps;
            this.name = "mock__" + MOCK_COUNTER.incrementAndGet();
        }
        
        @Nonnull
        @Override
        public String getNamespace() {
            return "__";
        }
        
        @Nonnull
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public long getStep() {
            return step;
        }
        
        @Override
        public long nextMaxId(long step) {
            LockSupport.parkNanos(ioWaiting);
            return adder.addAndGet(step);
        }
        
    }
}
