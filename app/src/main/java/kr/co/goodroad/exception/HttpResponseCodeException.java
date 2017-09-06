package kr.co.goodroad.exception;

import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;

/**
 * Created by hjlee on 2017-08-03.
 */

public class HttpResponseCodeException extends Exception {
    StatusLine mStatusLine;
    public HttpResponseCodeException(StatusLine status) {
        mStatusLine = status;
    }

    private class HttpStatusLine implements StatusLine {

        ProtocolVersion protocolVersion;
        int statusCode;
        String reasonPhrase;
        public HttpStatusLine(ProtocolVersion protocolVersion, int statusCode, String reasonPhrase) {
            this.protocolVersion = protocolVersion;
            this.statusCode = statusCode;
            this.reasonPhrase = reasonPhrase;
        }

        @Override
        public ProtocolVersion getProtocolVersion() {
            return null;
        }

        @Override
        public int getStatusCode() {
            return 0;
        }

        @Override
        public String getReasonPhrase() {
            return null;
        }
    }

    public HttpResponseCodeException(ProtocolVersion protocolVersion, int statusCode, String reasonPhrase) {
        mStatusLine = new HttpStatusLine(protocolVersion,statusCode,reasonPhrase);
    }

    public StatusLine getmStatusLine() {
        return mStatusLine;
    }

    public void setmStatusLine(StatusLine mStatusLine) {
        this.mStatusLine = mStatusLine;
    }

    @Override
    public String getMessage() {
        String ret = mStatusLine.toString() + "(" + mStatusLine.getStatusCode() + ")";
        return ret;
    }
}
