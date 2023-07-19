package com.liuhu.socket.entity;

import java.io.Serializable;
import java.util.Date;

public class MarketInfoNew implements Serializable {
    private Integer id;

    private String shareCode;

    private Double openValue;

    private Double endValue;

    private Double riseFall;

    private Double riseFallRatio;

    private Double highest;

    private Double lowest;

    private Long dealCount;

    private Double dealAmount;
    //换手率
    private Double turnOverRate;

    private Date date;

    private String riseFallRatioStr;

    private String shareName;

    private Double secondAmount;


    private static final long serialVersionUID = 1L;

    public void setSecondAmount(Double secondAmount) {
        this.secondAmount = secondAmount;
    }

    public Double getSecondAmount() {
        return secondAmount;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getShareCode() {
        return shareCode;
    }

    public void setShareCode(String shareCode) {
        this.shareCode = shareCode == null ? null : shareCode.trim();
    }

    public Double getOpenValue() {
        return openValue;
    }

    public void setOpenValue(Double openValue) {
        this.openValue = openValue;
    }

    public Double getEndValue() {
        return endValue;
    }

    public void setEndValue(Double endValue) {
        this.endValue = endValue;
    }

    public Double getRiseFall() {
        return riseFall;
    }

    public void setRiseFall(Double riseFall) {
        this.riseFall = riseFall;
    }

    public Double getRiseFallRatio() {
        return riseFallRatio;
    }

    public void setRiseFallRatio(Double riseFallRatio) {
        this.riseFallRatio = riseFallRatio;
    }

    public Double getHighest() {
        return highest;
    }

    public void setHighest(Double highest) {
        this.highest = highest;
    }

    public Double getLowest() {
        return lowest;
    }

    public void setLowest(Double lowest) {
        this.lowest = lowest;
    }

    public Long getDealCount() {
        return dealCount;
    }

    public void setDealCount(Long dealCount) {
        this.dealCount = dealCount;
    }

    public Double getDealAmount() {
        return dealAmount;
    }

    public void setDealAmount(Double dealAmount) {
        this.dealAmount = dealAmount;
    }

    public Double getTurnOverRate() {
        return turnOverRate;
    }

    public void setTurnOverRate(Double turnOverRate) {
        this.turnOverRate = turnOverRate;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setRiseFallRatioStr(String riseFallRatioStr) {
        this.riseFallRatioStr = riseFallRatioStr;
    }

    public String getRiseFallRatioStr() {
        return riseFallRatioStr;
    }

    public void setShareName(String shareName) {
        this.shareName = shareName;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getShareName() {
        return shareName;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        MarketInfoNew other = (MarketInfoNew) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getShareCode() == null ? other.getShareCode() == null : this.getShareCode().equals(other.getShareCode()))
            && (this.getOpenValue() == null ? other.getOpenValue() == null : this.getOpenValue().equals(other.getOpenValue()))
            && (this.getEndValue() == null ? other.getEndValue() == null : this.getEndValue().equals(other.getEndValue()))
            && (this.getRiseFall() == null ? other.getRiseFall() == null : this.getRiseFall().equals(other.getRiseFall()))
            && (this.getRiseFallRatio() == null ? other.getRiseFallRatio() == null : this.getRiseFallRatio().equals(other.getRiseFallRatio()))
            && (this.getHighest() == null ? other.getHighest() == null : this.getHighest().equals(other.getHighest()))
            && (this.getLowest() == null ? other.getLowest() == null : this.getLowest().equals(other.getLowest()))
            && (this.getDealCount() == null ? other.getDealCount() == null : this.getDealCount().equals(other.getDealCount()))
            && (this.getDealAmount() == null ? other.getDealAmount() == null : this.getDealAmount().equals(other.getDealAmount()))
            && (this.getTurnOverRate() == null ? other.getTurnOverRate() == null : this.getTurnOverRate().equals(other.getTurnOverRate()))
            && (this.getDate() == null ? other.getDate() == null : this.getDate().equals(other.getDate()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getShareCode() == null) ? 0 : getShareCode().hashCode());
        result = prime * result + ((getOpenValue() == null) ? 0 : getOpenValue().hashCode());
        result = prime * result + ((getEndValue() == null) ? 0 : getEndValue().hashCode());
        result = prime * result + ((getRiseFall() == null) ? 0 : getRiseFall().hashCode());
        result = prime * result + ((getRiseFallRatio() == null) ? 0 : getRiseFallRatio().hashCode());
        result = prime * result + ((getHighest() == null) ? 0 : getHighest().hashCode());
        result = prime * result + ((getLowest() == null) ? 0 : getLowest().hashCode());
        result = prime * result + ((getDealCount() == null) ? 0 : getDealCount().hashCode());
        result = prime * result + ((getDealAmount() == null) ? 0 : getDealAmount().hashCode());
        result = prime * result + ((getTurnOverRate() == null) ? 0 : getTurnOverRate().hashCode());
        result = prime * result + ((getDate() == null) ? 0 : getDate().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", shareCode=").append(shareCode);
        sb.append(", openValue=").append(openValue);
        sb.append(", endValue=").append(endValue);
        sb.append(", riseFall=").append(riseFall);
        sb.append(", riseFallRatio=").append(riseFallRatio);
        sb.append(", highest=").append(highest);
        sb.append(", lowest=").append(lowest);
        sb.append(", dealCount=").append(dealCount);
        sb.append(", dealAmount=").append(dealAmount);
        sb.append(", turnOverRate=").append(turnOverRate);
        sb.append(", date=").append(date);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}