package labs.Models;

import java.util.UUID;

public class Member {
	private String id;
	private Person accountHolder;
	private Family relatives;

	public Member(
			final String id,
			final Person accountHolder,
			final Family relatives) {
		this.id = id;
		this.accountHolder = accountHolder;
		this.relatives = relatives;
	}

	public Member() {
		this("", new Person(), new Family());

		this.id = UUID.randomUUID().toString();
		// Person and Family are taken care of
	}

	public Member(final Person accountHolder) {
		this("", accountHolder, new Family());

		this.id = UUID.randomUUID().toString();
	}

	public String getId() {
		return id;
	}

	public Family getRelatives() {
		return relatives;
	}

	public void setRelatives(final Family relatives) {
		this.relatives = relatives;
	}

	public Person getAccountHolder() {
		return accountHolder;
	}

	public void setAccountHolder(final Person accountHolder) {
		this.accountHolder = accountHolder;
	}

	public void setId(final String id) {
		this.id = id;
	}
}
