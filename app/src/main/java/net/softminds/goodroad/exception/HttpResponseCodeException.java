package net.softminds.goodroad.exception;

import org.apache.http.StatusLine;

/**
 * Created by hjlee on 2017-08-03.
 */

public class HttpResponseCodeException extends Exception {
    StatusLine mStatusLine;
    public HttpResponseCodeException(StatusLine status) {
        mStatusLine = status;
    }

    public StatusLine getmStatusLine() {
        return mStatusLine;
    }

    public void setmStatusLine(StatusLine mStatusLine) {
        this.mStatusLine = mStatusLine;
    }

}
