package labs.Models;

import java.util.ArrayList;
import java.util.List;

import com.github.javafaker.Faker;

public class Family {
	private Person spouse;
	private List<Person> children;

	public Family(final Person spouse, final List<Person> children) {
		this.spouse = spouse;
		this.children = children;
	}

	public Family() {
		this(new Person(), new ArrayList<Person>());

		Faker faker = new Faker();

		int num_children = faker.number().numberBetween(0, 10);

		for (int i = 0; i < num_children; i++)
			this.children.add(new Person());
	}

	public List<Person> getChildren() {
		return children;
	}

	public void setChildren(final List<Person> children) {
		this.children = children;
	}

	public void addChildren(final Person child) {
		this.children.add(child);
	}

	public Person getSpouse() {
		return spouse;
	}

	public void setSpouse(final Person spouse) {
		this.spouse = spouse;
	}
}
