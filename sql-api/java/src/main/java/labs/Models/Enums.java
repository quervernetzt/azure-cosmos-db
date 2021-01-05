package labs.Models;

public class Enums {
	public enum CosmosDBStatusCodes {
		TooManyRequests {
			@Override
			public int getValue() {
				return 429;
			}
		},
		
		PreconditionFailure {
			@Override
			public int getValue() {
				return 412;
			}
		};

		public abstract int getValue();
	}
}
