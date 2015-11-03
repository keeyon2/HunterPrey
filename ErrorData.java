public class ErrorData {
    private String message;
    private Integer code;
    private String reason;
    private ErrorDataField data;
    
    public ErrorData() {
    }

    public ErrorData(String message, Integer code, String reason,
            ErrorDataField data) {
        this.message = message;
        this.code = code;
        this.reason = reason;
        this.data = data;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return this.code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getReason () {
        return this.reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ErrorDataField getData() {
        return this.data;
    }

    public void setData(ErrorDataField data) {
        this.data = data;
    }

    public class ErrorDataField {
        private String command;
        private String direction;

        public ErrorDataField() {
        }

        public ErrorDataField(String command, String direction) {
            this.command = command;
            this.direction = direction;
        }

        public String getCommand() {
            return this.command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public String getDirection() {
            return this.direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }
    }
}


