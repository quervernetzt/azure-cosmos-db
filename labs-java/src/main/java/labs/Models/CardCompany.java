package labs.Models;

import com.github.javafaker.Faker;

public class CardCompany {
	private String name;
	private String catchPhrase;
	private String bs;

	public CardCompany(
			final String name,
			final String catchPhrase,
			final String bs) {
		this.name = name;
		this.catchPhrase = catchPhrase;
		this.bs = bs;
	}

	public CardCompany() {
		this("", "", "");

		Faker faker = new Faker();

		this.name = faker.company().name();
		this.catchPhrase = faker.company().catchPhrase();
		this.bs = faker.company().bs();
	}

	public String getName() {
		return name;
	}

	public String getBs() {
		return bs;
	}

	public void setBs(final String bs) {
		this.bs = bs;
	}

	public String getCatchPhrase() {
		return catchPhrase;
	}

	public void setCatchPhrase(final String catchPhrase) {
		this.catchPhrase = catchPhrase;
	}

	public void setName(final String name) {
		this.name = name;
	}
}
