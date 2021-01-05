package labs.Models;

import com.github.javafaker.Faker;

public class CardAddress {
	private String street;
	private String suite;
	private String city;
	private String state;
	private String zipCode;
	private CardGeo geo;

	public CardAddress(
			final String street,
			final String suite,
			final String city,
			final String state,
			final String zipCode,
			final CardGeo geo) {
		this.street = street;
		this.suite = suite;
		this.city = city;
		this.state = state;
		this.zipCode = zipCode;
		this.geo = geo;
	}

	public CardAddress() {
		this("", "", "", "", "", new CardGeo());

		Faker faker = new Faker();

		this.street = faker.name().firstName();
		this.suite = faker.address().buildingNumber();
		this.city = faker.address().city();
		this.state = faker.address().state();
		this.zipCode = faker.address().zipCode();
		// geo is taken care of
	}

	public String getStreet() {
		return street;
	}

	public CardGeo getGeo() {
		return geo;
	}

	public void setGeo(final CardGeo geo) {
		this.geo = geo;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(final String zipCode) {
		this.zipCode = zipCode;
	}

	public String getState() {
		return state;
	}

	public void setState(final String state) {
		this.state = state;
	}

	public String getCity() {
		return city;
	}

	public void setCity(final String city) {
		this.city = city;
	}

	public String getSuite() {
		return suite;
	}

	public void setSuite(final String suite) {
		this.suite = suite;
	}

	public void setStreet(final String street) {
		this.street = street;
	}
}
