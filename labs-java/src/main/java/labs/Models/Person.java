package labs.Models;

import java.util.Date;
import java.util.UUID;

import com.github.javafaker.Faker;

public class Person {
	private String id;
	private String website;
	private String gender;
	private String firstName;
	private String lastName;
	private String userName;
	private String avatar;
	private String email;
	private Date dateOfBirth;
	private CardAddress address;
	private String phone;
	private CardCompany company;

	public Person(
			final String id,
			final String website,
			final String gender,
			final String firstName,
			final String lastName,
			final String userName,
			final String avatar,
			final String email,
			final Date dateOfBirth,
			final CardAddress address,
			final String phone,
			final CardCompany company) {

		this.setId(id);
		this.website = website;
		this.gender = gender;
		this.firstName = firstName;
		this.lastName = lastName;
		this.userName = userName;
		this.avatar = avatar;
		this.email = email;
		this.dateOfBirth = dateOfBirth;
		this.address = address;
		this.phone = phone;
		this.company = company;
	}

	public Person() {
		this(UUID.randomUUID().toString(), "", "", "", "", "", "", "", new Date(), new CardAddress(), "",
				new CardCompany());

		Faker faker = new Faker();

		// id is already taken care of
		this.website = faker.internet().domainName();
		if (faker.bool().bool())
			this.gender = "female";
		else
			this.gender = "male";
		this.firstName = faker.name().firstName();
		this.lastName = faker.name().lastName();
		this.userName = faker.name().username();
		this.avatar = faker.funnyName().name();
		this.email = faker.internet().emailAddress();
		this.dateOfBirth = faker.date().birthday();
		// address is taken care of
		this.phone = faker.phoneNumber().phoneNumber();
		// company is taken care of
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getWebsite() {
		return website;
	}

	public CardCompany getCompany() {
		return company;
	}

	public void setCompany(final CardCompany company) {
		this.company = company;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(final String phone) {
		this.phone = phone;
	}

	public CardAddress getAddress() {
		return address;
	}

	public void setAddress(final CardAddress address) {
		this.address = address;
	}

	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(final Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(final String avatar) {
		this.avatar = avatar;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(final String userName) {
		this.userName = userName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(final String lastName) {
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(final String gender) {
		this.gender = gender;
	}

	public void setWebsite(final String website) {
		this.website = website;
	}
}
