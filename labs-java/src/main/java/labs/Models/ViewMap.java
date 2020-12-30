package labs.Models;

import labs.Interfaces.IInteraction;

public class ViewMap implements IInteraction {
	String id;
	int minutesViewed;
	String type;
	String _etag;

	public String getId() {
		return this.id;
	};

	public String getETag() {
		return _etag;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public int getMinutesViewed() {
		return this.minutesViewed;
	}

	public void setMinutesViewed(final int minutesViewed) {
		this.minutesViewed = minutesViewed;
	}

	public String getType() {
		return this.type;
	}

	public void setType(final String type) {
		this.type = type;
	}
}
