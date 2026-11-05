/**
 * Copyright 2026 Realtime ETL
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
package org.realtimeetl.common.metric;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2024-06-02 22:44
 */
public class TimeMetric {

    /**
     * Number of slots
     */
    private int bucketSize;

    /**
     * TimeFilm, unitms
     */
    private int slice;

    /**
     * For judgementYesNoBut skip the lock fight.
     */
    private int timeSliceUsed;

    /**
     * Slot
     */
    private Bucket[] buckets;

    /**
     * Objective CombinedLocation of slots
     */
    private volatile int position;

    /**
     * CloseObjective CombinedZoom UpdateTime
     */
    private volatile Long latestPassedTime;

    /**
     * Lock used to enter Proposal
     */
    private ReentrantLock enterNextBucketLock;

    /**
     * Default60A slot. A slot.TimeAs1000ms
     */
    public TimeMetric() {
        this(60, 1000);
    }

    /**
     * InitializeBucketNumberandEachBucketYes.TimeSnippets
     *
     * @param bucketSize
     * @param slice
     */
    public TimeMetric(int bucketSize, int slice) {
        this.bucketSize = bucketSize;
        this.position = bucketSize > 3 ? bucketSize - 3 : bucketSize - 1;
        this.slice = slice;
        long now = System.currentTimeMillis();
        this.latestPassedTime = now - (2 * slice);
        this.timeSliceUsed = 3 * slice;
        this.enterNextBucketLock = new ReentrantLock();
        this.buckets = new Bucket[bucketSize];
        for (int i = 0; i < bucketSize; i++) {
            this.buckets[i] = new Bucket(now);
        }
    }

    public void add(long count) {
        long passTime = System.currentTimeMillis();
        if (passTime - latestPassedTime < slice) {
            buckets[position].add(count);
            return;
        }

        if (enterNextBucketLock.isLocked() && passTime - latestPassedTime < timeSliceUsed) {
            buckets[position].add(count);
            return;
        }

        try {
            enterNextBucketLock.lock();
            if (passTime - latestPassedTime < slice) {
                buckets[position].add(count);
                return;
            }

            position = position + 1 >= bucketSize ? 0 : position + 1;
            Bucket nextBucket = buckets[position];
            nextBucket.reset(passTime);
            nextBucket.add(count);
            latestPassedTime = passTime;
        } finally {
            enterNextBucketLock.unlock();
        }
    }

    public Bucket[] getBucketAll() {
        return buckets;
    }
}
