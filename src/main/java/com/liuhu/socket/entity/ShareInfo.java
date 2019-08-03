package com.liuhu.socket.entity;

import java.io.Serializable;

public class ShareInfo implements Serializable {
    private String shareCode;

    private String shareName;

    private String status;

    private static final long serialVersionUID = 1L;

    public String getShareCode() {
        return shareCode;
    }

    public void setShareCode(String shareCode) {
        this.shareCode = shareCode == null ? null : shareCode.trim();
    }

    public String getShareName() {
        return shareName;
    }

    public void setShareName(String shareName) {
        this.shareName = shareName == null ? null : shareName.trim();
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
        ShareInfo other = (ShareInfo) that;
        return (this.getShareCode() == null ? other.getShareCode() == null : this.getShareCode().equals(other.getShareCode()))
            && (this.getShareName() == null ? other.getShareName() == null : this.getShareName().equals(other.getShareName()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getShareCode() == null) ? 0 : getShareCode().hashCode());
        result = prime * result + ((getShareName() == null) ? 0 : getShareName().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", shareCode=").append(shareCode);
        sb.append(", shareName=").append(shareName);
        sb.append(", status=").append(status);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}