

		public class InvalidUnitsException extends Exception {
		    private double rejectedValue;
		    public InvalidUnitsException(String message, double rejectedValue) {
		        super(message);
		        this.rejectedValue = rejectedValue;
		    }
		    public double getRejectedValue() { return rejectedValue; }
		

	}


