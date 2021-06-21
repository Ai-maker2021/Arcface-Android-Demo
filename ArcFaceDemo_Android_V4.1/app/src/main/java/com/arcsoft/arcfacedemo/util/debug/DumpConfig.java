package com.arcsoft.arcfacedemo.util.debug;

public class DumpConfig {
    boolean dumpFaceTrackError;
    boolean dumpLivenessDetectResult;
    boolean dumpExtractError;
    boolean dumpCompareFailedError;
    boolean dumpPerformanceInfo;

    public boolean isDumpFaceTrackError() {
        return dumpFaceTrackError;
    }

    public void setDumpFaceTrackError(boolean dumpFaceTrackError) {
        this.dumpFaceTrackError = dumpFaceTrackError;
    }

    public boolean isDumpLivenessDetectResult() {
        return dumpLivenessDetectResult;
    }

    public void setDumpLivenessDetectResult(boolean dumpLivenessDetectResult) {
        this.dumpLivenessDetectResult = dumpLivenessDetectResult;
    }

    public boolean isDumpExtractError() {
        return dumpExtractError;
    }

    public void setDumpExtractError(boolean dumpExtractError) {
        this.dumpExtractError = dumpExtractError;
    }

    public boolean isDumpCompareFailedError() {
        return dumpCompareFailedError;
    }

    public void setDumpCompareFailedError(boolean dumpCompareFailedError) {
        this.dumpCompareFailedError = dumpCompareFailedError;
    }

    public boolean isDumpPerformanceInfo() {
        return dumpPerformanceInfo;
    }

    public void setDumpPerformanceInfo(boolean dumpPerformanceInfo) {
        this.dumpPerformanceInfo = dumpPerformanceInfo;
    }
}
