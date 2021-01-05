package labs.Models;

import java.math.BigDecimal;
import java.util.UUID;

import com.github.javafaker.Faker;

public class Transaction {
	private String id;
	private BigDecimal amount;
	private boolean processed;
	private String paidBy;
	private String costCenter;

	public Transaction(
			final String id,
			final BigDecimal amount,
			final boolean processed,
			final String paidBy,
			final String costCenter) {
		this.id = id;
		this.amount = amount;
		this.processed = processed;
		this.paidBy = paidBy;
		this.costCenter = costCenter;
	}

	public Transaction() {
		this("", new BigDecimal("0.00"), false, "", "");

		Faker faker = new Faker();

		this.id = UUID.randomUUID().toString();
		this.amount = new BigDecimal(faker.commerce().price().replace(",", "."));
		this.processed = faker.bool().bool();
		this.costCenter = faker.address().cityName();
	}

	public String getId() {
		return id;
	}

	public String getCostCenter() {
		return costCenter;
	}

	public void setCostCenter(final String costCenter) {
		this.costCenter = costCenter;
	}

	public String getPaidBy() {
		return paidBy;
	}

	public void setPaidBy(final String paidBy) {
		this.paidBy = paidBy;
	}

	public boolean isProcessed() {
		return processed;
	}

	public void setProcessed(final boolean processed) {
		this.processed = processed;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(final BigDecimal amount) {
		this.amount = amount;
	}

	public void setId(final String id) {
		this.id = id;
	}
}
