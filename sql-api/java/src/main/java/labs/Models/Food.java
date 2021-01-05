package labs.Models;

import java.util.ArrayList;
import java.util.List;

public class Food extends BaseModel {
	private String description;
	private String manufacturerName;
	private List<Tag> tags;
	private String foodGroup;
	private List<Nutrient> nutrients;
	private List<Serving> servings;

	public List<Serving> getServings() {
		return servings;
	}

	public void setServings(final List<Serving> servings) {
		this.servings = servings;
	}

	public void addServing(final Serving serving) {
		if (this.servings == null)
			this.servings = new ArrayList<Serving>();

		this.servings.add(serving);
	}

	public List<Nutrient> getNutrients() {
		return nutrients;
	}

	public void setNutrients(final List<Nutrient> nutrients) {
		this.nutrients = nutrients;
	}

	public void addNutrient(final Nutrient nutrient) {
		if (this.nutrients == null)
			this.nutrients = new ArrayList<Nutrient>();

		this.nutrients.add(nutrient);
	}

	public String getFoodGroup() {
		return foodGroup;
	}

	public void setFoodGroup(final String foodGroup) {
		this.foodGroup = foodGroup;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(final List<Tag> tags) {
		this.tags = tags;
	}

	public void addTag(final Tag tag) {
		if (this.tags == null)
			this.tags = new ArrayList<Tag>();

		this.tags.add(tag);
	}

	public String getManufacturerName() {
		return manufacturerName;
	}

	public void setManufacturerName(final String manufacturerName) {
		this.manufacturerName = manufacturerName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}
}
