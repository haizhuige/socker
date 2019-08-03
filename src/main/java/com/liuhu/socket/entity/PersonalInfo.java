package com.liuhu.socket.entity;

import java.io.Serializable;
import java.util.Date;

public class PersonalInfo implements Serializable {
    private Integer id;

    private String personId;

    private Double totalAmount;

    private String amountDetail;

    private Date marketDate;

    private String type;

    private Date updateDate;

    private String status;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId == null ? null : personId.trim();
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getAmountDetail() {
        return amountDetail;
    }

    public void setAmountDetail(String amountDetail) {
        this.amountDetail = amountDetail == null ? null : amountDetail.trim();
    }

    public Date getMarketDate() {
        return marketDate;
    }

    public void setMarketDate(Date marketDate) {
        this.marketDate = marketDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
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
        PersonalInfo other = (PersonalInfo) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getPersonId() == null ? other.getPersonId() == null : this.getPersonId().equals(other.getPersonId()))
            && (this.getTotalAmount() == null ? other.getTotalAmount() == null : this.getTotalAmount().equals(other.getTotalAmount()))
            && (this.getAmountDetail() == null ? other.getAmountDetail() == null : this.getAmountDetail().equals(other.getAmountDetail()))
            && (this.getMarketDate() == null ? other.getMarketDate() == null : this.getMarketDate().equals(other.getMarketDate()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
            && (this.getUpdateDate() == null ? other.getUpdateDate() == null : this.getUpdateDate().equals(other.getUpdateDate()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPersonId() == null) ? 0 : getPersonId().hashCode());
        result = prime * result + ((getTotalAmount() == null) ? 0 : getTotalAmount().hashCode());
        result = prime * result + ((getAmountDetail() == null) ? 0 : getAmountDetail().hashCode());
        result = prime * result + ((getMarketDate() == null) ? 0 : getMarketDate().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getUpdateDate() == null) ? 0 : getUpdateDate().hashCode());
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
        sb.append(", personId=").append(personId);
        sb.append(", totalAmount=").append(totalAmount);
        sb.append(", amountDetail=").append(amountDetail);
        sb.append(", marketDate=").append(marketDate);
        sb.append(", type=").append(type);
        sb.append(", updateDate=").append(updateDate);
        sb.append(", status=").append(status);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}