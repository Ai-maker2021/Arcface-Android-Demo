package com.arcsoft.arcfacedemo.util.debug.model;

import com.arcsoft.arcfacedemo.util.face.constants.RequestFeatureStatus;
import com.arcsoft.arcfacedemo.util.face.model.RecognizeInfo;

import org.json.JSONException;
import org.json.JSONObject;

public class DebugRecognizeInfo extends RecognizeInfo {

    private int status = RequestFeatureStatus.DEFAULT;
    private long enterTime;
    private int trackId;
    private long ftCost;
    private long maskCost;
    private long frQualityCost;
    private long extractCost;
    private long livenessCost;
    private long compareCost;
    private long totalCost;

    public long getFrQualityCost() {
        return frQualityCost;
    }

    public void setFrQualityCost(long qualityCost) {
        this.frQualityCost = qualityCost;
    }

    public long getExtractCost() {
        return extractCost;
    }

    public void setExtractCost(long extractCost) {
        this.extractCost = extractCost;
    }

    public long getLivenessCost() {
        return livenessCost;
    }

    public void setLivenessCost(long livenessCost) {
        this.livenessCost = livenessCost;
    }

    public long getCompareCost() {
        return compareCost;
    }

    public void setCompareCost(long compareCost) {
        this.compareCost = compareCost;
    }

    public long getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(long totalCost) {
        this.totalCost = totalCost;
    }

    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    public void setEnterTime(long enterTime) {
        this.enterTime = enterTime;
    }

    public long getEnterTime() {
        return enterTime;
    }

    public long getFtCost() {
        return ftCost;
    }

    public void setFtCost(long ftProcessCost) {
        this.ftCost = ftProcessCost;
    }

    public long getMaskCost() {
        return maskCost;
    }

    public void setMaskCost(long maskCost) {
        this.maskCost = maskCost;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String performanceDaraToJsonString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("trackId", getTrackId());
            jsonObject.put("ftCost", getFtCost());
            jsonObject.put("maskCost", getMaskCost());
            jsonObject.put("frQualityCost", getFrQualityCost());
            jsonObject.put("frCost", getExtractCost());
            jsonObject.put("flCost", getLivenessCost());
            jsonObject.put("frCount", getExtractErrorRetryCount());
            jsonObject.put("flCount", getLivenessErrorRetryCount());
            jsonObject.put("compareCost", getCompareCost());
            jsonObject.put("totalCost", getTotalCost());
            jsonObject.put("status", getStatus());
        } catch (JSONException e) {
            return "error";
        }
        return jsonObject.toString();
    }

    public void resetCost() {
        setFtCost(0);
        setMaskCost(0);
        setFrQualityCost(0);
        setExtractCost(0);
        setLivenessCost(0);
        setCompareCost(0);
        setTotalCost(0);
    }
}
