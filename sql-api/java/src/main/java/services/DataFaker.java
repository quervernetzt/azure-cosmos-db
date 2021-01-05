package services;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;

import com.github.javafaker.Faker;

import labs.Models.Food;
import labs.Models.Nutrient;
import labs.Models.PurchaseFoodOrBeverage;
import labs.Models.Serving;
import labs.Models.Tag;
import labs.Models.ViewMap;
import labs.Models.WatchLiveTelevisionChannel;

public class DataFaker {
	/**
	 * Generate fake PurchaseFoodOrBeverage objects.
	 * 
	 * @param numberOfFakeObjects Number of fake objects to generate
	 * @return List of generated PurchaseFoodOrBeverage objects
	 */
	public ArrayList<PurchaseFoodOrBeverage> generatePurchaseFoodOrBeverage(final int numberOfFakeObjects) {
		ArrayList<PurchaseFoodOrBeverage> foodInteractions = new ArrayList<PurchaseFoodOrBeverage>();
		Faker faker = new Faker();

		for (int i = 0; i < numberOfFakeObjects; i++) {
			PurchaseFoodOrBeverage doc = new PurchaseFoodOrBeverage();
			DecimalFormat df = new DecimalFormat("###,###");
			doc.setType("PurchaseFoodOrBeverage");
			doc.setQuantity(faker.random().nextInt(1, 5));
			String unitPrice = df.format(Double.valueOf((Double) faker.random().nextDouble()));
			doc.setUnitPrice(new BigDecimal(unitPrice));
			int quantity = Integer.valueOf((Integer) doc.getQuantity());
			String totalPrice = df.format(Double.valueOf(unitPrice) * quantity);
			doc.setTotalPrice(new BigDecimal(totalPrice));
			doc.setId(UUID.randomUUID().toString());
			foodInteractions.add(doc);
		}

		return foodInteractions;
	}

	/**
	 * Generate fake WatchLiveTelevisionChannel objects.
	 * 
	 * @param numberOfFakeObjects Number of fake objects to generate
	 * @return List of generated WatchLiveTelevisionChannel objects
	 */
	public ArrayList<WatchLiveTelevisionChannel> generateWatchLiveTelevisionChannel(final int numberOfFakeObjects) {
		ArrayList<WatchLiveTelevisionChannel> tvInteractions = new ArrayList<WatchLiveTelevisionChannel>();
		Faker faker = new Faker();

		for (int i = 0; i < numberOfFakeObjects; i++) {
			WatchLiveTelevisionChannel doc = new WatchLiveTelevisionChannel();

			doc.setChannelName(faker.funnyName().toString());
			doc.setMinutesViewed(faker.random().nextInt(1, 60));
			doc.setType("WatchLiveTelevisionChannel");
			doc.setId(UUID.randomUUID().toString());
			tvInteractions.add(doc);
		}

		return tvInteractions;
	}

	/**
	 * Generate fake ViewMap objects.
	 * 
	 * @param numberOfFakeObjects Number of fake objects to generate
	 * @return List of generated ViewMap objects
	 */
	public ArrayList<ViewMap> generateViewMap(final int numberOfFakeObjects) {
		ArrayList<ViewMap> mapInteractions = new ArrayList<ViewMap>();
		Faker faker = new Faker();

		for (int i = 0; i < numberOfFakeObjects; i++) {
			ViewMap doc = new ViewMap();

			doc.setMinutesViewed(faker.random().nextInt(1, 60));
			doc.setType("WatchLiveTelevisionChannel");
			doc.setId(UUID.randomUUID().toString());
			mapInteractions.add(doc);
		}

		return mapInteractions;
	}

	/**
	 * Generate fake Food objects.
	 * 
	 * @param numberOfFakeObjects Number of fake objects to generate
	 * @return
	 */
	public ArrayList<Food> generateFood(final int numberOfFakeObjects) {
		ArrayList<Food> foods = new ArrayList<Food>();
		Faker faker = new Faker();

		for (int i = 0; i < numberOfFakeObjects; i++) {
			Food food = new Food();

			food.setId(UUID.randomUUID().toString());
			food.setDescription(faker.food().dish());
			food.setManufacturerName(faker.company().name());
			food.setFoodGroup("Energy Bars");
			food.addTag(new Tag("Food"));
			food.addNutrient(new Nutrient());
			food.addServing(new Serving());
			foods.add(food);
		}

		return foods;
	}
}
