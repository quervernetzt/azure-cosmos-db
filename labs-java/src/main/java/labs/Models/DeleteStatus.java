package labs.Models;

public class DeleteStatus {
	private int deleted;
	private boolean continuation;

	public int getDeleted() {
		return deleted;
	}

	public boolean isContinuation() {
		return continuation;
	}

	public void setContinuation(final boolean continuation) {
		this.continuation = continuation;
	}

	public void setDeleted(final int deleted) {
		this.deleted = deleted;
	}
}