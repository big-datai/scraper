package com.utils.datatype;

public class TimeoutRegexCharSequence implements CharSequence{

	private final CharSequence inner;

    private final int timeoutMillis;

    private final long timeoutTime;

    private final String stringToMatch;

    private final String regularExpression;

    public TimeoutRegexCharSequence(CharSequence inner, int timeoutMillis, String stringToMatch, String regularExpression) {
        super();
        this.inner = inner;
        this.timeoutMillis = timeoutMillis;
        this.stringToMatch = stringToMatch;
        this.regularExpression = regularExpression;
        timeoutTime = System.currentTimeMillis() + timeoutMillis;
    }

    public char charAt(int index) {
        if (System.currentTimeMillis() > timeoutTime) {
            throw new RuntimeException("Timeout occurred after " + timeoutMillis + "ms while processing regular expression '"
                            + regularExpression + "' on input '" + stringToMatch + "'!");
        }
        return inner.charAt(index);
    }

    public int length() {
        return inner.length();
    }

    public CharSequence subSequence(int start, int end) {
        return new TimeoutRegexCharSequence(inner.subSequence(start, end), timeoutMillis, stringToMatch, regularExpression);
    }

    @Override
    public String toString() {
        return inner.toString();
    }


}
