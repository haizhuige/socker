package com.liuhu.socket.domain.output;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class MarketRealTimeOutputDomain implements Serializable {

	private static final long serialVersionUID = -923156671835410830L;

	private String shareCode;

    private Double CurrentValue;

    private Double high;

    private Double low;

    private Double currentPercent;

    private Double amount;

    private Double lastClose;






	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MarketRealTimeOutputDomain person = (MarketRealTimeOutputDomain) o;
		return shareCode == person.shareCode &&
				shareCode.equals(person.shareCode);
	}

	@Override
	public int hashCode() {
		return shareCode.hashCode();
	}
}
