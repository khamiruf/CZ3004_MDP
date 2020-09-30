package util;

import constant.Constants.MOVEMENT;

public class NextMove {
	 private boolean hasNextMove;
	    private MOVEMENT nextMove;

	    public NextMove(boolean hasNextMove, MOVEMENT nextMove) {
	        this.hasNextMove = hasNextMove;
	        this.nextMove = nextMove;
	    }

	    public boolean isHasNextMove() {
	        return hasNextMove;
	    }

	    public void setHasNextMove(boolean hasNextMove) {
	        this.hasNextMove = hasNextMove;
	    }

	    public MOVEMENT getNextMove() {
	        return nextMove;
	    }

	    public void setNextMove(MOVEMENT nextMove) {
	        this.nextMove = nextMove;
	    }

	    @Override
	    public String toString() {
	        return "NextMove [hasNextMove=" + hasNextMove + ", nextMove=" + nextMove + "]";
	    }
}
