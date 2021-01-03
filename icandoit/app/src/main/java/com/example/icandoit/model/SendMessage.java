package com.example.icandoit.model;

public class SendMessage {
    private String session_id;
    private boolean firstCall;
    private Input input;

    public SendMessage(String session_id, boolean firstCall, String message) {
        this.session_id = session_id;
        this.firstCall = firstCall;
        this.input = new Input(message);
    }

    class Input {
        private String message_type="text";
        private String text;
        Input (String text) {
            this.text = text;
        }

        public String getMessage() {
            return text;
        }
    }

    public String getSession_id() {
        return session_id;
    }

    public boolean isFirstCall() {
        return firstCall;
    }

    public String getMessage() {
        return input.getMessage();
    }
}
