package labs.Models;

import com.github.javafaker.Faker;

public class CardGeo {
	private double lat;
	private double lng;

	public CardGeo(final double lat, final double lng) {
		this.lat = lat;
		this.lng = lng;
	}

	public CardGeo() {
		this(0.0, 0.0);

		Faker faker = new Faker();
		this.lat = Double.parseDouble(faker.address().latitude().replace(',', '.'));
		this.lng = Double.parseDouble(faker.address().longitude().replace(',', '.'));
	}

	public double getLat() {
		return lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(final double lng) {
		this.lng = lng;
	}

	public void setLat(final double lat) {
		this.lat = lat;
	}
}
