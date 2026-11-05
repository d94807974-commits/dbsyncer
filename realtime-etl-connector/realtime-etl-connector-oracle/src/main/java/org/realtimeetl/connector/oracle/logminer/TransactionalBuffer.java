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
package org.realtimeetl.connector.oracle.logminer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2023-12-09 20:26
 */
public class TransactionalBuffer {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, Transaction> transactions;
    private BigInteger lastCommittedScn;

    public TransactionalBuffer() {
        this.transactions = new HashMap<>();
        this.lastCommittedScn = BigInteger.ZERO;
    }

    public interface CommitCallback {

        void execute(BigInteger smallestScn, BigInteger commitScn, int callbackNumber) throws InterruptedException;
    }

    public boolean isEmpty() {
        return this.transactions.isEmpty();
    }

    private BigInteger calculateSmallestScn() {
        return transactions.isEmpty() ? null
                : transactions.values().stream().map(transaction->transaction.firstScn).min(BigInteger::compareTo).orElseThrow(()->new RuntimeException("Cannot calculate smallest SCN"));
    }

    /**
     * Get the earliest in the buffer zoneSubmitServiceSCN
     * @return MinimumfirstScn，If the buffer zone is emptyBacknull
     */
    public Long getSmallestScn() {
        BigInteger smallest = calculateSmallestScn();
        return smallest == null ? null : smallest.longValue();
    }

    public void registerCommitCallback(String txId, BigInteger scn, String operationId, CommitCallback callback) {
        transactions.compute(txId, (k, transaction)-> {
            if (transaction == null) {
                transaction = new Transaction(scn);
            }
            // InspectionActionsYesNoRegistered (prevention of duplicate processing resulting from repeated queries)
            if (transaction.registeredOperations.add(operationId)) {
                transaction.commitCallbacks.add(callback);
                // Update serviceslastScn
                if (scn.compareTo(transaction.lastScn) > 0) {
                    transaction.lastScn = scn;
                }
            }
            return transaction;
        });
    }

    public boolean commit(String txId, BigInteger commitScn, long committedScn) {
        Transaction transaction = transactions.remove(txId);
        if (transaction == null) {
            // Only ifTransactionalBufferOnly when there is no such thing.YesNoAlreadySubmitPass.
            // IfcommittedScnorlastCommittedScnMore than currentcommitScn，DescriptionThe matter may have been processed (resulting from Strategies)
            if (committedScn > commitScn.longValue() || lastCommittedScn.longValue() > commitScn.longValue()) {
                logger.info("txId {} not found in buffer and already committed (committedScn={}, lastCommittedScn={}, commitScn={}), ignore.", txId, committedScn, lastCommittedScn, commitScn);
                return false;
            }
            // There's no such thing.DMLRecords, maybe.YesBelowNormalSituation:
            // 1. Business onlyUpdateIt's a system watch or a watch that's not in the listening range.DMLWithin QueryConditionFilter Out
            // 2. ServiceDMLYes.startScnIt's been dealt with before.COMMIT
            // 3. Empty services (only)BEGIN/COMMIT，No actualDMLActions）
            logger.debug("txId {} not found in TransactionalBuffer. Likely modified tables outside monitoring scope or system tables.", txId);
            return false;
        }

        BigInteger smallestScn = calculateSmallestScn();

        int counter = transaction.commitCallbacks.size();
        for (CommitCallback callback : transaction.commitCallbacks) {
            try {
                callback.execute(smallestScn, commitScn, --counter);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
        lastCommittedScn = commitScn;
        return true;
    }

    public boolean rollback(String txId) {
        return transactions.remove(txId) != null;
    }

    private static final class Transaction {

        private final BigInteger firstScn;
        private BigInteger lastScn;
        private final List<CommitCallback> commitCallbacks;
        private final Set<String> registeredOperations;

        private Transaction(BigInteger firstScn) {
            this.firstScn = firstScn;
            this.commitCallbacks = new ArrayList<>();
            this.lastScn = firstScn;
            this.registeredOperations = new HashSet<>();
        }

        @Override
        public String toString() {
            return "Transaction{" + "firstScn=" + firstScn + ", lastScn=" + lastScn + '}';
        }
    }
}