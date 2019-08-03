package com.liuhu.socket.entity;

import java.io.Serializable;
import java.util.Date;

public class TradeInfo implements Serializable {
    private Integer id;

    private String shareCode;

    private String tradeNo;

    private Double amount;

    private Double unitAmount;

    private Double shareHand;

    private Date tradeDate;

    private Date updatedDate;

    private String type;

    private String status;

    private static final long serialVersionUID = 1L;

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

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo == null ? null : tradeNo.trim();
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getUnitAmount() {
        return unitAmount;
    }

    public void setUnitAmount(Double unitAmount) {
        this.unitAmount = unitAmount;
    }

    public Double getShareHand() {
        return shareHand;
    }

    public void setShareHand(Double shareHand) {
        this.shareHand = shareHand;
    }

    public Date getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(Date tradeDate) {
        this.tradeDate = tradeDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
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
        TradeInfo other = (TradeInfo) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getShareCode() == null ? other.getShareCode() == null : this.getShareCode().equals(other.getShareCode()))
            && (this.getTradeNo() == null ? other.getTradeNo() == null : this.getTradeNo().equals(other.getTradeNo()))
            && (this.getAmount() == null ? other.getAmount() == null : this.getAmount().equals(other.getAmount()))
            && (this.getUnitAmount() == null ? other.getUnitAmount() == null : this.getUnitAmount().equals(other.getUnitAmount()))
            && (this.getShareHand() == null ? other.getShareHand() == null : this.getShareHand().equals(other.getShareHand()))
            && (this.getTradeDate() == null ? other.getTradeDate() == null : this.getTradeDate().equals(other.getTradeDate()))
            && (this.getUpdatedDate() == null ? other.getUpdatedDate() == null : this.getUpdatedDate().equals(other.getUpdatedDate()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getShareCode() == null) ? 0 : getShareCode().hashCode());
        result = prime * result + ((getTradeNo() == null) ? 0 : getTradeNo().hashCode());
        result = prime * result + ((getAmount() == null) ? 0 : getAmount().hashCode());
        result = prime * result + ((getUnitAmount() == null) ? 0 : getUnitAmount().hashCode());
        result = prime * result + ((getShareHand() == null) ? 0 : getShareHand().hashCode());
        result = prime * result + ((getTradeDate() == null) ? 0 : getTradeDate().hashCode());
        result = prime * result + ((getUpdatedDate() == null) ? 0 : getUpdatedDate().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
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
        sb.append(", tradeNo=").append(tradeNo);
        sb.append(", amount=").append(amount);
        sb.append(", unitAmount=").append(unitAmount);
        sb.append(", shareHand=").append(shareHand);
        sb.append(", tradeDate=").append(tradeDate);
        sb.append(", updatedDate=").append(updatedDate);
        sb.append(", type=").append(type);
        sb.append(", status=").append(status);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}