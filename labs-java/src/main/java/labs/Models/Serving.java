package labs.Models;

import java.math.BigDecimal;

public class Serving {
	private BigDecimal amount;
	private String description;
	private BigDecimal weightInGrams;

	public Serving() {
		this.amount = new BigDecimal("0.0");
		this.description = "";
		this.weightInGrams = new BigDecimal("0.0");
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public BigDecimal getWeightInGrams() {
		return weightInGrams;
	}

	public void setWeightInGrams(final BigDecimal weightInGrams) {
		this.weightInGrams = weightInGrams;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setAmount(final BigDecimal amount) {
		this.amount = amount;
	}
}
