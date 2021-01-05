package labs.Models;

import java.math.BigDecimal;

import labs.Interfaces.IInteraction;

public class PurchaseFoodOrBeverage implements IInteraction {
	String id;
	BigDecimal unitPrice;
	BigDecimal totalPrice;
	int quantity;
	String type;
	String _etag;

	public String getId() {
		return this.id;
	}

	public String getETag() {
		return _etag;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public BigDecimal getUnitPrice() {
		return new BigDecimal(this.unitPrice.toString());
	}

	public void setUnitPrice(final BigDecimal unitPrice) {
		this.unitPrice = new BigDecimal(unitPrice.toString());
	}

	public BigDecimal getTotalPrice() {
		return new BigDecimal(this.totalPrice.toString());
	}

	public void setTotalPrice(final BigDecimal totalPrice) {
		this.totalPrice = new BigDecimal(totalPrice.toString());
	}

	public int getQuantity() {
		return this.quantity;
	}

	public void setQuantity(final int quantity) {
		this.quantity = quantity;
	}

	public String getType() {
		return this.type;
	}

	public void setType(final String type) {
		this.type = type;
	}
}
