package labs.Models;

import labs.Interfaces.IInteraction;

public class WatchLiveTelevisionChannel implements IInteraction {
	String id;
	String channelName;
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

	public String getChannelName() {
		return this.channelName;
	}

	public void setChannelName(final String channelName) {
		this.channelName = channelName;
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
