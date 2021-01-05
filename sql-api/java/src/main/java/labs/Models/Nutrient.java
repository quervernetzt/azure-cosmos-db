package labs.Models;

import java.math.BigDecimal;

public class Nutrient {
	private String id;
	private String description;
	private BigDecimal nutritionValue;
	private String units;

	public Nutrient() {
		this.id = "";
		this.description = "";
		this.nutritionValue = new BigDecimal("0.0");
		this.units = "";
	}

	public String getId() {
		return id;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(final String units) {
		this.units = units;
	}

	public BigDecimal getNutritionValue() {
		return nutritionValue;
	}

	public void setNutritionValue(final BigDecimal nutritionValue) {
		this.nutritionValue = nutritionValue;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setId(final String id) {
		this.id = id;
	}
}
