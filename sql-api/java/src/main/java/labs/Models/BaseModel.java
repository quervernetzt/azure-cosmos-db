package labs.Models;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class BaseModel {
	private String id;
	private String _etag;

	public String getId() {
		return this.id;
	};

	public void setId(final String id) {
		this.id = id;
	}

	@JsonProperty("_etag")
	public String getETag() {
		return this._etag;
	}

	@JsonProperty("_etag")
	public String setETag(String _etag) {
		return this._etag = _etag;
	}
}
