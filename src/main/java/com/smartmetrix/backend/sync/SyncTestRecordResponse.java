package com.smartmetrix.backend.sync;

import java.util.List;

public class SyncTestRecordResponse {

    private int totalReceived;
    private int successfullySynced;
    private int failed;
    private List<String> syncedClientRecordIds;
    private List<String> failedClientRecordIds;

    public SyncTestRecordResponse() {
    }

    public SyncTestRecordResponse(
            int totalReceived,
            int successfullySynced,
            int failed,
            List<String> syncedClientRecordIds,
            List<String> failedClientRecordIds) {

        this.totalReceived = totalReceived;
        this.successfullySynced = successfullySynced;
        this.failed = failed;
        this.syncedClientRecordIds = syncedClientRecordIds;
        this.failedClientRecordIds = failedClientRecordIds;
    }

    public int getTotalReceived() {
        return totalReceived;
    }

    public int getSuccessfullySynced() {
        return successfullySynced;
    }

    public int getFailed() {
        return failed;
    }

    public List<String> getSyncedClientRecordIds() {
        return syncedClientRecordIds;
    }

    public List<String> getFailedClientRecordIds() {
        return failedClientRecordIds;
    }
}